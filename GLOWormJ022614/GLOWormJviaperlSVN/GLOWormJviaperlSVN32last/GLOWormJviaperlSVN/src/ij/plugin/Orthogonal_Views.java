package ij.plugin;
import ij.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.frame.RoiManager;
import ij.process.*;
import java.awt.*;
import java.awt.List;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import org.vcell.gloworm.MultiQTVirtualStack;
 
/**
 * 
* @author Dimiter Prodanov
* 		  IMEC
* 
* @acknowledgments Many thanks to Jerome Mutterer for the code contributions and testing.
* 				   Thanks to Wayne Rasband for the code that properly handles the image magnification.
* 		
* @version 		1.2 28 April 2009
* 					- added support for arrow keys
* 					- fixed a bug in the cross position calculation
* 					- added FocusListener behavior
* 					- added support for magnification factors
* 				1.1.6 31 March 2009
* 					- added AdjustmentListener behavior thanks to Jerome Mutterer
* 					- improved pane visualization
* 					- added window rearrangement behavior. Initial code suggested by Jerome Mutterer
* 					- bug fixes by Wayne Raspband
* 				1.1 24 March 2009
* 					- improved projection image resizing
* 					- added ImageListener behaviors
* 					- added check-ups
* 					- improved pane updating
* 				1.0.5 23 March 2009
* 					- fixed pane updating issue
* 				1.0 21 March 2009
* 
* @contents This plugin projects dynamically orthogonal XZ and YZ views of a stack. 
* The output images are calibrated, which allows measurements to be performed more easily. 
*/

public class Orthogonal_Views implements PlugIn, MouseListener, MouseMotionListener, KeyListener, ActionListener, 
	ImageListener, WindowListener, AdjustmentListener, MouseWheelListener, FocusListener, CommandListener {

	private ImageWindow win;
	private ImagePlus imp;
	private boolean rgb;
	public ImageStack imageStack;
	private boolean hyperstack;
	private int currentChannel, currentFrame; 
	private int currentMode = 10000;
	private ImageCanvas canvas;
	private static final int H_ROI=0, H_ZOOM=1;
	private static boolean sticky=true;
	private static int xzID, yzID, xyID;
	private static Orthogonal_Views instance;
	private ImagePlus xz_image, yz_image;
	/** ImageProcessors for the xz and yz images */
	private ImageProcessor fp1, fp2;
	private double ax, ay, az;
	private boolean rotateYZ = Prefs.rotateYZ;
	private boolean flipXZ = Prefs.flipXZ;
	
	private int xyX, xyY, xyW, xyH;
	private Calibration cal=null, cal_xz=new Calibration(), cal_yz=new Calibration();
	private double magnification=1.0;
	private Color color = Roi.getColor();
	private Updater updater = new Updater();
	private double min, max;
	private Dimension screen = IJ.getScreenSize();
	private boolean syncZoom = true;
	private Point crossLoc;
	private boolean firstTime = true;
	private static int previousID, previousX, previousY;
	private Rectangle startingSrcRect;
	private Roi[] stackRois = new Roi[1];
	private ArrayList<Roi> stackRoiArrayList = new ArrayList<Roi>();
	private ImageStack originalStack;
	private ColorModel originalCM;
	private int originalMode;
	private Color defaultWinColor;
	private ImagePlus xy_image;
	 
	public void run(String arg) {
		imp = IJ.getImage();
		originalStack = imp.getStack();
		originalCM = originalStack.getColorModel();
		defaultWinColor = new Color(imp.getWindow().getBackground().getRGB());
		if (instance!=null) {
			instance.dispose();
			return;
		}
		if (imp.getStackSize()==1) {
			IJ.error("Othogonal Views", "This command requires a stack.");
			return;
		}
		hyperstack = imp.isHyperStack();
		if ((hyperstack||imp.isComposite()) && imp.getNSlices()<=1) {
			IJ.error("Othogonal Views", "This command requires a stack, or a hypertack with Z>1.");
			return;
		}
		yz_image = WindowManager.getImage(yzID);
		rgb = imp.getBitDepth()==24 || hyperstack;
		int yzBitDepth = hyperstack?24:imp.getBitDepth();
		if (yz_image==null || yz_image.getHeight()!=imp.getHeight() || yz_image.getBitDepth()!=yzBitDepth){
			yz_image = new ImagePlus();
			yz_image.setMotherImp(imp.getMotherImp(), imp.getMotherFrame());
//			IJ.log("SET MOTHERIMP XZ");
		}
		xz_image = WindowManager.getImage(xzID);
		//if (xz_image!=null) IJ.log(imp+"  "+xz_image+"  "+xz_image.getHeight()+"  "+imp.getHeight()+"  "+xz_image.getBitDepth()+"  "+yzBitDepth);
		if (xz_image==null || xz_image.getWidth()!=imp.getWidth() || xz_image.getBitDepth()!=yzBitDepth) {
			xz_image = new ImagePlus();
//			IJ.log("NEW XZ");
			xz_image.setMotherImp(imp.getMotherImp(), imp.getMotherFrame());
//			IJ.log("SET MOTHERIMP YZ");
		}
		xy_image = WindowManager.getImage(xyID);
		//if (xy_image!=null) IJ.log(imp+"  "+xy_image+"  "+xy_image.getHeight()+"  "+imp.getHeight()+"  "+xy_image.getBitDepth()+"  "+yzBitDepth);
		if (xy_image==null || xy_image.getWidth()!=imp.getWidth() || xy_image.getBitDepth()!=yzBitDepth) {
			xy_image = new ImagePlus();
//			IJ.log("NEW xy");
			xy_image.setMotherImp(imp.getMotherImp(), imp.getMotherFrame());
//			IJ.log("SET MOTHERIMP YZ");
		}
		instance = this;
		ImageProcessor ip = (imp instanceof CompositeImage && imp.getNChannels()>1)?
							new ColorProcessor(imp.getImage()):imp.getProcessor();
		min = ip.getMin();
		max = ip.getMax();
		cal=this.imp.getCalibration();
		double calx=cal.pixelWidth;
		double caly=cal.pixelHeight;
		double calz=cal.pixelDepth;
		ax=1.0;
		ay=caly/calx;
		az=calz/calx;
		imageStack = getStack();
		win = xy_image.getWindow();
		canvas = win.getCanvas();
		addListeners(canvas);
		magnification= canvas.getMagnification();
		imp.deleteRoi();
		Rectangle r = canvas.getSrcRect();
		if (imp.getID()==previousID)
			crossLoc = new Point(previousX, previousY);
		else
			crossLoc = new Point(r.x+r.width/2, r.y+r.height/2);
		calibrate();
		if (createProcessors(imageStack)) {
			if (ip.isColorLut() || ip.isInvertedLut()) {
				ColorModel cm = ip.getColorModel();
				fp1.setColorModel(cm);
				fp1.setMinAndMax(ip.getMin(), ip.getMax());
				fp2.setColorModel(cm);				
				fp2.setMinAndMax(ip.getMin(), ip.getMax());
			}
//			IJ.log("run");
			update();
		} else
			dispose();
	}
	
	private ImageStack getStack() {
		if (true/*imp.isHyperStack()*/) {
			boolean xy_setup =false;
			if (xy_image == null)
				xy_setup =true;
			if (xy_setup  || (imp.isComposite() && ((CompositeImage)imp).getMode() != currentMode)) {
				xy_image.flush();
				xy_image = imp.createImagePlus();
			}
			xy_image.setTitle(imp.getTitle()+" t="+imp.getFrame());
			if (imp.isComposite() && currentMode == 10000) {
				originalMode =  ((CompositeImage)imp).getMode();
			}
			if (imp.isComposite()) {
//				((CompositeImage)imp).setMode(CompositeImage.GRAYSCALE);
				currentMode = ((CompositeImage)imp).getMode();
			}
			imp.deleteRoi();
			int slices = imp.getNSlices();
			ImageStack stack = new ImageStack(imp.getWidth(), imp.getHeight());
			int c=imp.getChannel(), z=imp.getSlice(), t=imp.getFrame();
			
			stackRoiArrayList.clear();
			RoiManager rm = imp.getRoiManager();
			Roi[] allRois = rm.getShownRoisAsArray();

			for (int j=0; j<allRois.length; j++) {
				if ( (Math.abs(t-allRois[j].getTPosition())<rm.getTSustain() || allRois[j].getTPosition() ==0)							
						&& (allRois[j].getCPosition() == c || allRois[j].getCPosition() == 0)) {
					stackRoiArrayList.add((Roi) allRois[j].clone());
					if (t-allRois[j].getTPosition() !=0) {
						stackRoiArrayList.get(stackRoiArrayList.size()-1).setStrokeColor(stackRoiArrayList.get(stackRoiArrayList.size()-1).getStrokeColor().darker());
					}
				}
			}	

			Arrays.fill(stackRois, null);
			stackRois = (Roi[]) stackRoiArrayList.toArray(stackRois);

			stackRoiArrayList.clear();

			for (int i=1; i<=slices; i++) {
				int slicePos = (t-1)*imp.getNSlices() + (i-1)*imp.getNChannels() + c;
				if (imp instanceof CompositeImage && imp.getNChannels()>1 && currentMode < CompositeImage.GRAYSCALE) {
					imp.getWindow().setEnabled(false);
					imp.setPositionWithoutUpdate(c, i, t);
					((CompositeImage)imp).setMode(currentMode);
					stack.addSlice(new ColorProcessor(imp.getImage()));
				} else if (imp.getStack() instanceof VirtualStack) {
					imp.getWindow().setEnabled(false);
					imp.setPositionWithoutUpdate(c, i, t);
					stack.addSlice(imp.getProcessor());
				} else {
					imp.getWindow().setEnabled(false);
					imp.setPositionWithoutUpdate(c, i, t);
					stack.addSlice(imp.getProcessor());
				}
			}
			if (imp.isComposite()) {
				if (((StackWindow)imp.getWindow()).cSelector != null)
					((StackWindow)imp.getWindow()).cSelector.setIconEnabled(false);
			}
			if (imp.getStack().isVirtual()) {
				if (((StackWindow)imp.getWindow()).tSelector != null)
					((StackWindow)imp.getWindow()).tSelector.setEnabled(false);
			}
			if (imp.isComposite() && imp.getStack().isVirtual()) {
				if (((StackWindow)imp.getWindow()).zSelector != null)
					((StackWindow)imp.getWindow()).zSelector.setEnabled(false);
			}


			imp.setPosition(c, z, t);
			imp.getWindow().setEnabled(true);
			currentChannel = c;
			currentFrame = t;
			imp.deleteRoi();
			xy_image.setStack(stack);
			xy_image.show();
			while (xy_image.getWindow() == null) IJ.wait(10);
			xy_image.getWindow().setBackground(Color.yellow);
			if (xy_setup)
				xy_image.getWindow().setLocation(imp.getWindow().getLocation());
			xyID = xy_image.getID();
			for (Roi stackRoi:stackRois) {
				if (stackRoi != null) {
					xy_image.setPositionWithoutUpdate(1, stackRoi.getZPosition(), 1);
					xy_image.getRoiManager().addRoi(stackRoi);
				}
			}
			xy_image.getRoiManager().showAll(RoiManager.SHOW_ALL);
			xy_image.getRoiManager().setZSustain(imp.getRoiManager().getZSustain());
			return stack;
		} else
			return imp.getStack();
	}
 
	private void addListeners(ImageCanvas canvass) {
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);
		win.addWindowListener (this);  
		win.addMouseWheelListener(this);
		win.addFocusListener(this);
//		Component[] c = win.getComponents();
//		//IJ.log(c[1].toString());
//		((ScrollbarWithLabel) c[1]).addAdjustmentListener (this);
		((StackWindow) win).zSelector.addAdjustmentListener(this);
		ImagePlus.addImageListener(this);
		Executer.addCommandListener(this);

	}
	 
	private void calibrate() {
		String unit=cal.getUnit();
		double o_depth=cal.pixelDepth;
		double o_height=cal.pixelHeight;
		double o_width=cal.pixelWidth;
		cal_yz.setUnit(unit);
		if (rotateYZ) {
			cal_yz.pixelHeight=o_depth/az;
			cal_yz.pixelWidth=o_height;
		} else {
			cal_yz.pixelWidth=o_depth/az;
			cal_yz.pixelHeight=o_height;
		}
		yz_image.setCalibration(cal_yz);
		cal_xz.setUnit(unit);
		cal_xz.pixelWidth=o_width;
		cal_xz.pixelHeight=o_depth/az;
		xz_image.setCalibration(cal_xz);
	}

	private void updateMagnification(int x, int y) {
        double magnification= win.getCanvas().getMagnification();
        int z = xy_image.getSlice()-1;
        ImageWindow xz_win = xz_image.getWindow();
        if (xz_win==null) return;
        ImageCanvas xz_ic = xz_win.getCanvas();
        double xz_mag = xz_ic.getMagnification();
        double arat = az/ax;
		int zcoord=(int)(arat*z);
		if (flipXZ) zcoord=(int)(arat*(xy_image.getNSlices()-z));
        while (xz_mag<magnification) {
        	xz_ic.zoomIn(xz_ic.screenX(x), xz_ic.screenY(zcoord));
        	xz_mag = xz_ic.getMagnification();
        }
        while (xz_mag>magnification) {
        	xz_ic.zoomOut(xz_ic.screenX(x), xz_ic.screenY(zcoord));
        	xz_mag = xz_ic.getMagnification();
        }
        ImageWindow yz_win = yz_image.getWindow();
        if (yz_win==null) return;
        ImageCanvas yz_ic = yz_win.getCanvas();
        double yz_mag = yz_ic.getMagnification();
		zcoord = (int)(arat*z);
        while (yz_mag<magnification) {
        	//IJ.log(magnification+"  "+yz_mag+"  "+zcoord+"  "+y+"  "+x);
        	yz_ic.zoomIn(yz_ic.screenX(zcoord), yz_ic.screenY(y));
        	yz_mag = yz_ic.getMagnification();
        }
        while (yz_mag>magnification) {
        	yz_ic.zoomOut(yz_ic.screenX(zcoord), yz_ic.screenY(y));
        	yz_mag = yz_ic.getMagnification();
        }
	}
	
	void updateViews(Point p, ImageStack iStack) {
		ImageProcessor ip = null;
		if (imp instanceof CompositeImage && imp.getNChannels()>1 && currentMode < CompositeImage.GRAYSCALE)
			ip = xy_image.getProcessor();
		else
			ip = imp.getProcessor();
		if (!(ip instanceof ColorProcessor)) {
			ColorModel cm = ip.getColorModel();
			iStack.setColorModel(cm);
			fp1.setColorModel(cm);
			fp2.setColorModel(cm);				
		}
		min = ip.getMin();
		max = ip.getMax();
		fp1.setMinAndMax(min, max);
		fp2.setMinAndMax(min, max);

		if (fp1==null) return;
		updateXZView(p,iStack);
		
		double arat=az/ax;
		int width2 = fp1.getWidth();
		int height2 = (int)Math.round(fp1.getHeight()*az);
		if (width2!=fp1.getWidth()||height2!=fp1.getHeight()) {
			fp1.setInterpolate(true);
			ImageProcessor sfp1=fp1.resize(width2, height2);
			if (!rgb) sfp1.setMinAndMax(min, max);
			xz_image.setProcessor("XZ "+p.y +" "+ xy_image.getTitle(), sfp1);
		} else {
			if (!rgb) fp1.setMinAndMax(min, max);
	    	xz_image.setProcessor("XZ "+p.y+" "+ xy_image.getTitle(), fp1);
		}
			
		if (rotateYZ)
			updateYZView(p, iStack);
		else
			updateZYView(p, iStack);
				
		width2 = (int)Math.round(fp2.getWidth()*az);
		height2 = fp2.getHeight();
		String title = "YZ ";
		if (rotateYZ) {
			width2 = fp2.getWidth();
			height2 = (int)Math.round(fp2.getHeight()*az);
			title = "ZY ";
		}
		//IJ.log("updateViews "+width2+" "+height2+" "+arat+" "+ay+" "+fp2);
		if (width2!=fp2.getWidth()||height2!=fp2.getHeight()) {
			fp2.setInterpolate(true);
			ImageProcessor sfp2=fp2.resize(width2, height2);
			if (!rgb) sfp2.setMinAndMax(min, max);
			yz_image.setProcessor(title+p.x+" "+ xy_image.getTitle(), sfp2);
		} else {
			if (!rgb) fp2.setMinAndMax(min, max);
			yz_image.setProcessor(title+p.x+" "+ xy_image.getTitle(), fp2);
		}
		
		calibrate();
		if (yz_image.getWindow()==null) {
			yz_image.show();
			yz_image.getWindow().setBackground(Color.cyan);
			ImageCanvas ic = yz_image.getCanvas();
			ic.addKeyListener(this);
			ic.addMouseListener(this);
			ic.addMouseMotionListener(this);
			ic.addMouseListener(yz_image.getMotherImp().getRoiManager().getColorLegend());
			ic.addMouseMotionListener(yz_image.getMotherImp().getRoiManager().getColorLegend());

			ic.setCustomRoi(true);
			yz_image.getWindow().addMouseWheelListener(this);
			yzID = yz_image.getID();
			yz_image.getWindow().setSubTitleBkgdColor(xy_image.getWindow().getBackground());
			yz_image.getRoiManager().showAll(RoiManager.SHOW_ALL);
	

		} else {
			ImageCanvas ic = yz_image.getWindow().getCanvas();
			yz_image.getRoiManager().showAll(RoiManager.SHOW_ALL);
		}
		if (xz_image.getWindow()==null) {
			xz_image.show();
			xz_image.getWindow().setBackground(Color.magenta);
			ImageCanvas ic = xz_image.getCanvas();
			ic.addKeyListener(this);
			ic.addMouseListener(this);
			ic.addMouseMotionListener(this);
			ic.setCustomRoi(true);
			ic.addMouseListener(xz_image.getMotherImp().getRoiManager().getColorLegend());
			ic.addMouseMotionListener(xz_image.getMotherImp().getRoiManager().getColorLegend());
			xz_image.getWindow().addMouseWheelListener(this);
			xzID = xz_image.getID();
			xz_image.getWindow().setSubTitleBkgdColor(xy_image.getWindow().getBackground());
			xz_image.getRoiManager().showAll(RoiManager.SHOW_ALL);			

		} else {
			ImageCanvas ic = xz_image.getWindow().getCanvas();
			xz_image.getRoiManager().showAll(RoiManager.SHOW_ALL);			
		}
 
	}
	
	void arrangeWindows(boolean sticky) {
		ImageWindow xyWin = xy_image.getWindow();
		if (xyWin==null) return;
		Point loc = xyWin.getLocation();
		if ((xyX!=loc.x)||(xyY!=loc.y)
				||(xyW!=xyWin.getWidth())||(xyH!=xyWin.getHeight())) {
			xyX =  loc.x;
			xyY =  loc.y;
			xyW = xyWin.getWidth();
			xyH = xyWin.getHeight();
 			ImageWindow yzWin =null;
 			long start = System.currentTimeMillis();
 			while (yzWin==null && (System.currentTimeMillis()-start)<=2500L) {
				yzWin = yz_image.getWindow();
				if (yzWin==null) IJ.wait(50);
			}
			if (yzWin!=null)
 				yzWin.setLocation(xyX+xyWin.getWidth(), xyY);
			ImageWindow xzWin =null;
 			start = System.currentTimeMillis();
 			while (xzWin==null && (System.currentTimeMillis()-start)<=2500L) {
				xzWin = xz_image.getWindow();
				if (xzWin==null) IJ.wait(50);
			}
			if (xzWin!=null)
 				xzWin.setLocation(xyX,xyY+xyWin.getHeight());
 			if (firstTime) {
 				xy_image.getWindow().toFront();
 				if (hyperstack)
 					imp.setPosition(imp.getChannel(), imp.getSlice(), imp.getFrame());
 				else
 					imp.setSlice(imp.getNSlices()/2);
 				firstTime = false;
 			}
		}
	}
	
	/**
	 * @param iStack - used to get the dimensions of the new ImageProcessors
	 * @return
	 */
	boolean createProcessors(ImageStack iStack) {
		ImageProcessor ip=iStack.getProcessor(1);
		int width= iStack.getWidth();
		int height=iStack.getHeight();
		int ds=iStack.getSize(); 
		double arat=1.0;//az/ax;
		double brat=1.0;//az/ay;
		int za=(int)(ds*arat);
		int zb=(int)(ds*brat);
		//IJ.log("za: "+za +" zb: "+zb);
		
		if (ip instanceof FloatProcessor) {
			fp1=new FloatProcessor(width,za);
			if (rotateYZ)
				fp2=new FloatProcessor(height,zb);
			else
				fp2=new FloatProcessor(zb,height);
			return true;
		}
		
		if (ip instanceof ByteProcessor) {
			fp1=new ByteProcessor(width,za);
			if (rotateYZ)
				fp2=new ByteProcessor(height,zb);
			else
				fp2=new ByteProcessor(zb,height);
			return true;
		}
		
		if (ip instanceof ShortProcessor) {
			fp1=new ShortProcessor(width,za);
			if (rotateYZ)
				fp2=new ShortProcessor(height,zb);
			else
				fp2=new ShortProcessor(zb,height);
			//IJ.log("createProcessors "+rotateYZ+"  "+height+"   "+zb+"  "+fp2);
			return true;
		}
		
		if (ip instanceof ColorProcessor) {
			fp1=new ColorProcessor(width,za);
			if (rotateYZ)
				fp2=new ColorProcessor(height,zb);
			else
				fp2=new ColorProcessor(zb,height);
			return true;
		}
		
		return false;
	}
	
	void updateXZView(Point p, ImageStack iStack) {
		xy_image.deleteRoi();
				int width= iStack.getWidth();
		int size=iStack.getSize();
		ImageProcessor ip=iStack.getProcessor(1);
		int y=p.y;
		
		//loop will set up RoiManager
		xz_image.getRoiManager().getListModel().removeAllElements();
		xz_image.getRoiManager().getROIs().clear();
		if (xz_image.getWindow() != null && imp.getRoiManager().isShowAll()) {
			if ( stackRois[0] != null) {
				ArrayList<Roi> cloneeRois = new ArrayList<Roi>();
				for (int r = 0; r < stackRois.length; r++) {
					Rectangle srb = stackRois[r].getBounds();
					for (int x = 0; x < width; x++) {
						if (srb.contains(x, y) && !cloneeRois.contains(stackRois[r])) {
							if (stackRois[r] instanceof TextRoi) {
								Roi nextRoi = (Roi) stackRois[r].clone();
								cloneeRois.add(stackRois[r]);
								nextRoi.setLocation(
										(int) (nextRoi.getBounds().x) ,
										(int) (nextRoi.getZPosition()
												* imp.getCalibration().pixelDepth - srb.getHeight() / 2));
								xz_image.getRoiManager().addRoi(nextRoi);
								//							x = x+srb.width;
								//							r=stackRois.length;
							}
						}
					}
				}
			}
			xz_image.getRoiManager().showAll(RoiManager.SHOW_ALL);
		}

		// XZ
		if (ip instanceof ShortProcessor) {
			short[] newpix=new short[width*size];
			for (int i=0; i<size; i++) { 
				Object pixels=iStack.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
		if (ip instanceof ByteProcessor) {
			byte[] newpix=new byte[width*size];
			for (int i=0;i<size; i++) { 
				Object pixels=iStack.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
		if (ip instanceof FloatProcessor) {
			float[] newpix=new float[width*size];
			for (int i=0; i<size; i++) { 
				Object pixels=iStack.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
		if (ip instanceof ColorProcessor) {
			int[] newpix=new int[width*size];
			for (int i=0;i<size; i++) { 
				Object pixels=iStack.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
	}
	
	void updateYZView(Point p, ImageStack iStack) {
		int width= iStack.getWidth();
		int height=iStack.getHeight();
		int ds=iStack.getSize();
		ImageProcessor ip=iStack.getProcessor(1);
		int x=p.x;
		
		if (ip instanceof FloatProcessor) {
			float[] newpix=new float[ds*height];
			for (int i=0;i<ds; i++) { 
				float[] pixels= (float[]) iStack.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ByteProcessor) {
			byte[] newpix=new byte[ds*height];
			for (int i=0;i<ds; i++) { 
				byte[] pixels= (byte[]) iStack.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ShortProcessor) {
			short[] newpix=new short[ds*height];
			for (int i=0;i<ds; i++) { 
				short[] pixels= (short[]) iStack.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ColorProcessor) {
			int[] newpix=new int[ds*height];
			for (int i=0;i<ds; i++) { 
				int[] pixels= (int[]) iStack.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		if (!flipXZ) fp2.flipVertical();
		
	}
	
	void updateZYView(Point p, ImageStack iStack) {
		xy_image.deleteRoi();
		int width= iStack.getWidth();
		int height=iStack.getHeight();
		int ds=iStack.getSize();
		ImageProcessor ip=iStack.getProcessor(1);
		int x=p.x;

		//loop will set up RoiManager
		yz_image.getRoiManager().getListModel().removeAllElements();
		yz_image.getRoiManager().getROIs().clear();
		if (yz_image.getWindow() != null  && imp.getRoiManager().isShowAll()) {
			if (stackRois[0] != null ) {
				for (int r = 0; r < stackRois.length; r++) {
					Rectangle srb = stackRois[r].getBounds();
					for (int y = 0; y < height; y++) {
						if (srb.contains(x, y)) {
							Roi nextRoi = (Roi) stackRois[r].clone();
							nextRoi.setLocation((int) (nextRoi.getZPosition()
									* imp.getCalibration().pixelDepth - srb.getWidth() / 2),
									(int) (nextRoi.getBounds().y));
							yz_image.getRoiManager().addRoi(nextRoi);
//							y = y + srb.height;
//							r=stackRois.length;
						}
					}
				}
			}
			yz_image.getRoiManager().showAll(RoiManager.SHOW_ALL);
		}
		
		if (ip instanceof FloatProcessor) {
			float[] newpix=new float[ds*height];
			for (int i=0;i<ds; i++) { 
				float[] pixels= (float[]) iStack.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ByteProcessor) {
			byte[] newpix=new byte[ds*height];
			for (int i=0;i<ds; i++) { 
				byte[] pixels= (byte[]) iStack.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ShortProcessor) {
			short[] newpix=new short[ds*height];
			for (int i=0;i<ds; i++) { 
				short[] pixels= (short[]) iStack.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ColorProcessor) {
			int[] newpix=new int[ds*height];
			for (int i=0;i<ds; i++) { 
				int[] pixels= (int[]) iStack.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
	}
	 
	/** draws the crosses in the images */
	void drawCross(ImagePlus imp, Point p, Line vLine, Line hLine) {
		int width=imp.getWidth();
		int height=imp.getHeight();
		float x = p.x;
		float y = p.y;
		hLine = new Line(0f, y, width, y);
		vLine = new Line(x, 0f, x, height);	
	}
	      
	void dispose() {
		if (((StackWindow)imp.getWindow()).cSelector != null)
			((StackWindow)imp.getWindow()).cSelector.setIconEnabled(true);
		updater.quit();
		updater = null;
		xy_image.setOverlay(null);
		int recentZ = xy_image.getSlice();
		canvas.removeMouseListener(this);
		canvas.removeMouseMotionListener(this);
		canvas.removeKeyListener(this);
		canvas.setCustomRoi(false);
		xz_image.setOverlay(null);
		xz_image.getRoiManager().dispose();
		WindowManager.removeWindow(xz_image.getRoiManager());
		ImageWindow win1 = xz_image.getWindow();
		if (win1!=null) {
			win1.removeMouseWheelListener(this);
			ImageCanvas ic = win1.getCanvas();
			if (ic!=null) {
			//IJ.log("TEST1");
				ic.removeKeyListener(this);
				ic.removeMouseListener(this);
				ic.removeMouseMotionListener(this);
				ic.setCustomRoi(false);
				win1.removeWindowListener(this);
				win1.removeFocusListener(this);
				win1.setResizable(true);
				win1.setBackground(defaultWinColor);
			}
		}
		yz_image.setOverlay(null);
		yz_image.getRoiManager().dispose();
		WindowManager.removeWindow(yz_image.getRoiManager());
		ImageWindow win2 = yz_image.getWindow();
		if (win2!=null) {
			win2.removeMouseWheelListener(this);
			ImageCanvas ic = win2.getCanvas();
			if (ic!=null) {
			//IJ.log("TEST2");
				ic.removeKeyListener(this);
				ic.removeMouseListener(this);
				ic.removeMouseMotionListener(this);
				ic.setCustomRoi(false);
				win2.removeWindowListener(this);
				win2.removeFocusListener(this);
				win2.setResizable(true);
				win2.setBackground(defaultWinColor);
			}
		}
		ImagePlus.removeImageListener(this);
		Executer.removeCommandListener(this);
		win.removeWindowListener(this);
		win.removeFocusListener(this);
		win.setResizable(true);
		win.setBackground(defaultWinColor);
		instance = null;
		previousID = imp.getID();
		previousX = crossLoc.x;
		previousY = crossLoc.y;
		if (!(imp instanceof CompositeImage) || imp.getNChannels()<=1 || currentMode != CompositeImage.COMPOSITE){
			ColorModel cm = (imp.getProcessor().getColorModel());
			int min = (int) imp.getProcessor().getMin();
			int max = (int) imp.getProcessor().getMax();
			imp.setStack(originalStack);
			imp.getProcessor().setColorModel(cm);
			imp.getProcessor().setMinAndMax(min, max);
			if (((StackWindow)imp.getWindow()).cSelector != null)
				((StackWindow)imp.getWindow()).cSelector.setEnabled(true);
			if (((StackWindow)imp.getWindow()).tSelector != null)
				((StackWindow)imp.getWindow()).tSelector.setEnabled(true);
			if (((StackWindow)imp.getWindow()).zSelector != null)
				((StackWindow)imp.getWindow()).zSelector.setEnabled(true);
		} else {
			imp.getStack().setColorModel(originalCM);
			imp.setStack(originalStack);
			((CompositeImage)imp).setMode(originalMode);
			if (((StackWindow)imp.getWindow()).cSelector != null)
				((StackWindow)imp.getWindow()).cSelector.setEnabled(true);
			if (((StackWindow)imp.getWindow()).tSelector != null)
				((StackWindow)imp.getWindow()).tSelector.setEnabled(true);
			if (((StackWindow)imp.getWindow()).zSelector != null)
				((StackWindow)imp.getWindow()).zSelector.setEnabled(true);
		}
		imp.setPosition(imp.getChannel(), recentZ, imp.getFrame());
		imp.setPosition(imp.getChannel(), recentZ+1, imp.getFrame());
		imp.setPosition(imp.getChannel(), recentZ, imp.getFrame());
//		IJ.runMacro("waitForUser;");
		imp.updateAndDraw();
		imageStack = null;
	}
	
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		ImageCanvas xyCanvas = xy_image.getCanvas();
		startingSrcRect = (Rectangle)xyCanvas.getSrcRect().clone();
		mouseDragged(e);
	}

	public void mouseDragged(MouseEvent e) {
		if (IJ.spaceBarDown())  // scrolling?
			return;
		if (e.getSource().equals(canvas)) {
			crossLoc = canvas.getCursorLoc();
//			IJ.log("md");
			update();

		} else if (e.getSource().equals(xz_image.getCanvas())) {
//			IJ.log("XZDRAG");
			crossLoc.x = xz_image.getCanvas().getCursorLoc().x;
			int pos = xz_image.getCanvas().getCursorLoc().y;
			int z = (int)Math.round(pos/az);
			int slice = flipXZ?imp.getNSlices()-z:z+1;
			if (imp instanceof CompositeImage && imp.getNChannels()>1 && currentMode < CompositeImage.GRAYSCALE) {
				xy_image.setSlice(slice);
//				imp.zeroUpdateMode = true;
//				imp.setPositionWithoutUpdate(imp.getChannel(), slice, imp.getFrame());
//				imp.zeroUpdateMode = false;
				((StackWindow)xy_image.getWindow()).zSelector.setValue(slice);

			} else if (imp.getStack() instanceof VirtualStack) {
				int min = (int) imp.getProcessor().getMin();
				int max = (int) imp.getProcessor().getMax();
				xy_image.setSlice(slice);
				xy_image.getProcessor().setMinAndMax(min, max);
//				imp.zeroUpdateMode = true;
//				imp.setPositionWithoutUpdate(imp.getChannel(), slice, imp.getFrame());
//				imp.zeroUpdateMode = false;
				((StackWindow)xy_image.getWindow()).zSelector.setValue(slice);

			} else {
				xy_image.setSlice(slice);
				((StackWindow)xy_image.getWindow()).zSelector.setValue(slice);

			}
		} else if (e.getSource().equals(yz_image.getCanvas())) {
//			IJ.log("YZDRAG");
			int pos;
			if (rotateYZ) {
				crossLoc.y = yz_image.getCanvas().getCursorLoc().x;
				pos = yz_image.getCanvas().getCursorLoc().y;
			} else {
				crossLoc.y = yz_image.getCanvas().getCursorLoc().y;
				pos = yz_image.getCanvas().getCursorLoc().x;
			}
			int z = (int)Math.round(pos/az);
			int slice = z+1;
			if (imp instanceof CompositeImage && imp.getNChannels()>1 && currentMode < CompositeImage.GRAYSCALE) {
				xy_image.setSlice(slice);
//				imp.zeroUpdateMode = true;
//				imp.setPositionWithoutUpdate(imp.getChannel(), slice, imp.getFrame());
//				imp.zeroUpdateMode = false;
				((StackWindow)xy_image.getWindow()).zSelector.setValue(slice);

			} else if (imp.getStack() instanceof VirtualStack) {
				int min = (int) imp.getProcessor().getMin();
				int max = (int) imp.getProcessor().getMax();
				xy_image.setSlice(slice);
				xy_image.getProcessor().setMinAndMax(min, max);
//				imp.zeroUpdateMode = true;
//				imp.setPositionWithoutUpdate(imp.getChannel(), slice, imp.getFrame());
//				imp.zeroUpdateMode = false;
				((StackWindow)xy_image.getWindow()).zSelector.setValue(slice);

			} else {
				xy_image.setSlice(slice);
				((StackWindow)xy_image.getWindow()).zSelector.setValue(slice);

			}
		}
		update();
	}

	public void mouseReleased(MouseEvent e) {
		ImageCanvas ic = xy_image.getCanvas();
		Rectangle srcRect = ic.getSrcRect();
		if (srcRect.x!=startingSrcRect.x || srcRect.y!=startingSrcRect.y) {
			// user has scrolled xy image
			int dy = srcRect.y - startingSrcRect.y;
			ImageCanvas yzic = yz_image.getCanvas();
			Rectangle yzSrcRect =yzic.getSrcRect();
			if (rotateYZ) {
				yzSrcRect.x += dy;
				if (yzSrcRect.x<0)
					yzSrcRect.x = 0;
				if (yzSrcRect.x>yz_image.getWidth()-yzSrcRect.width)
					yzSrcRect.y = yz_image.getWidth()-yzSrcRect.width;
			} else {
				yzSrcRect.y += dy;
				if (yzSrcRect.y<0)
					yzSrcRect.y = 0;
				if (yzSrcRect.y>yz_image.getHeight()-yzSrcRect.height)
					yzSrcRect.y = yz_image.getHeight()-yzSrcRect.height;
			}
			yzic.repaint();
			int dx = srcRect.x - startingSrcRect.x;
			ImageCanvas xzic = xz_image.getCanvas();
			Rectangle xzSrcRect =xzic.getSrcRect();
			xzSrcRect.x += dx;
			if (xzSrcRect.x<0)
				xzSrcRect.x = 0;
			if (xzSrcRect.x>xz_image.getWidth()-xzSrcRect.width)
				xzSrcRect.x = xz_image.getWidth()-xzSrcRect.width;
			xzic.repaint();
		}
	}
	
	/**
	 * Refresh the output windows. This is done by sending a signal 
	 * to the Updater() thread. 
	 */
	void update() {
		if (updater!=null)
			updater.doUpdate();
	}
	
	private void exec() {
		if (canvas==null) return;
		int width=imp.getWidth();
		int height=imp.getHeight();
		if (hyperstack) {
			int c = imp.getChannel();
			int t = imp.getFrame();
			if (c!=currentChannel || t!=currentFrame)
				imageStack = null;
			if (imp.isComposite()) {
				int mode = ((CompositeImage)imp).getMode();
				if (mode!=currentMode)
					imageStack = null;
			}
		}
		
		if (imageStack==null)
			imageStack = getStack();
		xy_image.setStack(imageStack);
//		xy_image.setSlice(imp.getSlice());

		double arat=az/ax;
		double brat=az/ay;
		Point p=crossLoc;
		if (p.y>=height) p.y=height-1;
		if (p.x>=width) p.x=width-1;
		if (p.x<0) p.x=0;
		if (p.y<0) p.y=0;
//		IJ.log("UPDATEVIEWS");
		updateViews(p, imageStack);
		Line vLine = null;
		Line hLine = null;
		hLine = new Line(0f, p.y, width, p.y);
		vLine = new Line(p.x, 0f, p.x, height);	
		Overlay overlay = new Overlay();
		vLine.setStrokeColor(Color.cyan);
		hLine.setStrokeColor(Color.magenta);
		overlay.add(vLine);
		overlay.add(hLine);
		
		Roi yzLabel = new TextRoi(p.x+1,1,"YZ", Font.decode("Arial-"+8/canvas.getMagnification()));
		yzLabel.setStrokeColor(Color.cyan);
		Roi xzLabel = new TextRoi(1,p.y,"XZ", Font.decode("Arial-"+8/canvas.getMagnification()));
		xzLabel.setStrokeColor(Color.magenta);
		overlay.add(yzLabel);
		overlay.add(xzLabel);
		xy_image.setOverlay(overlay);
		canvas.setCustomRoi(true);
		updateCrosses(p.x, p.y, arat, brat);
		if (syncZoom) updateMagnification(p.x, p.y);
		arrangeWindows(sticky);
	}

	private void updateCrosses(int x, int y, double arat, double brat) {
		Point p;
		int z=imp.getNSlices();
		int zlice=((StackWindow)xy_image.getWindow()).zSelector.getValue()-1;
		int zcoord=(int)Math.round(arat*zlice);
		if (flipXZ) zcoord = (int)Math.round(arat*(z-zlice));
		
//		ImageCanvas xzCanvas = xz_image.getCanvas();
		p=new Point (x, zcoord);
		Line vLine = null;
		Line hLine = null;
		hLine = new Line(0f, p.y, xz_image.getWidth(), p.y);
		vLine = new Line(p.x, 0f, p.x, xz_image.getHeight());	
		Overlay overlay = new Overlay();
		vLine.setStrokeColor(Color.cyan);
		hLine.setStrokeColor(Color.yellow);
		overlay.add(vLine);
		overlay.add(hLine);
		
		Roi yzLabel = new TextRoi(p.x+1,1,"YZ", Font.decode("Arial-"+8/canvas.getMagnification()));
		yzLabel.setStrokeColor(Color.cyan);
		Roi xyLabel = new TextRoi(1,p.y,"XY", Font.decode("Arial-"+8/canvas.getMagnification()));
		xyLabel.setStrokeColor(Color.yellow);
		overlay.add(yzLabel);
		overlay.add(xyLabel);
		xz_image.setOverlay(overlay);
//		xz_image.setOverlay(path, color, new BasicStroke(1));
		if (rotateYZ) {
			if (flipXZ)
				zcoord=(int)Math.round(brat*(z-zlice));
			else
				zcoord=(int)Math.round(brat*(zlice));
			p=new Point (y, zcoord);
		} else {
			zcoord=(int)Math.round(arat*zlice);
			p=new Point (zcoord, y);
		}
		hLine = new Line(0f, p.y, yz_image.getWidth(), p.y);
		vLine = new Line(p.x, 0f, p.x, yz_image.getHeight());	
		overlay = new Overlay();
		vLine.setStrokeColor(Color.yellow);
		hLine.setStrokeColor(Color.magenta);
		overlay.add(vLine);
		overlay.add(hLine);
		
		xyLabel = new TextRoi(p.x+1,1,"XY", Font.decode("Arial-"+8/canvas.getMagnification()));
		xyLabel.setStrokeColor(Color.yellow);
		Roi xzLabel = new TextRoi(1,p.y,"XZ", Font.decode("Arial-"+8/canvas.getMagnification()));
		xzLabel.setStrokeColor(Color.magenta);
		overlay.add(xyLabel);
		overlay.add(xzLabel);
		yz_image.setOverlay(overlay);
//		yz_image.setOverlay(path, color, new BasicStroke(1));
		IJ.showStatus(xy_image.getLocationAsString(crossLoc.x, crossLoc.y));
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key==KeyEvent.VK_ESCAPE) {
			IJ.beep();
			dispose();
		} else if (IJ.shiftKeyDown()) {
			int width=imp.getWidth(), height=imp.getHeight();
			switch (key) {
				case KeyEvent.VK_LEFT: crossLoc.x--; if (crossLoc.x<0) crossLoc.x=0; break;
				case KeyEvent.VK_RIGHT: crossLoc.x++; if (crossLoc.x>=width) crossLoc.x=width-1; break;
				case KeyEvent.VK_UP: crossLoc.y--; if (crossLoc.y<0) crossLoc.y=0; break;
				case KeyEvent.VK_DOWN: crossLoc.y++; if (crossLoc.y>=height) crossLoc.y=height-1; break;
				default: return;
			}
//			IJ.log("kp");
			update();
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void actionPerformed(ActionEvent ev) {
	}

	public void imageClosed(ImagePlus imp) {
		dispose();
	}

	public void imageOpened(ImagePlus imp) {
	}

	public void imageUpdated(ImagePlus imp) {
		if (imp==this.imp) {
			ImageProcessor ip = imp.getProcessor();
			min = ip.getMin();
			max = ip.getMax();
//			IJ.log("iu");
			update();
		}
	}

	public String commandExecuting(String command) {
		if (command.equals("In")||command.equals("Out")) {
			ImagePlus cimp = WindowManager.getCurrentImage();
			if (cimp==null) return command;
			if (cimp==xy_image) {
				ImageCanvas ic = xy_image.getCanvas();
				if (ic==null) return null;
				int x = ic.screenX(crossLoc.x);
				int y = ic.screenY(crossLoc.y);
				if (command.equals("In")) {
					ic.zoomIn(x, y);
					if (ic.getMagnification()<=1.0) xy_image.repaintWindow();
				} else {
					ic.zoomOut(x, y);
					if (ic.getMagnification()<1.0) xy_image.repaintWindow();
				}
				xyX=crossLoc.x; xyY=crossLoc.y;
//				IJ.log("cx");
				update();
				return null;
			} else if (cimp==xz_image || cimp==yz_image) {
				syncZoom = false;
				return command;
			} else
				return command;
		} else if (command.equals("Flip Vertically")&& xz_image!=null) {
			if (xz_image==WindowManager.getCurrentImage()) {
				flipXZ = !flipXZ;
//				IJ.log("cx");
				update();
				return null;
			} else
				return command;
		} else
			return command;
	}

	public void windowActivated(WindowEvent e) {
		 arrangeWindows(sticky);
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		dispose();		
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
		 arrangeWindows(sticky);
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
//		IJ.log("avc");
		update();
	}
		
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getSource().equals(xz_image.getWindow())) {
			crossLoc.y += e.getWheelRotation();
		} else if (e.getSource().equals(yz_image.getWindow())) {
			crossLoc.x += e.getWheelRotation();
		}
//		IJ.log("mwm");
		update();
	}

	public void focusGained(FocusEvent e) {
		ImageCanvas ic = xy_image.getCanvas();
		if (ic!=null) canvas.requestFocus();
		arrangeWindows(sticky);
	}

	public void focusLost(FocusEvent e) {
		arrangeWindows(sticky);
	}
	
	public static ImagePlus getImage() {
		if (instance!=null)
			return instance.imp;
		else
			return null;
	}
	
	public static synchronized boolean isOrthoViewsImage(ImagePlus imp) {
		if (imp==null || instance==null)
			return false;
		else
			return imp==instance.imp || imp==instance.xz_image || imp==instance.yz_image || imp == instance.xy_image;
	}

	public static Orthogonal_Views getInstance() {
		return instance;
	}

	public int[] getCrossLoc() {
		int[] loc = new int[3];
		loc[0] = crossLoc.x;
		loc[1] = crossLoc.y;
		loc[2] = imp.getSlice()-1;
		return loc;
	}
	
	public void setCrossLoc(int x, int y, int z) {
		crossLoc.setLocation(x, y);
		if (hyperstack)
			imp.setPosition(imp.getChannel(), z+1, imp.getFrame());
		else
			imp.setSlice(z+1);
//		IJ.log("setCrossLoc");
		update();
	}
	
	public ImagePlus getXZImage(){
		return xz_image;
	}
	
	public ImagePlus getYZImage(){
		return yz_image;
	}

	/**
	 * This is a helper class for Othogonal_Views that delegates the
	 * repainting of the destination windows to another thread.
	 * 
	 * @author Albert Cardona
	 */
	private class Updater extends Thread {
		long request = 0;

		// Constructor autostarts thread
		Updater() {
			super("Othogonal Views Updater");
			setPriority(Thread.NORM_PRIORITY);
			start();
		}

		void doUpdate() {
			if (isInterrupted()) return;
			synchronized (this) {
				request++;
//				IJ.log(""+request);
				notify();
			}
		}

		void quit() {
			IJ.wait(10);
			interrupt();
			synchronized (this) {
				notify();
			}
		}

		public void run() {
			exec();
			while (!isInterrupted()) {
				try {
					final long r;
					synchronized (this) {
						r = request;
					}
					// Call update from this thread
					if (r>0){
//						IJ.log(""+r);
//						request--;
						exec();
					}
					synchronized (this) {
						if (r==request) {
							request = 0; // reset
							wait();
						}
						// else loop through to update again
					}
				} catch (Exception e) { }
			}
		}
		
	}  // Updater class

}
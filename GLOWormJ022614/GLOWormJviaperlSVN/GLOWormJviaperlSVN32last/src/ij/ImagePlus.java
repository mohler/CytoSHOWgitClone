package ij;
import java.awt.*;
import java.awt.image.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.vcell.gloworm.ListVirtualStack;
import org.vcell.gloworm.MultiChannelController;
import org.vcell.gloworm.MultiQTVirtualStack;

import client.RemoteMQTVSHandler;
import ij.process.*;
import ij.io.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.filter.Analyzer;
import ij.util.DicomTools;
import ij.util.Tools;
import ij.macro.Interpreter;
import ij.plugin.frame.ColorLegend;
import ij.plugin.frame.ContrastAdjuster;
import ij.plugin.frame.Recorder;
import ij.plugin.frame.RoiManager;
import ij.plugin.Colors;
import ij.plugin.Converter;
import ij.plugin.Duplicator;
import ij.plugin.MultiFileInfoVirtualStack;
import ij.plugin.RectToolOptions;


/**
An ImagePlus contain an ImageProcessor (2D image) or an ImageStack (3D, 4D or 5D image).
It also includes metadata (spatial calibration and possibly the directory/file where
it was read from). The ImageProcessor contains the pixel data (8-bit, 16-bit, float or RGB) 
of the 2D image and some basic methods to manipulate it. An ImageStack is essentually 
a list ImageProcessors of same type and size.
@see ij.process.ImageProcessor
@see ij.ImageStack
@see ij.gui.ImageWindow
@see ij.gui.ImageCanvas
*/
   
public class ImagePlus implements ImageObserver, Measurements, Cloneable {

	/** 8-bit grayscale (unsigned)*/
	public static final int GRAY8 = 0;
	
	/** 16-bit grayscale (unsigned) */
	public static final int GRAY16 = 1;
	
	/** 32-bit floating-point grayscale */
	public static final int GRAY32 = 2;
	
	/** 8-bit indexed color */
	public static final int COLOR_256 = 3;
	
	/** 32-bit RGB color */
	public static final int COLOR_RGB = 4;
	
	/** True if any changes have been made to this image. */
	public boolean changes;
	
	protected Image img;
	protected ImageProcessor ip;
	protected ImageWindow win;
	protected Roi roi;
	protected RoiManager rm;
	protected MultiChannelController mcc;
	protected int currentSlice; // current stack index (one-based)
	protected static final int OPENED=0, CLOSED=1, UPDATED=2;
	protected boolean compositeImage;
	protected int width;
	protected int height;
	protected boolean locked = false;
	protected int nChannels = 1;
	protected int nSlices = 1;
	protected int nFrames = 1;

	private ImageJ ij = IJ.getInstance();
	private String title;
	private	String url;
	private FileInfo fileInfo;
	private int imageType = GRAY8;
	private ImageStack stack;
	private static int currentID = -1;
	private int ID;
	private static Component comp;
	private boolean imageLoaded;
	private int imageUpdateY, imageUpdateW;
	private Properties properties;
	private long startTime;
	private Calibration calibration;
	private static Calibration globalCalibration;
	private boolean activated;
	private boolean ignoreFlush;
	private boolean errorLoadingImage;
	private static ImagePlus clipboard;
	private static Vector listeners = new Vector();
	private boolean openAsHyperStack;
	private int[] position = {1,1,1};
	private boolean noUpdateMode;
	private ImageCanvas flatteningCanvas;
	private Overlay overlay;
	private boolean hideOverlay;
	private static int default16bitDisplayRange;
    private ScheduledThreadPoolExecutor blinkService = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture schfut;


    /** Constructs an uninitialized ImagePlus. */
    public ImagePlus() {
    	ID = --currentID;
		title="null";
    }
    
    /** Constructs an ImagePlus from an Image or BufferedImage. The first 
		argument will be used as the title of the window that displays the image.
		Throws an IllegalStateException if an error occurs while loading the image. */
    public ImagePlus(String title, Image img) {
		this.title = title;
    	ID = --currentID;
		if (img!=null)
			setImage(img);
    }
    
    /** Constructs an ImagePlus from an ImageProcessor. */
    public ImagePlus(String title, ImageProcessor ip) {
 		setProcessor(title, ip);
   		ID = --currentID;
    }
    
	/** Constructs an ImagePlus from a TIFF, BMP, DICOM, FITS,
		PGM, GIF or JPRG specified by a path or from a TIFF, DICOM,
		GIF or JPEG specified by a URL. */
    public ImagePlus(String pathOrURL) {
    	Opener opener = new Opener();
    	ImagePlus imp = null;
    	boolean isURL = pathOrURL.indexOf("://")>0;
    	if (isURL)
    		imp = opener.openURL(pathOrURL);
    	else
    		imp = opener.openImage(pathOrURL);
    	if (imp!=null) {
    		if (imp.getStackSize()>1)
    			setStack(imp.getTitle(), imp.getStack());
    		else
     			setProcessor(imp.getTitle(), imp.getProcessor());
     		setCalibration(imp.getCalibration());
     		properties = imp.getProperties();
     		setFileInfo(imp.getOriginalFileInfo());
     		setDimensions(imp.getNChannels(), imp.getNSlices(), imp.getNFrames());
     		setOverlay(imp.getOverlay());
     		setRoi(imp.getRoi());
   			if (isURL)
   				this.url = pathOrURL;
   			ID = --currentID;
    	}
    }

	/** Constructs an ImagePlus from a stack. */
    public ImagePlus(String title, ImageStack stack) {
    	setStack(title, stack);
    	ID = --currentID;
    }
    
	/** Locks the image so other threads can test to see if it
		is in use. Returns true if the image was successfully locked.
		Beeps, displays a message in the status bar, and returns
		false if the image is already locked. */
	public synchronized boolean lock() {
		if (locked) {
			IJ.beep();
			IJ.showStatus("\"" + title + "\" is locked");
			//if (IJ.macroRunning()) {
			//	IJ.error("Image is locked");
			//	Macro.abort();
			//}
			return false;
        } else {
        	locked = true;
			if (IJ.debugMode) IJ.log(title + ": lock");
			return true;
        }
	}
	
	/** Similar to lock, but doesn't beep and display an error
		message if the attempt to lock the image fails. */
	public synchronized boolean lockSilently() {
		if (locked)
			return false;
        else {
        	locked = true;
			if (IJ.debugMode) IJ.log(title + ": lock silently");
			return true;
        }
	}
	
	/** Unlocks the image. */
	public synchronized void unlock() {
		locked = false;
		if (IJ.debugMode) IJ.log(title + ": unlock");
	}
		
	private void waitForImage(Image img) {
		if (comp==null) {
			comp = IJ.getInstance();
			if (comp==null)
				comp = new Canvas();
		}
		imageLoaded = false;
		if (!comp.prepareImage(img, this)) {
			double progress;
			waitStart = System.currentTimeMillis();
			while (!imageLoaded && !errorLoadingImage) {
				//IJ.showStatus(imageUpdateY+" "+imageUpdateW);
				IJ.wait(30);
				if (imageUpdateW>1) {
					progress = (double)imageUpdateY/imageUpdateW;
					if (!(progress<1.0)) {
						progress = 1.0 - (progress-1.0);
						if (progress<0.0) progress = 0.9;
					}
					showProgress(progress);
				}
			}
			showProgress(1.0);
		}
	}
	
	long waitStart;
	private void showProgress(double percent) {
		if ((System.currentTimeMillis()-waitStart)>500L)
			IJ.showProgress(percent);
	}

	/** Draws the image. If there is an ROI, its
		outline is also displayed.  Does nothing if there
		is no window associated with this image (i.e. show()
		has not been called).*/
	public void draw(){
		if (win!=null)
			win.getCanvas().repaint();
	}
	
	/** Draws image and roi outline using a clip rect. */
	public void draw(int x, int y, int width, int height){
		if (win!=null) {
			ImageCanvas ic = win.getCanvas();
			double mag = ic.getMagnification();
			x = ic.screenX(x);
			y = ic.screenY(y);
			width = (int)(width*mag);
			height = (int)(height*mag);
			ic.repaint(x, y, width, height);
			if (listeners.size()>0 && roi!=null && roi.getPasteMode()!=Roi.NOT_PASTING)
				notifyListeners(UPDATED);
		}
	}
	
	/** Updates this image from the pixel data in its 
		associated ImageProcessor, then displays it. Does
		nothing if there is no window associated with
		this image (i.e. show() has not been called).*/
	public void updateAndDraw() {
		if (ip!=null) {
			if (win!=null) {
				win.getCanvas().setImageUpdated();
				if (listeners.size()>0) notifyListeners(UPDATED);
			}
			draw();
		}
	}
	
	/** Updates this image from the pixel data in its 
		associated ImageProcessor, then displays it.
		The CompositeImage class overrides this method 
		to only update the current channel. */
	public void updateChannelAndDraw() {
		updateAndDraw();
	}

	/** Returns a reference to the current ImageProcessor. The
		CompositeImage class overrides this method so it returns
		the processor associated with the current channel. */
	public ImageProcessor getChannelProcessor() {
		return getProcessor();
	}
		
	/* The CompositeImage class overrides this method  to
		return, as an array, copies of this image's channel LUTs. */
	public LUT[] getLuts() {
		return null;
		//ImageProcessor ip = getProcessor();
		//ColorModel cm = ip.getColorModel();
		//if (cm instanceof IndexColorModel) {
		//	LUT[] luts = new LUT[1];
		//	luts[0] = new LUT((IndexColorModel)cm, ip.getMin(), ip.getMax());
		//	return luts;
		//} else
		//	return null;
	}

	/** Calls draw to draw the image and also repaints the
		image window to force the information displayed above
		the image (dimension, type, size) to be updated. */
	public void repaintWindow() {
		if (win!=null) {
			draw();
			win.repaint();
		}
	}
		
	/** Calls updateAndDraw to update from the pixel data
		and draw the image, and also repaints the image
		window to force the information displayed above
		the image (dimension, type, size) to be updated. */
	public void updateAndRepaintWindow() {
		if (win!=null) {
			updateAndDraw();
			win.repaint();
		}
	}
		
	/** ImageCanvas.paint() calls this method when the
		ImageProcessor has generated new image. */
	public void updateImage() {
		if (ip!=null)
			img = getProcessor().createImage();
	}

	/** Closes the window, if any, that is displaying this image. */
	public void hide() {
		if (win==null) {
			Interpreter.removeBatchModeImage(this);
			return;
		}
		boolean unlocked = lockSilently();
		changes = false;
		win.close();
		win = null;
		if (unlocked) unlock();
	}

	/** Closes this image and sets the ImageProcessor to null. To avoid the
		"Save changes?" dialog, first set the public 'changes' variable to false. */
	public void close() {
		ImageWindow win = getWindow();
		if (win!=null) {
			//if (IJ.isWindows() && IJ.isJava14())
			//	changes = false; // avoid 'save changes?' dialog and potential Java 1.5 deadlocks
			dupImp = null;
			if (dupImp!=null)
				dupImp.flush();
			win.close();
			if (getMultiChannelController() != null) {
				WindowManager.removeWindow(getMultiChannelController());
				getMultiChannelController().dispose();
			}
		} else {
            if (WindowManager.getCurrentImage()==this)
                WindowManager.setTempCurrentImage(null);
			deleteRoi(); //save any ROI so it can be restored later
			Interpreter.removeBatchModeImage(this);
			this.flush();
		}
    }

	/** Opens a window to display this image and clears the status bar. */
	public void show() {
		show("");
	}

	/** Opens a window to display this image and displays
		'statusMessage' in the status bar. */
	public void show(String statusMessage) {
		if (win!=null) return;
		if ((IJ.isMacro() && ij==null) || Interpreter.isBatchMode()) {
			if (isComposite()) ((CompositeImage)this).reset();
			ImagePlus img = WindowManager.getCurrentImage();
			if (img!=null) img.saveRoi();
			WindowManager.setTempCurrentImage(this);
			Interpreter.addBatchModeImage(this);
			return;
		}
		if (Prefs.useInvertingLut && getBitDepth()==8 && ip!=null && !ip.isInvertedLut()&& !ip.isColorLut())
			invertLookupTable();
		img = getImage();
		if ((img!=null) && (width>=0) && (height>=0)) {
			activated = false;
			int stackSize = getStackSize();
			//if (compositeImage) stackSize /= nChannels;
			if (stackSize>1)
				win = new StackWindow(this, true);
			else
				win = new ImageWindow(this);
			if (roi!=null) roi.setImage(this);
			if (overlay!=null && getCanvas()!=null)
				getCanvas().setOverlay(overlay);
			draw();
			IJ.showStatus(statusMessage);
			if (IJ.isMacro()) { // wait for window to be activated
				long start = System.currentTimeMillis();
				while (!activated) {
					IJ.wait(5);
					if ((System.currentTimeMillis()-start)>2000) {
						WindowManager.setTempCurrentImage(this);
						break; // 2 second timeout
					}
				}
			}
			if (imageType==GRAY16 && default16bitDisplayRange!=0) {
				resetDisplayRange();
				updateAndDraw();
			}
			if (stackSize>1) {
				int c = getChannel();
				int z = getSlice();
				int t = getFrame();
				if (c>1 || z>1 || t>1)
					setPosition(c, z, t);
			}
			ColorLegend cl = getRoiManager().getColorLegend();
			if (cl != null) {
				getCanvas().addMouseMotionListener(cl);
				getCanvas().addMouseListener(cl);
			}
			notifyListeners(OPENED);
			ImageCanvas ic = getCanvas();
			win.pack();
			int padH = 1+win.getInsets().left
					+win.getInsets().right
					+(win.optionsPanel.isVisible()?win.optionsPanel.getWidth():0)
					+win.viewButtonPanel.getWidth();
			int padV = win.getInsets().top
					+win.getInsets().bottom
					+(win instanceof StackWindow?
							((StackWindow)win).getNScrollbars()
							*(((StackWindow)win).zSelector!=null?
									((StackWindow)win).zSelector.getHeight():
										((StackWindow)win).tSelector.getHeight())
							:0)
					+win.overheadPanel.getHeight();
			win.setSize(ic.dstWidth+padH, ic.dstHeight+padV);

		}
	}
	
	void invertLookupTable() {
		int nImages = getStackSize();
		ip.invertLut();
		if (nImages==1)
			ip.invert();
		else {
			ImageStack stack2 = getStack();
			for (int i=1; i<=nImages; i++)
				stack2.getProcessor(i).invert();
			stack2.setColorModel(ip.getColorModel());
		}
	}

	/** Called by ImageWindow.windowActivated(). */
	public void setActivated() {
		activated = true;
	}
		
	/** Returns this image as a AWT image. */
	public Image getImage() {
		if (img==null && ip!=null)
			img = getProcessor().createImage();
		return img;
	}
		
	/** Returns this image as a BufferedImage. */
	public BufferedImage getBufferedImage() {
		if (isComposite())
			return (new ColorProcessor(getImage())).getBufferedImage();
		else
			return ip.getBufferedImage();
	}

	/** Returns this image's unique numeric ID. */
	public int getID() {
		return ID;
	}
	
	/** Replaces the image, if any, with the one specified. 
		Throws an IllegalStateException if an error occurs 
		while loading the image. */
	public void setImage(Image img) {
		if (img instanceof BufferedImage) {
			BufferedImage bi = (BufferedImage)img;
			if (bi.getType()==BufferedImage.TYPE_USHORT_GRAY) {
				setProcessor(null, new ShortProcessor(bi));
				return;
			} else if (bi.getType()==BufferedImage.TYPE_BYTE_GRAY) {
				setProcessor(null, new ByteProcessor(bi));
				return;
			}
		}
		roi = null;
		errorLoadingImage = false;
		waitForImage(img);
		if (errorLoadingImage)
			throw new IllegalStateException ("Error loading image");
		this.img = img;
		int newWidth = img.getWidth(ij);
		int newHeight = img.getHeight(ij);
		boolean dimensionsChanged = newWidth!=width || newHeight!=height;
		width = newWidth;
		height = newHeight;
		ip = null;
		stack = null;
		LookUpTable lut = new LookUpTable(img);
		int type;
		if (lut.getMapSize() > 0) {
			if (lut.isGrayscale())
				type = GRAY8;
			else
				type = COLOR_256;
		} else
			type = COLOR_RGB;
		setType(type);
		setupProcessor();
		this.img = ip.createImage();
		if (win!=null) {
			if (dimensionsChanged)
				win = new ImageWindow(this);
			else
				repaintWindow();
		}
	}
	
	/** Replaces this image with the specified ImagePlus. May
		not work as expected if 'imp' is a CompositeImage
		and this image is not. */
	public void setImage(ImagePlus imp) {
		if (imp.getWindow()!=null)
			imp = imp.duplicate();
		ImageStack stack2 = imp.getStack();
		if (imp.isHyperStack())
			setOpenAsHyperStack(true);
		setStack(stack2, imp.getNChannels(), imp.getNSlices(), imp.getNFrames());
	}
	
	/** Replaces the ImageProcessor with the one specified and updates the display. */
	public void setProcessor(ImageProcessor ip) {
		setProcessor(null, ip);
	}

	/** Replaces the ImageProcessor with the one specified and updates the display.
		Set 'title' to null to leave the image title unchanged. */
	public void setProcessor(String title, ImageProcessor ip) {
        if (ip==null || ip.getPixels()==null)
            throw new IllegalArgumentException("ip null or ip.getPixels() null");
        int stackSize = getStackSize();
        if (stackSize>1 && (ip.getWidth()!=width || ip.getHeight()!=height))
            throw new IllegalArgumentException("ip wrong size");
		if (stackSize<=1) {
			stack = null;
			setCurrentSlice(1);
		}
		setProcessor2(title, ip, null);
	}
	
	public void setProcessor2(String title, ImageProcessor ip, ImageStack newStack) {
		if (title!=null) setTitle(title);
		this.ip = ip;
		if (ij!=null) ip.setProgressBar(ij.getProgressBar());
        int stackSize = 1;
		if (stack!=null) {
			stackSize = stack.getSize();
			if (currentSlice>stackSize) setCurrentSlice(stackSize);
		}
		img = null;
		boolean dimensionsChanged = width>0 && height>0 && (width!=ip.getWidth() || height!=ip.getHeight());
		if (dimensionsChanged) roi = null;
		int type;
		if (ip instanceof ByteProcessor)
			type = GRAY8;
		else if (ip instanceof ColorProcessor)
			type = COLOR_RGB;
		else if (ip instanceof ShortProcessor)
			type = GRAY16;
		else
			type = GRAY32;
		if (width==0)
			imageType = type;
		else
			setType(type);
		width = ip.getWidth();
		height = ip.getHeight();
		if (win!=null) {
			if (dimensionsChanged && stackSize==1)
                win.updateImage(this);
			else if (newStack==null)
				repaintWindow();
				draw();
		}
	}

	/** Replaces the image with the specified stack and updates the display. */
	public void setStack(ImageStack stack) {
    	setStack(null, stack);
    }
    
	/** Replaces the image with the specified stack and updates 
		the display. Set 'title' to null to leave the title unchanged. */
    public void setStack(String title, ImageStack newStack) {
		int newStackSize = newStack.getSize();
		//IJ.log("setStack: "+newStackSize+" "+this);
		if (newStackSize==0)
			throw new IllegalArgumentException("Stack is empty");
		if (!newStack.isVirtual()) {
			Object[] arrays = newStack.getImageArray();
			if (arrays==null || (arrays.length>0&&arrays[0]==null))
				throw new IllegalArgumentException("Stack pixel array null");
		}
    	boolean sliderChange = false;
    	if (win!=null && (win instanceof StackWindow)) {
    		int nScrollbars = ((StackWindow)win).getNScrollbars();
    		if (nScrollbars>0 && newStackSize==1)
    			sliderChange = true;
    		else if (nScrollbars==0 && newStackSize>1)
    			sliderChange = true;
    	}
    	if (currentSlice<1) setCurrentSlice(1);
    	boolean resetCurrentSlice = currentSlice>newStackSize;
    	if (resetCurrentSlice) setCurrentSlice(newStackSize);
    	ImageProcessor ip = newStack.getProcessor(currentSlice);
    	boolean dimensionsChanged = width>0 && height>0 && (width!=ip.getWidth()||height!=ip.getHeight());
    	this.stack = newStack;
    	this.stack.addOwnerImp(this);
    	setProcessor2(title, ip, newStack);
		if (win==null) {
			if (resetCurrentSlice) setSlice(currentSlice);
			return;
		}
		boolean invalidDimensions = isDisplayedHyperStack() && !((StackWindow)win).validDimensions();
		if (newStackSize>1 && !(win instanceof StackWindow)) {
			if (isDisplayedHyperStack()) setOpenAsHyperStack(true);
			win = new StackWindow(this, getCanvas(), true);   // replaces this window
			setPosition(1, 1, 1);
			if (Interpreter.getInstance()!=null)
				IJ.wait(25);
		} else if (newStackSize>1 && invalidDimensions) {
			if (isDisplayedHyperStack()) setOpenAsHyperStack(true);
			win = new StackWindow(this, win.isVisible());   // replaces this window
			setPosition(1, 1, 1);
		} else if (dimensionsChanged || sliderChange)
			win.updateImage(this);
		else {
			if (win!=null && win instanceof StackWindow)
				((StackWindow)win).updateSliceSelector();
			if (isComposite()) {
				((CompositeImage)this).reset();
				updateAndDraw();
			}
			repaintWindow();
		}
		if (resetCurrentSlice) setSlice(currentSlice);
    }
    
	public void setStack(ImageStack stack, int nChannels, int nSlices, int nFrames) {
		if (nChannels*nSlices*nFrames!=stack.getSize()) {
			if (nChannels*nSlices*nFrames>stack.getSize()) {
				for (int a=stack.getSize();a<nChannels*nSlices*nFrames;a++) {
					if (stack.isVirtual())
						((VirtualStack)stack).addSlice("blank slice");
					else
						stack.addSlice(this.getProcessor().createProcessor(this.getWidth(), this.getHeight()));
				}
			} else if (nChannels*nSlices*nFrames<stack.getSize()) {
				for (int a=nChannels*nSlices*nFrames;a<stack.getSize();a++) {
					stack.deleteSlice(nChannels*nSlices*nFrames);
				}
			}else {
				throw new IllegalArgumentException("channels*slices*frames!=stackSize");
			}
		}
		int channelsBefore = this.nChannels;
		this.nChannels = nChannels;
		this.nSlices = nSlices;
		this.nFrames = nFrames;
		setStack(null, stack);
		if (channelsBefore!=nChannels && isComposite())
			((CompositeImage)this).setChannelsUpdated();
	}

	/**	Saves this image's FileInfo so it can be later
		retieved using getOriginalFileInfo(). */
	public void setFileInfo(FileInfo fi) {
		if (fi!=null)
			fi.pixels = null;
		fileInfo = fi;
	}
		
	/** Returns the ImageWindow that is being used to display
		this image. Returns null if show() has not be called
		or the ImageWindow has been closed. */
	public ImageWindow getWindow() {
		return win;
	}
	
	/** Returns true if this image is currently being displayed in a window. */
	public boolean isVisible() {
		return win!=null && win.isVisible();
	}

	/** This method should only be called from an ImageWindow. */
	public void setWindow(ImageWindow win) {
		this.win = win;
		if (roi!=null)
			roi.setImage(this);  // update roi's 'ic' field
	}
	
	/** Returns the ImageCanvas being used to
		display this image, or null. */
	public ImageCanvas getCanvas() {
		return win!=null?win.getCanvas():flatteningCanvas;
	}

	/** Sets current foreground color. */
	public void setColor(Color c) {
		if (ip!=null)
			ip.setColor(c);
	}
	
	void setupProcessor() {
		if (imageType==COLOR_RGB) {
			if (ip == null || ip instanceof ByteProcessor)
				ip = new ColorProcessor(getImage());
		} else if (ip==null || (ip instanceof ColorProcessor))
			ip = new ByteProcessor(getImage());
		if (roi!=null && roi.isArea())
			ip.setRoi(roi.getBounds());
		else
			ip.resetRoi();
	}
	
	public boolean isProcessor() {
		return ip!=null;
	}
	
	/** Returns a reference to the current ImageProcessor. If there
	    is no ImageProcessor, it creates one. Returns null if this
	    ImagePlus contains no ImageProcessor and no AWT Image.
		Sets the line width to the current line width and sets the
		calibration table if the image is density calibrated. 
		Checks for and applies ColorLegend settings if is Sketch3D*/
	public ImageProcessor getProcessor() {
		if (ip==null && img==null)
			return null;
		setupProcessor();
		if (!compositeImage)
			ip.setLineWidth(Line.getWidth());
		if (ij!=null)
			ip.setProgressBar(ij.getProgressBar());
		Calibration cal = getCalibration();
		if (cal.calibrated())
			ip.setCalibrationTable(cal.getCTable());
		else
			ip.setCalibrationTable(null);
		if (Recorder.record) {
			Recorder recorder = Recorder.getInstance();
			if (recorder!=null) recorder.imageUpdated(this);
		}
		ImageProcessor pip = ip;
		if (isSketch3D() && rm!= null && rm.getColorLegend()!=null) {
			pip  = rm.getColorLegend().processedIP(pip);
		}

		return pip;
	}
	
	/** Frees RAM by setting the snapshot (undo) buffer in
		the current ImageProcessor to null. */
	public void trimProcessor() {
		ImageProcessor ip2 = ip;
		if (!locked && ip2!=null) {
			if (IJ.debugMode) IJ.log(title + ": trimProcessor");
			Roi roi2 = getRoi();
			if (roi2!=null && roi2.getPasteMode()!=Roi.NOT_PASTING)
				roi2.endPaste();
			ip2.setSnapshotPixels(null);
		}
	}
	
	/** For images with irregular ROIs, returns a byte mask, otherwise, returns
		null. Mask pixels have a non-zero value. */
	public ImageProcessor getMask() {
		if (roi==null) {
			if (ip!=null) ip.resetRoi();
			return null;
		}
		ImageProcessor mask = roi.getMask();
		if (mask==null)
			return null;
		if (ip!=null && roi!=null) {
			ip.setMask(mask);
			ip.setRoi(roi.getBounds());
		}
		return mask;
	}

	/** Returns an ImageStatistics object generated using the standard
		measurement options (area, mean, mode, min and max).
		This plugin demonstrates how get the area, mean and max of the
		current image or selection:
		<pre>
   public class Get_Statistics implements PlugIn {
      public void run(String arg) {
         ImagePlus imp = IJ.getImage();
         ImageStatistics stats = imp.getStatistics();
         IJ.log("Area: "+stats.area);
         IJ.log("Mean: "+stats.mean);
         IJ.log("Max: "+stats.max);
      }
   }
		</pre>
		@see ij.process.ImageStatistics
		@see ij.process.ImageStatistics#getStatistics
		*/
	public ImageStatistics getStatistics() {
		return getStatistics(AREA+MEAN+MODE+MIN_MAX);
	}
	
	/** Returns an ImageStatistics object generated using the
		specified measurement options. This plugin demonstrates how
		get the area and centroid of the current selection:
		<pre>
   public class Get_Statistics implements PlugIn, Measurements {
      public void run(String arg) {
         ImagePlus imp = IJ.getImage();
         ImageStatistics stats = imp.getStatistics(MEDIAN+CENTROID);
         IJ.log("Median: "+stats.median);
         IJ.log("xCentroid: "+stats.xCentroid);
         IJ.log("yCentroid: "+stats.yCentroid);
      }
   }
		</pre>
		@see ij.process.ImageStatistics
		@see ij.measure.Measurements
	*/
	public ImageStatistics getStatistics(int mOptions) {
		return getStatistics(mOptions, 256, 0.0, 0.0);
	}
	
	/** Returns an ImageStatistics object generated using the
		specified measurement options and histogram bin count. 
		Note: except for float images, the number of bins
		is currently fixed at 256.
	*/
	public ImageStatistics getStatistics(int mOptions, int nBins) {
		return getStatistics(mOptions, nBins, 0.0, 0.0);
	}

	/** Returns an ImageStatistics object generated using the
		specified measurement options, histogram bin count and histogram range. 
		Note: for 8-bit and RGB images, the number of bins
		is fixed at 256 and the histogram range is always 0-255.
	*/
	public ImageStatistics getStatistics(int mOptions, int nBins, double histMin, double histMax) {
		setupProcessor();
		if (roi!=null && roi.isArea())
			ip.setRoi(roi);
		else
			ip.resetRoi();
		ip.setHistogramSize(nBins);
		Calibration cal = getCalibration();
		if (getType()==GRAY16&& !(histMin==0.0&&histMax==0.0))
			{histMin=cal.getRawValue(histMin); histMax=cal.getRawValue(histMax);}
		ip.setHistogramRange(histMin, histMax);
		ImageStatistics stats = ImageStatistics.getStatistics(ip, mOptions, cal);
		ip.setHistogramSize(256);
		ip.setHistogramRange(0.0, 0.0);
		return stats;
	}
	
	/** Returns the image name. */
	public String getTitle() {
		if (title==null)
			return "";
		else
    		return title;
    }

	/** Returns a shortened version of image name that does not 
		include spaces or a file name extension. */
	public String getShortTitle() {
		String title = getTitle();
		int index = title.indexOf(' ');
		if (index>-1)
			title = title.substring(0, index);
		index = title.lastIndexOf('.');
		if (index>0)
			title = title.substring(0, index);
		return title;
    }

	/** Sets the image name. */
	public void setTitle(String title) {
		if (title==null)
			return;
    	if (win!=null) {
    		if (ij!=null)
				Menus.updateWindowMenuItem(this.title, title);
			String virtual = stack!=null && stack.isVirtual()?" (V)":"";
			String global = getGlobalCalibration()!=null?" (G)":"";
				
			String scale = "";
			double magnification = win.getCanvas().getMagnification();
			if (magnification!=1.0) {
				double percent = magnification*100.0;
				int digits = percent>100.0||percent==(int)percent?0:1;
				scale = " (" + IJ.d2s(percent,digits) + "%)";
			}
			win.setTitle(title+virtual+global+scale);
    	}
    	this.title = title;
    	if (rm !=null) {
			String oldTitle = rm.getTitle();
    		rm.setTitle("Tag Manager: "+title);
    		if (rm.getColorLegend()!=null) {
    			oldTitle = rm.getColorLegend().getTitle();
    			rm.getColorLegend().setTitle("Color Legend: "+title);
    		}
    	}
    	if (mcc !=null) {
			String oldTitle = mcc.getTitle();
    		mcc.setTitle("Multi-Channel Controller: "+title);
    	}

    }

    public int getWidth() {
    	return width;
    }

    public int getHeight() {
    	return height;
    }
    
	/** If this is a stack, returns the number of slices, else returns 1. */
	public int getStackSize() {
		if (stack==null)
			return 1;
		else {
			int slices = stack.getSize();
			//if (compositeImage) slices /= nChannels;
			if (slices<=0) slices = 1;
			return slices;
		}
	}
	
	/** If this is a stack, returns the actual number of images in the stack, else returns 1. */
	public int getImageStackSize() {
		if (stack==null)
			return 1;
		else {
			int slices = stack.getSize();
			if (slices==0) slices = 1;
			return slices;
		}
	}

	/** Sets the 3rd, 4th and 5th dimensions, where 
	<code>nChannels</code>*<code>nSlices</code>*<code>nFrames</code> 
	must be equal to the stack size. */
	public void setDimensions(int nChannels, int nSlices, int nFrames) {
		//IJ.log("setDimensions: "+nChannels+" "+nSlices+" "+nFrames+" "+getImageStackSize());
		if (nChannels*nSlices*nFrames!=getImageStackSize() && ip!=null) {
//			IJ.error("channels*slices*frames!=stackSize\n" + nChannels+"*"+nSlices+"*"+nFrames+"!="+getImageStackSize());
//			throw new IllegalArgumentException("channels*slices*frames!=stackSize");
//			nChannels = 1;
//			nSlices = getImageStackSize();
//			nFrames = 1;
//			if (isDisplayedHyperStack()) {
//				setOpenAsHyperStack(false);
//				new StackWindow(this);
//				setSlice(1);
//			}
		}
		boolean hyper = isDisplayedHyperStack();
		boolean updateWin = hyper && (this.nChannels!=nChannels||this.nSlices!=nSlices||this.nFrames!=nFrames);
		boolean newSingleImage = win!=null && (win instanceof StackWindow) && nChannels==1&&nSlices==1&&nFrames==1;
		if (newSingleImage) 
			updateWin = true;
		if (this.getStack() instanceof ListVirtualStack && nChannels ==1)
			updateWin = true;
		this.nChannels = nChannels;
		this.nSlices = nSlices;
		this.nFrames = nFrames;
		if (updateWin) {
			if (nSlices!=getImageStackSize())
				setOpenAsHyperStack(true);
			ip=null; img=null;
			setPositionWithoutUpdate(getChannel(), getSlice(), getFrame());
			if (isComposite()) ((CompositeImage)this).reset();
			win = new StackWindow(this, win == null || win.isVisible());
		}
		//IJ.log("setDimensions: "+ nChannels+"  "+nSlices+"  "+nFrames);
	}
	
	/** Returns 'true' if this image is a hyperstack. */
	public boolean isHyperStack() {
		return isDisplayedHyperStack() || (openAsHyperStack&&getNDimensions()>3);
	}
	
	/** Returns the number of dimensions (2, 3, 4 or 5). */
	public int getNDimensions() {
		int dimensions = 2;
		int[] dim = getDimensions(true);
		if (dim[2]>1) dimensions++;
		if (dim[3]>1) dimensions++;
		if (dim[4]>1) dimensions++;
		return dimensions;
	}

	/** Returns 'true' if this is a hyperstack currently being displayed in a StackWindow. */
	public boolean isDisplayedHyperStack() {
		return win!=null && win instanceof StackWindow && ((StackWindow)win).isHyperStack();
	}

	/** Returns the number of channels. */
	public int getNChannels() {
		verifyDimensions();
		return nChannels;
	}

	/** Returns the image depth (number of z-slices). */
	public int getNSlices() {
		//IJ.log("getNSlices: "+ nChannels+"  "+nSlices+"  "+nFrames);
		verifyDimensions();
		return nSlices;
	}

	/** Returns the number of frames (time-points). */
	public int getNFrames() {
		verifyDimensions();
		return nFrames;
	}
	
	/** Returns the dimensions of this image (width, height, nChannels, 
		nSlices, nFrames) as a 5 element int array. */
	public int[] getDimensions() {
		return getDimensions(true);
	}

	public int[] getDimensions(boolean varify) {
		if (varify)
			verifyDimensions();
		int[] d = new int[5];
		d[0] = width;
		d[1] = height;
		d[2] = nChannels;
		d[3] = nSlices;
		d[4] = nFrames;
		return d;
	}

	void verifyDimensions() {
		int stackSize = getImageStackSize();
		if (nSlices==1) {
			if (nChannels>1 && nFrames==1)
				nChannels = stackSize;
			else if (nFrames>1 && nChannels==1)
				nFrames = stackSize;
		}
		if (nChannels*nSlices*nFrames!=stackSize) {
			nSlices = stackSize;
			nChannels = 1;
			nFrames = 1;
		}
	}

	/** Returns the current image type (ImagePlus.GRAY8, ImagePlus.GRAY16,
		ImagePlus.GRAY32, ImagePlus.COLOR_256 or ImagePlus.COLOR_RGB).
		@see #getBitDepth
	*/
    public int getType() {
    	return imageType;
    }

    /** Returns the bit depth, 8, 16, 24 (RGB) or 32. RGB images actually use 32 bits per pixel. */
    public int getBitDepth() {
    	int bitDepth = 0;
    	switch (imageType) {
	    	case GRAY8: case COLOR_256: bitDepth=8; break;
	    	case GRAY16: bitDepth=16; break;
	    	case GRAY32: bitDepth=32; break;
	    	case COLOR_RGB: bitDepth=24; break;
    	}
    	return bitDepth;
    }
    
    /** Returns the number of bytes per pixel. */
    public int getBytesPerPixel() {
    	switch (imageType) {
	    	case GRAY16: return 2;
	    	case GRAY32: case COLOR_RGB: return 4;
	    	default: return 1;
    	}
	}

    protected void setType(int type) {
    	if ((type<0) || (type>COLOR_RGB))
    		return;
    	int previousType = imageType;
    	imageType = type;
		if (imageType!=previousType) {
			if (win!=null)
				Menus.updateMenus();
			getLocalCalibration().setImage(this);
		}
    }
		
 	/** Returns the string value from the "Info" property string  
	 * associated with 'key', or null if the key is not found. 
	 * Works with DICOM tags and Bio-Formats metadata.
	 * @see #getNumericProperty
	 * @see #getInfoProperty
	*/
	public String getStringProperty(String key) {
		if (key==null)
			return null;
		if (isDicomTag(key))
			return DicomTools.getTag(this, key);
		if (getStackSize()>1) {
			ImageStack stack = getStack();
			String label = stack.getSliceLabel(getCurrentSlice());
			if (label!=null && label.indexOf('\n')>0) {
				String value = getStringProperty(key, label);
				if (value!=null)
					return value;
			}
		}
		Object obj = getProperty("Info");
		if (obj==null || !(obj instanceof String))
			return null;
		String info = (String)obj;
		return getStringProperty(key, info);
	}
	
	private boolean isDicomTag(String key) {
		if (key.length()!=9 || key.charAt(4)!=',')
			return false;
		key = key.toLowerCase();
		for (int i=0; i<9; i++) {
			char c = i!=4?key.charAt(i):'0';
			if (!(Character.isDigit(c)||(c=='a'||c=='b'||c=='c'||c=='d'||c=='e'||c=='f')))
				return false;
		}
		return true;
	}
	
	/** Returns the numeric value from the "Info" property string  
	 * associated with 'key', or NaN if the key is not found or the
	 * value associated with the key is not numeric. Works with
	 * DICOM tags and Bio-Formats metadata.
	 * @see #getStringProperty
	 * @see #getInfoProperty
	*/
	public double getNumericProperty(String key) {
		return Tools.parseDouble(getStringProperty(key));
	}

	/**
	 * @deprecated
	 * @see #getStringProperty
	*/
	public String getProp(String key) {
		return getStringProperty(key);
	}
	
	private String getStringProperty(String key, String info) {
		int index1 = -1;
		index1 = findKey(info, key+": "); // standard 'key: value' pair?
		if (index1<0) // Bio-Formats metadata?
			index1 = findKey(info, key+" = ");
		if (index1<0) // otherwise not found
			return null;
		if (index1==info.length())
			return ""; //empty value at the end
		int index2 = info.indexOf("\n", index1);
		if (index2==-1)
			index2=info.length();
		String value = info.substring(index1, index2);
		return value;
	}
	
	/** Find a key in a String (words merely ending with 'key' don't qualify).
	* @return index of first character after the key, or -1 if not found
	*/
	private int findKey(String s, String key) {
		int i = s.indexOf(key);
		if (i<0)
			return -1; //key not found
		while (i>0 && Character.isLetterOrDigit(s.charAt(i-1)))
			i = s.indexOf(key, i+key.length());
		if (i>=0)
			return i + key.length();
		else
			return -1;
	}
		
	/** Returns the "Info" property string, or null if it is not found. */
	public String getInfoProperty() {
		String info = null;
		Object obj = getProperty("Info");
		if (obj!=null && (obj instanceof String)) {
			info = (String)obj;
			if (info.length()==0)
				info = null;
		}
		return info;
	}

	/** Returns the property associated with 'key', or null if it is not found.
	 * @see #getStringProperty
	 * @see #getNumericProperty
	 * @see #getInfoProperty
	*/
	public Object getProperty(String key) {
		if (properties==null)
			return null;
		else
			return properties.get(key);
	}
	
	/** Adds a key-value pair to this image's properties. The key
		is removed from the properties table if value is null. */
	public void setProperty(String key, Object value) {
		if (properties==null)
			properties = new Properties();
		if (value==null)
			properties.remove(key);
		else
			properties.put(key, value);
	}
		
	/** Returns this image's Properties. May return null. */
	public Properties getProperties() {
			return properties;
	}
		
	/** Creates a LookUpTable object that corresponds to this image. */
    public LookUpTable createLut() {
		ImageProcessor ip2 = getProcessor();
		if (ip2!=null)
			return new LookUpTable(ip2.getColorModel());
		else
			return new LookUpTable(LookUpTable.createGrayscaleColorModel(false));
	}
    
	/** Returns true is this image uses an inverting LUT that 
		displays zero as white and 255 as black. */
	public boolean isInvertedLut() {
		if (ip==null) {
			if (img==null)
				return false;
			setupProcessor();
		}
		return ip.isInvertedLut();
	}
    
	private int[] pvalue = new int[4];

	private Color roiFillColor;

	private float roiStrokeWidth;

	private Color roiStrokeColor;

	protected boolean blinkOn;

	public boolean zeroUpdateMode;

	/**
	Returns the pixel value at (x,y) as a 4 element array. Grayscale values
	are retuned in the first element. RGB values are returned in the first
	3 elements. For indexed color images, the RGB values are returned in the
	first 3 three elements and the index (0-255) is returned in the last.
	*/
	public int[] getPixel(int x, int y) {
		pvalue[0]=pvalue[1]=pvalue[2]=pvalue[3]=0;
		switch (imageType) {
			case GRAY8: case COLOR_256:
				int index;
				if (ip!=null)
					index = ip.getPixel(x, y);
				else {
					byte[] pixels8;
					if (img==null) return pvalue;
					PixelGrabber pg = new PixelGrabber(img,x,y,1,1,false);
					try {pg.grabPixels();}
					catch (InterruptedException e){return pvalue;};
					pixels8 = (byte[])(pg.getPixels());
					index = pixels8!=null?pixels8[0]&0xff:0;
				}
				if (imageType!=COLOR_256) {
					pvalue[0] = index;
					return pvalue;
				}
				pvalue[3] = index;
				// fall through to get rgb values
			case COLOR_RGB:
				int c = 0;
				if (imageType==COLOR_RGB && ip!=null)
					c = ip.getPixel(x, y);
				else {
					int[] pixels32 = new int[1];
					if (img==null) return pvalue;
					PixelGrabber pg = new PixelGrabber(img, x, y, 1, 1, pixels32, 0, width);
					try {pg.grabPixels();}
					catch (InterruptedException e) {return pvalue;};
					c = pixels32[0];
				}
				int r = (c&0xff0000)>>16;
				int g = (c&0xff00)>>8;
				int b = c&0xff;
				pvalue[0] = r;
				pvalue[1] = g;
				pvalue[2] = b;
				break;
			case GRAY16: case GRAY32:
				if (ip!=null) pvalue[0] = ip.getPixel(x, y);
				break;
		}
		return pvalue;
	}
    
	/** Returns an empty image stack that has the same
		width, height and color table as this image. */
	public ImageStack createEmptyStack() {
		ColorModel cm;
		if (ip!=null)
			cm = ip.getColorModel();
		else
			cm = createLut().getColorModel();
		return new ImageStack(width, height, cm);
	}
	
	/** Returns the image stack. The stack may have only 
		one slice. After adding or removing slices, call  
		<code>setStack()</code> to update the image and
		the window that is displaying it.
		@see #setStack
	*/
	public ImageStack getStack() {
		ImageStack s;
		if (stack==null) {
			s = createEmptyStack();
			ImageProcessor ip2 = getProcessor();
			if (ip2==null)
				return s;
            String info = (String)getProperty("Info");
            String label = info!=null?getTitle()+"\n"+info:null;
			s.addSlice(label, ip2);
			s.update(ip2);
		} else {
			s = stack;
			if (ip!=null) {
				Calibration cal = getCalibration();
				if (cal.calibrated())
					ip.setCalibrationTable(cal.getCTable());
				else
					ip.setCalibrationTable(null);
			}
			s.update(ip);
		}
		if (roi!=null)
			s.setRoi(roi.getBounds());
		else
			s.setRoi(null);
		return s;
	}
	
	/** Returns the base image stack. */ 
	public ImageStack getImageStack() {
		if (stack==null)
			return getStack();
		else {
			stack.update(ip);
			return stack;
		}
	}

	/** Returns the current stack index (one-based) or 1 if
		this is a single image. */
	public int getCurrentSlice() {
		if (currentSlice<1) setCurrentSlice(1);
		if (currentSlice>getStackSize()) setCurrentSlice(getStackSize());
		return currentSlice;
	}
	
	final void setCurrentSlice(int slice) {
		currentSlice = slice;
		int stackSize = getStackSize();
		if (nChannels==stackSize) updatePosition(currentSlice, 1, 1);
		if (nSlices==stackSize) updatePosition(1, currentSlice, 1);
		if (nFrames==stackSize) updatePosition(1, 1, currentSlice);
	}

	public int getChannel() {
		return position[0];
	}
	
	public int getSlice() {
		return position[1];
	}

	public int getFrame() {
		return position[2];
	}

	public void killStack() {
		stack = null;
		trimProcessor();
	}
	
	/** Sets the current hyperstack position and updates the display,
		where 'channel', 'slice' and 'frame' are one-based indexes. */
	public void setPosition(int channel, int slice, int frame) {
		//IJ.log("setPosition: "+channel+"  "+slice+"  "+frame+"  "+noUpdateMode);
		verifyDimensions();
   		if (channel<1) channel = 1;
    	if (channel>nChannels) channel = nChannels;
    	if (slice<1) slice = 1;
    	if (slice>nSlices) slice = nSlices;
    	if (frame<1) frame = 1;
    	if (frame>nFrames) frame = nFrames;
		if (isDisplayedHyperStack())
			((StackWindow)win).setPosition(channel, slice, frame);
		else {
			setSlice((frame-1)*nChannels*nSlices + (slice-1)*nChannels + channel);
			updatePosition(channel, slice, frame);
		}
	}
	
	/** Sets the current hyperstack position without updating the display,
		where 'channel', 'slice' and 'frame' are one-based indexes. */
	public void setPositionWithoutUpdate(int channel, int slice, int frame) {
		noUpdateMode = true;
		setPosition(channel, slice, frame);
		noUpdateMode = false;
	}
	
	/** Sets the hyperstack channel position (one based). */
	public void setC(int channel) {
		setPosition(channel, getZ(), getT());
	}
	
	/** Sets the hyperstack slice position (one based). */
	public void setZ(int slice) {
		setPosition(getC(), slice, getT());
	}

	/** Sets the hyperstack frame position (one based). */
	public void setT(int frame) {
		setPosition(getC(), getZ(), frame);
	}

	/** Returns the current hyperstack channel position. */
	public int getC() {
		return position[0];
	}
	
	/** Returns the current hyperstack slice position. */
	public int getZ() {
		return position[1];
	}

	/** Returns the current hyperstack frame position. */
	public int getT() {
		return position[2];
	}
	
	/** Returns that stack index (one-based) corresponding to the specified position. */
	public int getStackIndex(int channel, int slice, int frame) {	
   		if (channel<1) channel = 1;
    	if (channel>nChannels) channel = nChannels;
    	if (slice<1) slice = 1;
    	if (slice>nSlices) slice = nSlices;
    	if (frame<1) frame = 1;
    	if (frame>nFrames) frame = nFrames;
		if (getOriginalFileInfo() != null && getOriginalFileInfo().fileName.toLowerCase().endsWith("_csv.ome.tif")) {
//			IJ.log("ome");
			return (frame-1)*nChannels*nSlices + (channel-1)*nSlices + slice;
		} else if (getOriginalFileInfo() != null && getOriginalFileInfo().fileName.toLowerCase().endsWith(".ome.tif")) {
			return (frame-1)*nChannels*nSlices + (slice-1)*nChannels + channel;
		} else if (getStack() instanceof MultiFileInfoVirtualStack && ((MultiFileInfoVirtualStack)getStack()).getDimOrder() == "xyztc") {
			return (channel-1)*nSlices*nFrames + (frame-1)*nSlices + slice;
		}
//		IJ.log("not ome");
		return (frame-1)*nChannels*nSlices + (slice-1)*nChannels + channel;
	}
	
	/* Hack needed to make the HyperStackReducer work. */
	public void resetStack() {
		if (currentSlice==1 && stack!=null && stack.getSize()>0) {
			ColorModel cm = ip.getColorModel();
			double min = ip.getMin();
			double max = ip.getMax();
			ip = stack.getProcessor(1);
			ip.setColorModel(cm);
			ip.setMinAndMax(min, max);
		}
	}
	
	/** Set the current hyperstack position based on the stack index 'n' (one-based). */
	public void setPosition(int n) {
		int[] pos = convertIndexToPosition(n);
		setPosition(pos[0], pos[1], pos[2]);
	}
			
	/** Converts the stack index 'n' (one-based) into a hyperstack position (channel, slice, frame). */
	public int[] convertIndexToPosition(int n) {
		if (n<1 || n>getStackSize())
			throw new IllegalArgumentException("n out of range: "+n);
		int[] position = new int[3];
		int[] dim = getDimensions();
		position[0] = ((n-1)%dim[2])+1;
		position[1] = (((n-1)/dim[2])%dim[3])+1;
		position[2] = (((n-1)/(dim[2]*dim[3]))%dim[4])+1;
		return position;
	}

	/** Displays the specified stack image, where 1<=n<=stackSize.
	 * Does nothing if this image is not a stack.
	 * @see #setPosition
	 * @see #setC
	 * @see #setZ
	 * @see #setT
	 */
	public synchronized void setSlice(int n) {
		if (stack==null /*|| (n==currentSlice&&ip!=null)*/) {
			if (!noUpdateMode)
				updateAndRepaintWindow();
			return;
		}
		if (zeroUpdateMode) {
			img = null;
			return;
		}

		if (n>=1 && n<=stack.getSize()) {
			Roi roi = getRoi();
			if (roi!=null)
				roi.endPaste();
			if (isProcessor())
				stack.setPixels(ip.getPixels(),currentSlice);
//			ip = getProcessor();
			setCurrentSlice(n);
			if (win!=null && win instanceof StackWindow)
				((StackWindow)win).updateSliceSelector();
			Object pixels = stack.getPixels(currentSlice);
			if ((ip!=null && pixels!=null) 
					&& (!isComposite() 
							|| (isComposite() 
									&& ((CompositeImage)this).getCompositeMode() < CompositeImage.RATIO12))) {
				ip.setSnapshotPixels(null);
				ip.setPixels(pixels);
				ip.snapshot();
			} else
				ip = stack.getProcessor(n);

			if (stack.isVirtual()) {
				Overlay overlay2 = ip.getOverlay();
				if (overlay2!=null)
					setOverlay(overlay2);
			}
			if (imageType==COLOR_RGB)
//				ContrastAdjuster.update();
				;
			if (!noUpdateMode)
				updateAndRepaintWindow();
			else
				img = null;
		}
	}

	/** Displays the specified stack image (1<=n<=stackSize)
		without updating the display. */
	public void setSliceWithoutUpdate(int n) {
		noUpdateMode = true;
		setSlice(n);
		noUpdateMode = false;
	}

	/** Returns the current selection, or null if there is no selection. */
	public Roi getRoi() {
		return roi;
//		if (roi!=null) {
//			Roi resetRoi = (Roi)roi.clone();
//			resetRoi.setFillColor(roiFillColor);
//			return resetRoi;
//		}else
//			return null;
	}
	
	/** Assigns the specified ROI to this image and displays it. Any existing
		ROI is deleted if <code>roi</code> is null or its width or height is zero. */
	public void setRoi(Roi newRoi) {
		setRoi(newRoi, true);
	}

	/** Assigns 'newRoi'  to this image and displays it if 'updateDisplay' is true. */
	public void setRoi(Roi newRoi, boolean updateDisplay) {
		if (newRoi==null)
			{deleteRoi(); return;}
		Rectangle bounds = newRoi.getBounds();
		if (newRoi.isVisible()) {
			if ((newRoi instanceof Arrow) && newRoi.getState()==Roi.CONSTRUCTING && bounds.width==0 && bounds.height==0) {
				deleteRoi();
				roi = newRoi;
				roiFillColor = newRoi.getFillColor();
				return;
			}
			newRoi = (Roi)newRoi.clone();
			if (newRoi==null) {
				deleteRoi(); 
				return;
			}
		}
		if (bounds.width==0 && bounds.height==0 && !(newRoi.getType()==Roi.POINT||newRoi.getType()==Roi.LINE))
			{deleteRoi(); return;}
		roi = newRoi;
		roiFillColor = roi.getFillColor();
		roiStrokeWidth = roi.getStrokeWidth();
		roiStrokeColor = roi.getStrokeColor();
		if (roiStrokeColor ==null)
			roiStrokeColor = Color.yellow;
		
		if (ip!=null) {
			ip.setMask(null);
			if (roi.isArea())
				ip.setRoi(bounds);
			else
				ip.resetRoi();
		}
		if (this.isDisplayedHyperStack()){
			blinkOn=true;
			final Roi blinkRoi = (Roi)roi.clone();
			final Roi origRoi = (Roi)roi.clone();
		
			if (roi instanceof Arrow)
				blinkRoi.setStrokeColor(roiStrokeColor.brighter());
			
			blinkRoi.setFillColor(Roi.getDefaultFillColor());
			
			if (roi instanceof TextRoi)
				blinkRoi.setFillColor(Color.yellow);

			if (schfut != null)
				schfut.cancel(true);
			schfut = blinkService.scheduleAtFixedRate(new Runnable()
			{
				public void run()
				{
//					double strokeWidthMagAdjust = roiStrokeWidth/win.getCanvas().getMagnification();
//					Roi dummyRoi = new Roi(0,0,0,0);
//					dummyRoi.setStrokeWidth(strokeWidthMagAdjust);
//					strokeWidthMagAdjust = dummyRoi.getStrokeWidth();

					if (roi instanceof Line) 
						roi.setStrokeWidth(roiStrokeWidth);
					else
						roi.setStrokeWidth(roiStrokeWidth);
					
					if (blinkOn){
						killRoi();
						roi = blinkRoi;
						roi.setImage(ImagePlus.this);

						blinkOn = false;
					} else {
						killRoi();
						roi = origRoi;
						roi.setImage(ImagePlus.this);

						blinkOn =true;
					}
					draw();
				}
			}, 0, 500, TimeUnit.MILLISECONDS);
		}
//		if (roi!=null)
//			roi.setImage(this);
		if (updateDisplay) draw();
	}
	
	/** Creates a rectangular selection. */
	public void setRoi(int x, int y, int width, int height) {
		setRoi(new Rectangle(x, y, width, height));
	}

	/** Creates a rectangular selection. */
	public void setRoi(Rectangle r) {
		setRoi(new Roi(r.x, r.y, r.width, r.height));
	}
	
	/** Starts the process of creating a new selection, where sx and sy are the
		starting screen coordinates. The selection type is determined by which tool in
		the tool bar is active. The user interactively sets the selection size and shape. */
	public void createNewRoi(int sx, int sy) {
		deleteRoi();
		switch (Toolbar.getToolId()) {
			case Toolbar.RECTANGLE:
				int cornerDiameter = Toolbar.getRoundRectArcSize();
				roi = new Roi(sx, sy, this, cornerDiameter);
				//if (cornerDiameter>0) {
				//	roi.setStrokeColor(Toolbar.getForegroundColor());
				//	roi.setStrokeWidth(RectToolOptions.getDefaultStrokeWidth());
				//}
				break;
			case Toolbar.OVAL:
				if (Toolbar.getOvalToolType()==Toolbar.ELLIPSE_ROI)
					roi = new EllipseRoi(sx, sy, this);
				else
					roi = new OvalRoi(sx, sy, this);
				break;
			case Toolbar.POLYGON:
			case Toolbar.POLYLINE:
			case Toolbar.ANGLE:
				roi = new PolygonRoi(sx, sy, this);
				break;
			case Toolbar.FREEROI:
			case Toolbar.FREELINE:
				roi = new FreehandRoi(sx, sy, this);
				break;
			case Toolbar.LINE:
				if ("arrow".equals(Toolbar.getToolName()))
					roi = new Arrow(sx, sy, this);
				else
					roi = new Line(sx, sy, this);
				break;
			case Toolbar.TEXT:
				roi = new TextRoi(sx, sy, this);
				break;
			case Toolbar.POINT:
				roi = new PointRoi(sx, sy, this);
				if (Prefs.pointAutoMeasure || (Prefs.pointAutoNextSlice&&!Prefs.pointAddToManager)) IJ.run("Measure");
				if (Prefs.pointAddToManager) {
					IJ.run("Add to Manager ");
					ImageCanvas ic = getCanvas();
					if (ic!=null && ic.getShowAllList()==null)
						ic.setShowAllROIs(true);
				}
				if (Prefs.pointAutoNextSlice && getStackSize()>1) {
					IJ.run("Next Slice [>]");
					deleteRoi();
				}
				break;
		}
	}

	/** Deletes the current region of interest. Makes a copy of the ROI
		so it can be recovered by Edit/Selection/Restore Selection. */
	public void deleteRoi() {
		if (roi!=null) {
			saveRoi();
			roi = null;
			if (ip!=null)
				ip.resetRoi();
			draw();
		}
	}
	
	/** Deletes the current region of interest. */
	public void killRoi() {
		deleteRoi();
	}

	public void saveRoi() {
		if (roi!=null) {
			roi.endPaste();
			Rectangle r = roi.getBounds();
			if ((r.width>0 || r.height>0)) {
				Roi.previousRoi = (Roi)roi.clone();
				if (IJ.debugMode) IJ.log("saveRoi: "+roi);
			}
		}
	}
    
	public void restoreRoi() {
		if (Roi.previousRoi!=null) {
			Roi pRoi = Roi.previousRoi;
			Rectangle r = pRoi.getBounds();
			if (r.width<=width || r.height<=height || isSmaller(pRoi)) { // will it (mostly) fit in this image?
				roi = (Roi)pRoi.clone();
				roi.setImage(this);
				if (r.x>=width || r.y>=height || (r.x+r.width)<=0 || (r.y+r.height)<=0) // does it need to be moved?
					roi.setLocation((width-r.width)/2, (height-r.height)/2);
				else if (r.width==width && r.height==height) // is it the same size as the image
					roi.setLocation(0, 0);
				draw();
			}
		}
	}
	
	boolean isSmaller(Roi r) {
		ImageProcessor mask = r.getMask();
		if (mask==null) return false;
		mask.setThreshold(255, 255, ImageProcessor.NO_LUT_UPDATE);
		ImageStatistics stats = ImageStatistics.getStatistics(mask, MEAN+LIMIT, null);
		return stats.area<=width*height;
	}
	
	/** Implements the File/Revert command. */
	public void revert() {
		if (getStackSize()>1 && getStack().isVirtual())
			return;
		FileInfo fi = getOriginalFileInfo();
		boolean isFileInfo = fi!=null && fi.fileFormat!=FileInfo.UNKNOWN;
		if (!(isFileInfo || url!=null))
			return;
		if (ij!=null && changes && isFileInfo && !Interpreter.isBatchMode() && !IJ.isMacro() && !IJ.altKeyDown()) {
			if (!IJ.showMessageWithCancel("Revert?", "Revert to saved version of\n\""+getTitle()+"\"?"))
				return;
		}
		Roi saveRoi = null;
		if (roi!=null) {
			roi.endPaste();
			saveRoi = (Roi)roi.clone();
		}
		
		if (getStackSize()>1) {
			revertStack(fi);
			return;
		}

		trimProcessor();
		if (isFileInfo && !(url!=null&&(fi.directory==null||fi.directory.equals(""))))
			new FileOpener(fi).revertToSaved(this);
		else if (url!=null) {
			IJ.showStatus("Loading: " + url);
	    	Opener opener = new Opener();
	    	try {
	    		ImagePlus imp = opener.openURL(url);
	    		if (imp!=null)
	     			setProcessor(null, imp.getProcessor());
	    	} catch (Exception e) {} 
			if (getType()==COLOR_RGB && getTitle().endsWith(".jpg"))
				Opener.convertGrayJpegTo8Bits(this);
		}
		if (Prefs.useInvertingLut && getBitDepth()==8 && ip!=null && !ip.isInvertedLut()&& !ip.isColorLut())
			invertLookupTable();
		if (getProperty("FHT")!=null) {
			properties.remove("FHT");
			if (getTitle().startsWith("FFT of "))
				setTitle(getTitle().substring(7));
		}
		ContrastAdjuster.update();
		if (saveRoi!=null) setRoi(saveRoi);
		repaintWindow();
		IJ.showStatus("");
		changes = false;
		notifyListeners(UPDATED);
    }
    
	void revertStack(FileInfo fi) {
		String path = null;
		String url2 = null;
		if (url!=null && !url.equals("")) {
			path = url;
			url2 = url;
		} else if (fi!=null && !((fi.directory==null||fi.directory.equals("")))) {
			path = fi.directory+fi.fileName;
		} else if (fi!=null && fi.url!=null && !fi.url.equals("")) {
			path = fi.url;
			url2 = fi.url;
		} else
			return;
		//IJ.log("revert: "+path+"  "+fi);
		IJ.showStatus("Loading: " + path);
		ImagePlus imp = IJ.openImage(path);
		if (imp!=null) {
			int n = imp.getStackSize();
			int c = imp.getNChannels();
			int z = imp.getNSlices();
			int t = imp.getNFrames();
			if (z==n || t==n || (c==getNChannels()&&z==getNSlices()&&t==getNFrames())) {
				setCalibration(imp.getCalibration());
				setStack(imp.getStack(), c, z, t);
			} else {
				ImageWindow win = getWindow();
				Point loc = null;
				if (win!=null) loc = win.getLocation();
				changes = false;
				close();
				FileInfo fi2 = imp.getOriginalFileInfo();
				if (fi2!=null && (fi2.url==null || fi2.url.length()==0)) {
					fi2.url = url2;
					imp.setFileInfo(fi2);
				}
				ImageWindow.setNextLocation(loc);
				imp.show();
			}
		}
	}

    /** Returns a FileInfo object containing information, including the
		pixel array, needed to save this image. Use getOriginalFileInfo()
		to get a copy of the FileInfo object used to open the image.
		@see ij.io.FileInfo
		@see #getOriginalFileInfo
		@see #setFileInfo
	*/
    public FileInfo getFileInfo() {
    	FileInfo fi = new FileInfo();
    	fi.width = width;
    	fi.height = height;
    	fi.nImages = getStackSize();
    	if (compositeImage)
    		fi.nImages = getImageStackSize();
    	fi.whiteIsZero = isInvertedLut();
		fi.intelByteOrder = false;
    	setupProcessor();
    	if (fi.nImages==1)
    		fi.pixels = ip.getPixels();
    	else
			fi.pixels = stack.getImageArray();
		Calibration cal = getCalibration();
    	if (cal.scaled()) {
    		fi.pixelWidth = cal.pixelWidth;
    		fi.pixelHeight = cal.pixelHeight;
   			fi.unit = cal.getUnit();
    	}
    	if (fi.nImages>1)
     		fi.pixelDepth = cal.pixelDepth;
   		fi.frameInterval = cal.frameInterval;
    	if (cal.calibrated()) {
    		fi.calibrationFunction = cal.getFunction();
     		fi.coefficients = cal.getCoefficients();
    		fi.valueUnit = cal.getValueUnit();
		}
    	switch (imageType) {
	    	case GRAY8: case COLOR_256:
    			LookUpTable lut = createLut();
    			if (imageType==COLOR_256 || !lut.isGrayscale())
    				fi.fileType = FileInfo.COLOR8;
    			else
    				fi.fileType = FileInfo.GRAY8;
				fi.lutSize = lut.getMapSize();
				fi.reds = lut.getReds();
				fi.greens = lut.getGreens();
				fi.blues = lut.getBlues();
				break;
	    	case GRAY16:
	    		if (compositeImage && fi.nImages==3) {
	    			if ("Red".equals(getStack().getSliceLabel(1)))
						fi.fileType = fi.RGB48;
					else
						fi.fileType = fi.GRAY16_UNSIGNED;
				} else
					fi.fileType = fi.GRAY16_UNSIGNED;
				break;
	    	case GRAY32:
				fi.fileType = fi.GRAY32_FLOAT;
				break;
	    	case COLOR_RGB:
				fi.fileType = fi.RGB;
				break;
			default:
    	}
    	return fi;
    }
        
    /** Returns the FileInfo object that was used to open this image.
    	Returns null for images created using the File/New command.
		@see ij.io.FileInfo
		@see #getFileInfo
	*/
    public FileInfo getOriginalFileInfo() {
    	if (fileInfo==null & url!=null) {
    		fileInfo = new FileInfo();
    		fileInfo.width = width;
    		fileInfo.height = height;
    		fileInfo.url = url;
    		fileInfo.directory = null;
    	}
    	return fileInfo;
    }

    /** Used by ImagePlus to monitor loading of images. */
    public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
    	imageUpdateY = y;
    	imageUpdateW = w;
		if ((flags & ERROR) != 0) {
			errorLoadingImage = true;
			return false;
		}
    	imageLoaded = (flags & (ALLBITS|FRAMEBITS|ABORT)) != 0;
		return !imageLoaded;
    }

	/** Sets the ImageProcessor, Roi, AWT Image and stack image
		arrays to null. Does nothing if the image is locked. */
	public synchronized void flush() {
		notifyListeners(CLOSED);
		if (locked || ignoreFlush) return;
		ip = null;
		if (roi!=null) roi.setImage(null);
		roi = null;
		boolean preserveStack = false;		
		if (stack!=null) {
			preserveStack = stack.removeOwnerImp(this);
			if (!preserveStack) {
				Object[] arrays = stack.getImageArray();
				if (arrays!=null) {
					for (int i=0; i<arrays.length; i++)
						arrays[i] = null;
				}
				if (isComposite())
					((CompositeImage)this).setChannelsUpdated(); //flush
				stack = null;
			}
		}
		img = null;
		win = null;
		if (roi!=null) {
			roi.setImage(null);
			roi = null;
		}
		Roi.previousRoi = null;
		properties = null;
		calibration = null;
		overlay = null;
		flatteningCanvas = null;
		if (getCanvas() != null)
			this.getWindow().remove(getCanvas());
		if (dupImp != null) {
			dupImp.flush();
			dupImp = null;
		}
		if (motherImp != null) {
			motherImp = null;
		}
		getRoiManager().dispose();
		if (getMultiChannelController() != null)
			getMultiChannelController().dispose();
		if (getRoiManager() != null) {
			getRoiManager().dispose();
			rm = null;
		}

	}
	
	public void setIgnoreFlush(boolean ignoreFlush) {
		this.ignoreFlush = ignoreFlush;
	}
	

	/** Returns a copy (clone) of this ImagePlus. */
	public ImagePlus duplicate() {
		return (new Duplicator()).run(this);
	}

	/** Returns a new ImagePlus with this image's attributes
		(e.g. spatial scale), but no image. */
	public ImagePlus createImagePlus() {
		ImagePlus imp2 = new ImagePlus();
		imp2.setType(getType());
		imp2.setCalibration(getCalibration());
		String info = (String)getProperty("Info");
		if (info!=null)
			imp2.setProperty("Info", info);
		FileInfo fi = getOriginalFileInfo();
		if (fi!=null) {
			fi = (FileInfo)fi.clone();
			fi.directory = null;
			fi.url = null;
			imp2.setFileInfo(fi);
		}
		return imp2;
	}
	
			
 	/** This method has been replaced by IJ.createHyperStack(). */
	public ImagePlus createHyperStack(String title, int channels, int slices, int frames, int bitDepth) {
		int size = channels*slices*frames;
		ImageStack stack2 = new ImageStack(width, height, size); // create empty stack
		ImageProcessor ip2 = null;
		switch (bitDepth) {
			case 8: ip2 = new ByteProcessor(width, height); break;
			case 16: ip2 = new ShortProcessor(width, height); break;
			case 24: ip2 = new ColorProcessor(width, height); break;
			case 32: ip2 = new FloatProcessor(width, height); break;
			default: throw new IllegalArgumentException("Invalid bit depth");
		}
		stack2.setPixels(ip2.getPixels(), 1); // can't create ImagePlus will null 1st image
		ImagePlus imp2 = new ImagePlus(title, stack2);
		stack2.setPixels(null, 1);
		imp2.setDimensions(channels, slices, frames);
		imp2.setCalibration(getCalibration());
		imp2.setOpenAsHyperStack(true);
		return imp2;
	}
		
	/** Copies the calibration of the specified image to this image. */
	public void copyScale(ImagePlus imp) {
		if (imp!=null && globalCalibration==null)
			setCalibration(imp.getCalibration());
	}

	/** Copies attributes (name, ID, calibration, path) of the specified image to this image. */
	public void copyAttributes(ImagePlus imp) {
		if (imp==null || imp.getWindow()!=null)
			throw new IllegalArgumentException("Souce image is null or displayed");
		ID = imp.getID();
		setTitle(imp.getTitle());
		setCalibration(imp.getCalibration());
		FileInfo fi = imp.getOriginalFileInfo();
		if (fi!=null)
			setFileInfo(fi);
		Object info = imp.getProperty("Info");
		if (info!=null)
			setProperty("Info", imp.getProperty("Info"));
	}

    /** Calls System.currentTimeMillis() to save the current
		time so it can be retrieved later using getStartTime() 
		to calculate the elapsed time of an operation. */
    public void startTiming() {
		startTime = System.currentTimeMillis();
    }

    /** Returns the time in milliseconds when 
		startTiming() was last called. */
    public long getStartTime() {
		return startTime;
    }

	/** Returns this image's calibration. */
	public Calibration getCalibration() {
		//IJ.log("getCalibration: "+globalCalibration+" "+calibration);
		if (globalCalibration!=null) {
			Calibration gc = globalCalibration.copy();
			gc.setImage(this);
			return gc;
		} else {
			if (calibration==null)
				calibration = new Calibration(this);
			return calibration;
		}
	}

   /** Sets this image's calibration. */
    public void setCalibration(Calibration cal) {
		//IJ.write("setCalibration: "+cal);
		if (cal==null)
			calibration = null;
		else {
			calibration = cal.copy();
			calibration.setImage(this);
		}
   }

    /** Sets the system-wide calibration. */
    public void setGlobalCalibration(Calibration global) {
		//IJ.log("setGlobalCalibration ("+getTitle()+"): "+global);
		if (global==null)
			globalCalibration = null;
		else
			globalCalibration = global.copy();
    }
    
    /** Returns the system-wide calibration, or null. */
    public Calibration getGlobalCalibration() {
			return globalCalibration;
    }

	/** Returns this image's local calibration, ignoring 
		the "Global" calibration flag. */
	public Calibration getLocalCalibration() {
		if (calibration==null)
			calibration = new Calibration(this);
		return calibration;
	}

    /** Displays the cursor coordinates and pixel value in the status bar.
    	Called by ImageCanvas when the mouse moves. Can be overridden by
    	ImagePlus subclasses.
    */
    public void mouseMoved(int x, int y) {
    	if (ij!=null)
			ij.showStatus(getLocationAsString(x,y) + getValueAsString(x,y));
		savex=x; savey=y;
	}
	
    private int savex, savey;

	private ImagePlus motherImp = this;

	private int motherFrame;

	private ImagePlus dupImp;

	private RemoteMQTVSHandler remoteMQTVSHandler;

	public int rotation = 0;

   
    /** Redisplays the (x,y) coordinates and pixel value (which may
		have changed) in the status bar. Called by the Next Slice and
		Previous Slice commands to update the z-coordinate and pixel value.
    */
	public void updateStatusbarValue() {
		IJ.showStatus(getLocationAsString(savex,savey) + getValueAsString(savex,savey));
	}

	String getFFTLocation(int x, int y, Calibration cal) {
		double center = width/2.0;
		double r = Math.sqrt((x-center)*(x-center) + (y-center)*(y-center));
		if (r<1.0) r = 1.0;
		double theta = Math.atan2(y-center, x-center);
		theta = theta*180.0/Math.PI;
		if (theta<0) theta = 360.0+theta;
		String s = "r=";
		if (cal.scaled())
			s += IJ.d2s((width/r)*cal.pixelWidth,2) + " " + cal.getUnit() + "/c (" + IJ.d2s(r,0) + ")";
		else
			s += IJ.d2s(width/r,2) + " p/c (" + IJ.d2s(r,0) + ")";
		s += ", theta= " + IJ.d2s(theta,2) + IJ.degreeSymbol;
		return s;
	}

    /** Converts the current cursor location to a string. */
    public String getLocationAsString(int x, int y) {
		Calibration cal = getCalibration();
		if (getProperty("FHT")!=null)
			return getFFTLocation(x, height-y-1, cal);
		//y = Analyzer.updateY(y, height);
		if (!IJ.altKeyDown()) {
			String s = " x="+d2s(cal.getX(x)) + ", y=" + d2s(cal.getY(y,height));
			if (getStackSize()>1) {
				int z = isDisplayedHyperStack()?
							((StackWindow)win).zSelector!=null?
									((StackWindow)win).zSelector.getValue():
										((StackWindow)win).tSelector!=null?
												((StackWindow)win).tSelector.label == 'z'?
														((StackWindow)win).tSelector.getValue():
															1:1:
																getCurrentSlice()-1;
				s += ", z=" + z;
				int t = isDisplayedHyperStack()?
							((StackWindow)win).tSelector!=null?
									((StackWindow)win).tSelector.label == 'z'?
										1:
											((StackWindow)win).tSelector.getValue():
												1:
													1;
				s += ", t=" + t;
			}
			return s;
		} else {
			String s =  " x="+x+", y=" + y;
			if (getStackSize()>1) {
				int z = isDisplayedHyperStack() && ((StackWindow)win).zSelector != null?((StackWindow)win).zSelector.getValue():getCurrentSlice()-1;
				s += ", z=" + z;
				int t = isDisplayedHyperStack()?((StackWindow)win).tSelector.getValue():0;
				s += ", t=" + t;
			}
			return s;
		}
    }
    
    private String d2s(double n) {
		return n==(int)n?Integer.toString((int)n):IJ.d2s(n);
	}
    
    private String getValueAsString(int x, int y) {
    	if (win!=null && win instanceof PlotWindow)
    		return "";
		Calibration cal = getCalibration();
    	int[] v = getPixel(x, y);
    	int type = getType();
		switch (type) {
			case GRAY8: case GRAY16: case COLOR_256:
				//SPECIAL CASE FORE RATIO MODES
				if (isComposite() && ((CompositeImage)this).getMode() >= CompositeImage.RATIO12) {
		   			double value = Float.intBitsToFloat(v[0]);
	    			String s = (int)value==value?IJ.d2s(value,0)+".0":IJ.d2s(value,4,7);
	    			return(", value=" + s);
				} 
				if (type==COLOR_256) {
						if (cal.getCValue(v[3])==v[3]) // not calibrated
							return(", index=" + v[3] + ", value=" + v[0] + "," + v[1] + "," + v[2]);
						else
							v[0] = v[3];					
				}
				double cValue = cal.getCValue(v[0]);
				if (cValue==v[0])
    				return(", value=" + v[0]);
    			else
    				return(", value=" + IJ.d2s(cValue) + " ("+v[0]+")");
    		case GRAY32:
    			double value = Float.intBitsToFloat(v[0]);
    			String s = (int)value==value?IJ.d2s(value,0)+".0":IJ.d2s(value,4,7);
    			return(", value=" + s);
			case COLOR_RGB:
    			return(", value=" + v[0] + "," + v[1] + "," + v[2]);
    		default: return("");
		}
    }
    
	/** Copies the contents of the current selection to the internal clipboard.
		Copies the entire image if there is no selection. Also clears
		the selection if <code>cut</code> is true. */
	public void copy(boolean cut) {
		Roi roi = getRoi();
		if (roi!=null && !roi.isArea()) {
			IJ.error("Cut/Copy", "The Cut and Copy commands require\n"
				+"an area selection, or no selection.");
			return;
		}
		boolean batchMode = Interpreter.isBatchMode();
		String msg = (cut)?"Cutt":"Copy";
		if (!batchMode) IJ.showStatus(msg+ "ing...");
		ImageProcessor ip = getProcessor();
		ImageProcessor ip2;	
		Roi roi2 = null;	
		ip2 = ip.crop();
		if (roi!=null && roi.getType()!=Roi.RECTANGLE) {
			roi2 = (Roi)roi.clone();
			Rectangle r = roi.getBounds();
			if (r.x<0 || r.y<0 || r.x+r.width>width || r.y+r.height>height) {
				roi2 = new ShapeRoi(roi2);
				ShapeRoi image = new ShapeRoi(new Roi(0, 0, width, height));
				roi2 = image.and((ShapeRoi)roi2);
			}
		}
		clipboard = new ImagePlus("Clipboard", ip2);
		if (roi2!=null) clipboard.setRoi(roi2);
		if (cut) {
			ip.snapshot();
	 		ip.setColor(Toolbar.getBackgroundColor());
			ip.fill();
			if (roi!=null && roi.getType()!=Roi.RECTANGLE) {
				getMask();
				ip.reset(ip.getMask());
			} setColor(Toolbar.getForegroundColor());
			Undo.setup(Undo.FILTER, this);
			updateAndDraw();
		}
		int bytesPerPixel = 1;
		switch (clipboard.getType()) {
			case ImagePlus.GRAY16: bytesPerPixel = 2; break;
			case ImagePlus.GRAY32: case ImagePlus.COLOR_RGB: bytesPerPixel = 4;
		}
		//Roi roi3 = clipboard.getRoi();
		//IJ.log("copy: "+clipboard +" "+ "roi3="+(roi3!=null?""+roi3:""));
		if (!batchMode) {
			msg = (cut)?"Cut":"Copy";
			IJ.showStatus(msg + ": " + (clipboard.getWidth()*clipboard.getHeight()*bytesPerPixel)/1024 + "k");
		}
    }
                

	 /** Inserts the contents of the internal clipboard into the active image. If there
	 is a selection the same size as the image on the clipboard, the image is inserted 
	 into that selection, otherwise the selection is inserted into the center of the image.*/
	 public void paste() {
		if (clipboard==null) return;
		int cType = clipboard.getType();
		int iType = getType();
		
        int w = clipboard.getWidth();
        int h = clipboard.getHeight();
		Roi cRoi = clipboard.getRoi();
		Rectangle r = null;
		Roi roi = getRoi();
		if (roi!=null)
			r = roi.getBounds();
		//if (w==width && h==height && (r==null||w!=r.width||h!=r.height)) {
		//	setRoi(0, 0, width, height);
		//	roi = getRoi();
		//	r = roi.getBounds();
		//}
		if (r==null || (r!=null && (w!=r.width || h!=r.height))) {
			// create a new roi centered on visible part of image
			ImageCanvas ic = null;
			if (win!=null)
				ic = win.getCanvas();
			Rectangle srcRect = ic!=null?ic.getSrcRect():new Rectangle(0,0,width, height);
			int xCenter = srcRect.x + srcRect.width/2;
			int yCenter = srcRect.y + srcRect.height/2;
			if (cRoi!=null && cRoi.getType()!=Roi.RECTANGLE) {
				cRoi.setImage(this);
				cRoi.setLocation(xCenter-w/2, yCenter-h/2);
				setRoi(cRoi);
			} else
				setRoi(xCenter-w/2, yCenter-h/2, w, h);
			roi = getRoi();
		} 
		if (IJ.isMacro()) {
			//non-interactive paste
			int pasteMode = Roi.getCurrentPasteMode();
			boolean nonRect = roi.getType()!=Roi.RECTANGLE;
			ImageProcessor ip = getProcessor();
			if (nonRect) ip.snapshot();
			r = roi.getBounds();
			ip.copyBits(clipboard.getProcessor(), r.x, r.y, pasteMode);
			if (nonRect) ip.reset(getMask());
			updateAndDraw();
			//deleteRoi();
		} else if (roi!=null) {
			roi.startPaste(clipboard);
			Undo.setup(Undo.PASTE, this);
		}
		changes = true;
    }

	/** Returns the internal clipboard or null if the internal clipboard is empty. */
	public static ImagePlus getClipboard() {
		return clipboard;
	}
	
	/** Clears the internal clipboard. */
	public static void resetClipboard() {
		clipboard = null;
	}

	protected void notifyListeners(int id) {
		synchronized (listeners) {
			for (int i=0; i<listeners.size(); i++) {
				ImageListener listener = (ImageListener)listeners.elementAt(i);
				switch (id) {
					case OPENED:
						listener.imageOpened(this);
						break;
					case CLOSED:
						listener.imageClosed(this);
						break;
					case UPDATED: 
						listener.imageUpdated(this);
						break;
				}
			}
		}
	}

	public static void addImageListener(ImageListener listener) {
		listeners.addElement(listener);
	}
	
	public static void removeImageListener(ImageListener listener) {
		listeners.removeElement(listener);
	}
	
	/** Returns 'true' if the image is locked. */
	public boolean isLocked() {
		return locked;
	}
	
	public void setOpenAsHyperStack(boolean openAsHyperStack) {
		this.openAsHyperStack = openAsHyperStack;
	}
	
	public boolean getOpenAsHyperStack() {
		return openAsHyperStack;
	}
	
	/** Returns true if this is a CompositeImage. */
	public boolean isComposite() {
		return compositeImage && nChannels>=1 && imageType!=COLOR_RGB && (this instanceof CompositeImage);
	}

	/** Returns the display mode (IJ.COMPOSITE, IJ.COLOR
		or IJ.GRAYSCALE) if this is a CompositeImage, otherwise returns -1. */
	public int getCompositeMode() {
		if (isComposite())
			return ((CompositeImage)this).getMode();
		else
			return -1;
	}

	/** Sets the display range of the current channel. With non-composite
	    images it is identical to ip.setMinAndMax(min, max). */
	public void setDisplayRange(double min, double max) {
		if (ip!=null)
			ip.setMinAndMax(min, max);
	}

	public double getDisplayRangeMin() {
		return ip.getMin();
	}

	public double getDisplayRangeMax() {
		return ip.getMax();
	}

	/**	Sets the display range of specified channels in an RGB image, where 4=red,
		2=green, 1=blue, 6=red+green, etc. With non-RGB images, this method is
		identical to setDisplayRange(min, max).  This method is used by the 
		Image/Adjust/Color Balance tool . */
	public void setDisplayRange(double min, double max, int channels) {
		if (ip instanceof ColorProcessor)
			((ColorProcessor)ip).setMinAndMax(min, max, channels);
		else
			ip.setMinAndMax(min, max);
	}

	public void resetDisplayRange() {
		if (imageType==GRAY16 && default16bitDisplayRange>=8 && default16bitDisplayRange<=16 && !(getCalibration().isSigned16Bit()))
			ip.setMinAndMax(0, Math.pow(2,default16bitDisplayRange)-1);
		else
			ip.resetMinAndMax();
	}
	
    /** Set the default 16-bit display range, where 'bitDepth' must be 0 (auto-scaling), 
    	8 (0-255), 10 (0-1023), 12 (0-4095, 15 (0-32767) or 16 (0-65535). */
    public static void setDefault16bitRange(int bitDepth) {
    	if (!(bitDepth==8 || bitDepth==10 || bitDepth==12 || bitDepth==15 || bitDepth==16))
    		bitDepth = 0;
    	default16bitDisplayRange = bitDepth;
    }
    
    /** Returns the default 16-bit display range, 0 (auto-scaling), 8, 10, 12, 15 or 16. */
    public static int getDefault16bitRange() {
    	return default16bitDisplayRange;
    }

	public void updatePosition(int c, int z, int t) {
		//IJ.log("updatePosition: "+c+", "+z+", "+t);
		position[0] = c;
		position[1] = z;
		position[2] = t;
	}
	
	/** Returns a "flattened" version of this image, in RGB format. */
	public ImagePlus flatten() {
		ImagePlus imp2 = createImagePlus();
		String title = "Flat_"+getTitle();
		ImageCanvas ic2 = new ImageCanvas(imp2);
		imp2.flatteningCanvas = ic2;
		imp2.setRoi(getRoi());
		if (getStackSize()>1) {
			imp2.setStack(getStack());
			imp2.setSlice(getCurrentSlice());
			if (isHyperStack()) {
				imp2.setDimensions(getNChannels(),getNSlices(),getNFrames());
				imp2.setPosition(getChannel(),getSlice(),getFrame());
				imp2.setOpenAsHyperStack(true);
			}
		}
		ImageCanvas ic = getCanvas();
		Overlay overlay2 = getOverlay();
		ic2.setOverlay(overlay2);
		if (ic!=null) {
			ic2.setShowAllROIs(ic.getShowAllROIs());
			//double mag = ic.getMagnification();
			//if (mag<1.0) ic2.setMagnification(mag);
		}
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		boolean showing = this.getCanvas().getShowAllROIs();
		this.getCanvas().setShowAllROIs(true);
		this.getCanvas().paint(bi.getGraphics());
//		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		Graphics g = bi.getGraphics();
//		g.drawImage(getImage(), 0, 0, null);
//		ic2.paint(g);
//		imp2.flatteningCanvas = null;
		if (Recorder.record) Recorder.recordCall("imp = IJ.getImage().flatten();");
		return new ImagePlus(title, new ColorProcessor(bi));
	}
	
	/** Installs a list of ROIs that will be drawn on this image as a non-destructive overlay.
	 * @see ij.gui.Roi#setStrokeColor
	 * @see ij.gui.Roi#setStrokeWidth
	 * @see ij.gui.Roi#setFillColor
	 * @see ij.gui.Roi#setLocation
	 * @see ij.gui.Roi#setNonScalable
	 */
	public void setOverlay(Overlay overlay) {
		ImageCanvas ic = getCanvas();
		if (ic!=null) {
			ic.setOverlay(overlay);
			overlay = null;
		} else
			this.overlay = overlay;
		setHideOverlay(false);
	}
	
	/** Creates an Overlay from the specified Shape, Color 
	 * and BasicStroke, and assigns it to this image.
	 * @see #setOverlay(ij.gui.Overlay)
	 * @see ij.gui.Roi#setStrokeColor
	 * @see ij.gui.Roi#setStrokeWidth
	 */
	public void setOverlay(Shape shape, Color color, BasicStroke stroke) {
		if (shape==null)
			{setOverlay(null); return;}
		Roi roi = new ShapeRoi(shape);
		roi.setStrokeColor(color);
		roi.setStroke(stroke);
		setOverlay(new Overlay(roi));
	}
	
	/** Creates an Overlay from the specified ROI, and assigns it to this image.
	 * @see #setOverlay(ij.gui.Overlay)
	 */
	public void setOverlay(Roi roi, Color strokeColor, int strokeWidth, Color fillColor) {
		roi.setStrokeColor(strokeColor);
		roi.setStrokeWidth(strokeWidth);
		roi.setFillColor(fillColor);
		setOverlay(new Overlay(roi));
	}

	/** Returns the current overly, or null if this image does not have an overlay. */
	public Overlay getOverlay() {
		ImageCanvas ic = getCanvas();
		if (ic!=null)
			return ic.getOverlay();
		else
			return overlay;
	}
	
	public void setHideOverlay(boolean hide) {
		hideOverlay = hide;
		ImageCanvas ic = getCanvas();
		if (ic!=null && ic.getOverlay()!=null)
			ic.repaint();
	}

	public boolean getHideOverlay() {
		return hideOverlay;
	}

	/** Returns a shallow copy of this ImagePlus. */
	public synchronized Object clone() {
		try {
			ImagePlus copy = (ImagePlus)super.clone();
			copy.win = null;
			return copy;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

    public String toString() {
    	return "imp["+getTitle()+" ("+width+"x"+height+"x"+getNChannels()+"x"+getNSlices()+"x"+getNFrames()+")]";
    }

	public RoiManager getRoiManager() {
		if (rm!=null)
			return rm;
		else {
			rm = new RoiManager(this, true);
			return rm;
		}
	}

	public void setRoiManager(RoiManager rm) {
		
		this.rm = rm;
		if (this.getWindow() != null) {
			if (this.getMultiChannelController() == null &&
					this.getWindow().getBackground().equals(IJ.getInstance().getBackground())) {
				String[] hexChoices = { "77",/*"88","99","aa",*/
						"bb"/*,"cc"*/, "dd"/*,"ee"*/, "ff" };

				String randomHexString = "";
				boolean unique = true;
				do {
					randomHexString = "0x"
							+ hexChoices[(int) Math.round(Math.random()
									* (hexChoices.length - 1))]
							+ hexChoices[(int) Math.round(Math.random()
									* (hexChoices.length - 1))]
							+ hexChoices[(int) Math.round(Math.random()
									* (hexChoices.length - 1))];

					for (int i = 0; i < WindowManager.getIDList().length; i++) {
						if (WindowManager
								.getImage(WindowManager.getIDList()[i])
								.getWindow() != null)
							if (WindowManager
								.getImage(WindowManager.getIDList()[i])
								.getWindow().getBackground() == Color
								.decode(randomHexString)) {
								unique = false;
							}
					}
					for (int i = 0; i < WindowManager.getNonImageWindows().length; i++) {
						if (WindowManager.getNonImageWindows()[i]
								.getBackground() == Color
								.decode(randomHexString)) {
							unique = false;
						}
					}
				} while (!unique);
				//		IJ.log(randomHexString+"");
				rm.setBackground(Color.decode(randomHexString));
				rm.getList().setBackground(
						this.getWindow().getSubTitleBkgdColor());
				this.getWindow().setBackground(Color.decode(randomHexString));
			} else {
				rm.setBackground(this.getWindow().getBackground());
			}
		}
		
	}

	public MultiChannelController getMultiChannelController() {
		return mcc;
	}

	public void setMultiChannelController(MultiChannelController mcc) {
		
		this.mcc = mcc;
		if (this.mcc == null)
			return;
		if ( this.getWindow() != null) {
			if (this.getRoiManager() == null) {
				String[] hexChoices = { "77",/*"88","99","aa",*/
						"bb"/*,"cc"*/, "dd"/*,"ee"*/, "ff" };

				String randomHexString = "";
				boolean unique = true;
				do {
					randomHexString = "0x"
							+ hexChoices[(int) Math.round(Math.random()
									* (hexChoices.length - 1))]
							+ hexChoices[(int) Math.round(Math.random()
									* (hexChoices.length - 1))]
							+ hexChoices[(int) Math.round(Math.random()
									* (hexChoices.length - 1))];

					for (int i = 0; i < WindowManager.getIDList().length; i++) {
						if (WindowManager
								.getImage(WindowManager.getIDList()[i])
								.getWindow().getBackground() == Color
								.decode(randomHexString)) {
							unique = false;
						}
					}
					for (int i = 0; i < WindowManager.getNonImageWindows().length; i++) {
						if (WindowManager.getNonImageWindows()[i]
								.getBackground() == Color
								.decode(randomHexString)) {
							unique = false;
						}
					}

				} while (!unique);
				//		IJ.log(randomHexString+"");
				mcc.setBackground(Color.decode(randomHexString));
				this.getWindow().setBackground(Color.decode(randomHexString));
			} else {
				mcc.setBackground(this.getWindow().getBackground());
			}
		}
	}

	public ImagePlus getMotherImp() {
		if (motherImp!=null)
			return motherImp;
		else 
			return this;
	}

	public void setMotherImp(ImagePlus imp, int frame) {
		this.motherImp = imp;
		this.motherFrame = frame;
	}

	public int getMotherFrame() {
		return motherFrame;
	}

	public Color getRoiFillColor() {
		return roiFillColor;
	}

	public void setDupImp(ImagePlus impCopy) {
		dupImp = impCopy;
	}
	public ImagePlus getDupImp() {
		return dupImp;
	}

	public boolean isSketch3D() {
		return (getTitle().contains("Sketch3D") || (getCanvas() != null && getCanvas().sketchyMQTVS));
	}

	public ImageProcessor getIP() {
		// TODO Auto-generated method stub
		return ip;
	}

	public void setRemoteHandler(RemoteMQTVSHandler remoteMQTVSHandler) {
		this.remoteMQTVSHandler = remoteMQTVSHandler;
	}

	public RemoteMQTVSHandler getRemoteMQTVSHandler() {
		return remoteMQTVSHandler;
	}

	public void setRotation(int i) {
		rotation = i;
	}

    
}

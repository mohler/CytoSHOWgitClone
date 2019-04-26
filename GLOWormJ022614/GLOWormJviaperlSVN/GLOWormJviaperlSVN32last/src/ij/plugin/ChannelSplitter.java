package ij.plugin;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.Calibration;
import ij.plugin.HyperStackReducer;
import java.awt.*;
import java.util.Vector;

/** This plugin implements the Image/Color/Split Channels command. */
public class ChannelSplitter implements PlugIn {

	public void run(String arg) {
		ImagePlus imp = IJ.getImage();
		if (imp.isComposite()) {
			ImagePlus[] channels = split(imp);
			for (int i=0; i<channels.length; i++)
				channels[i].show();
			imp.changes = false;
			imp.close();
		} else if (imp.getType()==ImagePlus.COLOR_RGB)
			splitRGB(imp);
		else 
			IJ.error("Split Channels", "Multichannel image required");
	}
	
	private void splitRGB(ImagePlus imp) {
		boolean keepSource = IJ.altKeyDown();
		String title = imp.getTitle();
		Calibration cal = imp.getCalibration();
		ImageStack[] channels = splitRGB(imp.getStack(), keepSource);
		if (!keepSource)
			{imp.unlock(); imp.changes=false; imp.close();}
		ImagePlus rImp = new ImagePlus(title+" (red)", channels[0]);
		rImp.setCalibration(cal);
		rImp.show();
		if (IJ.isMacOSX()) IJ.wait(500);
		ImagePlus gImp = new ImagePlus(title+" (green)", channels[1]);
		gImp.setCalibration(cal);
		gImp.show();
		if (IJ.isMacOSX()) IJ.wait(500);
		ImagePlus bImp = new ImagePlus(title+" (blue)", channels[2]);
		bImp.setCalibration(cal);
		bImp.show();
	}

	private void split123(ImagePlus imp) {
		boolean keepSource = IJ.altKeyDown();
		String title = imp.getTitle();
		Calibration cal = imp.getCalibration();
		ImageStack[] channels = splitRGB(imp.getStack(), keepSource);
		if (!keepSource)
			{imp.unlock(); imp.changes=false; imp.close();}
		ImagePlus rImp = new ImagePlus(title+" (Ch1)", channels[0]);
		rImp.setCalibration(cal);
		rImp.show();
		if (IJ.isMacOSX()) IJ.wait(500);
		ImagePlus gImp = new ImagePlus(title+" (Ch2)", channels[1]);
		gImp.setCalibration(cal);
		gImp.show();
		if (IJ.isMacOSX()) IJ.wait(500);
		ImagePlus bImp = new ImagePlus(title+" (Ch3)", channels[2]);
		bImp.setCalibration(cal);
		bImp.show();
	}

	/** Splits the specified image into separate channels. */
	public static ImagePlus[] split(ImagePlus imp) {
		if (imp.getType()==ImagePlus.COLOR_RGB) {
			return null;
		}
		int width = imp.getWidth();
		int height = imp.getHeight();
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		int bitDepth = imp.getBitDepth();
		int size = slices*frames;
		Vector images = new Vector();
		HyperStackReducer reducer = new HyperStackReducer(imp);
		for (int c=1; c<=channels; c++) {
			ImageStack stack2 = new ImageStack(width, height, size); // create empty stack
			stack2.setPixels(imp.getProcessor().getPixels(), 1); // can't create ImagePlus will null 1st image
			ImagePlus imp2 = new ImagePlus("C"+c+"-"+imp.getTitle(), stack2);
			stack2.setPixels(null, 1);
			imp.setPosition(c, 1, 1);
			imp2.setDimensions(1, slices, frames);
			imp2.setCalibration(imp.getCalibration());
			reducer.reduce(imp2);
			if (imp.isComposite() && ((CompositeImage)imp).getMode()==CompositeImage.GRAYSCALE)
				IJ.run(imp2, "Grays", "");
			if (imp2.getNDimensions()>3)
				imp2.setOpenAsHyperStack(true);
			images.add(imp2);
		}
		ImagePlus[] array = new ImagePlus[images.size()];
		return (ImagePlus[])images.toArray(array);
	}
	
	public static ImageStack getChannel(ImagePlus imp, int c) {
		ImageStack stack1 = imp.getStack();
		ImageStack stack2 = new ImageStack(imp.getWidth(), imp.getHeight());
		for (int t=1; t<=imp.getNFrames(); t++) {
			for (int z=1; z<=imp.getNSlices(); z++) {
				int n = imp.getStackIndex(c, z, t);
				stack2.addSlice(stack1.getProcessor(n));
			}
		}
		return stack2;
	}
	
	/** Splits the specified RGB stack into three 8-bit grayscale stacks. 
		Deletes the source stack if keepSource is false. */
	public static ImageStack[] splitRGB(ImageStack rgb, boolean keepSource) {
		 int w = rgb.getWidth();
		 int h = rgb.getHeight();
		 ImageStack[] channels = new ImageStack[3];
		 for (int i=0; i<3; i++)
		 	channels[i] = new ImageStack(w,h);
		 byte[] r,g,b;
		 ColorProcessor cp;
		 int slice = 1;
		 int inc = keepSource?1:0;
		 int n = rgb.getSize();
		 for (int i=1; i<=n; i++) {
			 IJ.showStatus(i+"/"+n);
			 r = new byte[w*h];
			 g = new byte[w*h];
			 b = new byte[w*h];
			 //IJ.log(rgb.getProcessor(slice).toString());
			 cp = (ColorProcessor)rgb.getProcessor(slice);
			 slice += inc;
			 cp.getRGB(r,g,b);
			 if (!keepSource)
				rgb.deleteSlice(1);
			 channels[0].addSlice(null,r);
			 channels[1].addSlice(null,g);
			 channels[2].addSlice(null,b);
			 IJ.showProgress((double)i/n);
		}
		return channels;
	}

}


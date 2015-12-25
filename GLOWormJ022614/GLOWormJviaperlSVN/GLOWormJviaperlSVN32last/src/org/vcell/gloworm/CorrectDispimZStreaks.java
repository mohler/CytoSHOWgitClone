package org.vcell.gloworm;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javafx.scene.control.Spinner;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Toolbar;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.MaximumFinder;
import ij.process.Blitter;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class CorrectDispimZStreaks implements PlugIn {
	ImagePlus imp;
	ImageProcessor gaussianDiffIP;
	private MaximumFinder mf;

	public void run(String arg) {
		imp = IJ.getImage();
		int slice = imp.getSlice();
		ImagePlus gaussianDiffImp = (new ImagePlus("http://fsbill.cam.uchc.edu/Xwords/z-x_Mask_ver_-32bkg_x255over408_15x33rect.tif"));
		gaussianDiffImp.getProcessor().setMinAndMax(0, 255);
		IJ.run(gaussianDiffImp, imp.getBitDepth()+"-bit", "");
		gaussianDiffIP = gaussianDiffImp.getProcessor();
		GenericDialog gd = new GenericDialog("Specify Z Correction Options...");
//		IJ.log(Prefs.getPrefsDir());
		gd.addNumericField("Mask Scale Factor", Prefs.getDouble("Zstreak.maskScaleFactor", 0.25), 5);
		gd.addNumericField("Max Tolerance", Prefs.getInt("Zstreak.maxTolerance", 10), 0);
		gd.addNumericField("Min Tolerance", Prefs.getInt("Zstreak.minTolerance", 0), 0);
		gd.addNumericField("Iterations at Min Tol", Prefs.getInt("Zstreak.iterations", 10), 0);
		gd.addNumericField("BlankWidth", Prefs.getInt("Zstreak.blankWidth", 5), 0);
		gd.addNumericField("BlankHeight", Prefs.getInt("Zstreak.blankHeight", 3), 0);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		double maskScaleFactor = (double) gd.getNextNumber();
		Prefs.set("Zstreak.maskScaleFactor", maskScaleFactor);
		int maxTolerance = (int) gd.getNextNumber();
		Prefs.set("Zstreak.maxTolerance", maxTolerance);
		int minTolerance = (int) gd.getNextNumber();
		Prefs.set("Zstreak.minTolerance", minTolerance);
		int iterations = (int) gd.getNextNumber();
		Prefs.set("Zstreak.iterations", iterations);
		int blankWidth = (int) gd.getNextNumber();
		Prefs.set("Zstreak.blankWidth", blankWidth);
		int blankHeight = (int) gd.getNextNumber();
		Prefs.set("Zstreak.blankHeight", blankHeight);
		Prefs.savePreferences();	
		for (int s=1;s<=imp.getStackSize();s++) {
			imp.setSlice(s);
			ArrayList<String> maxCum = new ArrayList<String>();
			ImageProcessor targetIP = imp.getProcessor().duplicate();
			for (int t=minTolerance;t>maxTolerance;t--) {
				mf = new MaximumFinder();
				Polygon maxPoly = mf.getMaxima(targetIP, t, false);

				int[] maxXs = new int[maxPoly.npoints];
				int[] maxYs = new int[maxPoly.npoints];

				maxXs = maxPoly.xpoints;
				maxYs = maxPoly.ypoints;

				for (int n=0; n<maxXs.length; n++) {
					if (!maxCum.contains(maxXs[n]+","+maxYs[n])) {
						maxCum.add(maxXs[n]+","+maxYs[n]);
						ImageProcessor modIP = gaussianDiffIP.duplicate();
						modIP.multiply(maskScaleFactor * ((double)imp.getProcessor().getPixel(maxXs[n], maxYs[n]))/255);
						imp.getProcessor().copyBits(modIP, maxXs[n]-gaussianDiffIP.getWidth()/2, maxYs[n]-gaussianDiffIP.getHeight()/2, Blitter.DIFFERENCE);
						targetIP.copyBits(modIP, maxXs[n]-gaussianDiffIP.getWidth()/2, maxYs[n]-gaussianDiffIP.getHeight()/2, Blitter.DIFFERENCE);
						Color fgc = Toolbar.getForegroundColor();
						Toolbar.setForegroundColor(Color.BLACK);
						targetIP.fillOval(maxXs[n]-2, maxYs[n]-1, blankWidth, blankHeight);
						Toolbar.setForegroundColor(fgc);

					}
				}
			}
			for (int t=iterations;t>0;t--) {
				mf = new MaximumFinder();
				Polygon maxPoly = mf.getMaxima(targetIP, minTolerance, false);

				int[] maxXs = new int[maxPoly.npoints];
				int[] maxYs = new int[maxPoly.npoints];

				maxXs = maxPoly.xpoints;
				maxYs = maxPoly.ypoints;

				for (int n=0; n<maxXs.length; n++) {
					if (!maxCum.contains(maxXs[n]+","+maxYs[n])) {
						maxCum.add(maxXs[n]+","+maxYs[n]);
						ImageProcessor modIP = gaussianDiffIP.duplicate();
						modIP.multiply(maskScaleFactor * ((double)imp.getProcessor().getPixel(maxXs[n], maxYs[n]))/1000);
						imp.getProcessor().copyBits(modIP, maxXs[n]-gaussianDiffIP.getWidth()/2, maxYs[n]-gaussianDiffIP.getHeight()/2, Blitter.DIFFERENCE);
						targetIP.copyBits(modIP, maxXs[n]-gaussianDiffIP.getWidth()/2, maxYs[n]-gaussianDiffIP.getHeight()/2, Blitter.DIFFERENCE);
						Color fgc = Toolbar.getForegroundColor();
						Toolbar.setForegroundColor(Color.BLACK);
						targetIP.fillOval(maxXs[n]-2, maxYs[n]-1, blankWidth, blankHeight);
						Toolbar.setForegroundColor(fgc);

					}
				}
			}
		}
		imp.setSlice(slice);
		imp.updateAndDraw();
	}

}

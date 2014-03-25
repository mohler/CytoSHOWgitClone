package ij.plugin;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.awt.image.*;

/** This plugin implements the Help/About ImageJ command by opening
	the about.jpg in ij.jar, scaling it 400% and adding some text. */
	public class AboutBox implements PlugIn {
		static final int SMALL_FONT=14, LARGE_FONT=30;

	public void run(String arg) {
		System.gc();
		int lines = 12;
		String[] text = new String[lines];
		text[0] = "ImageJ "+ImageJ.VERSION+ImageJ.BUILD;
		text[1] = "Wayne Rasband";
		text[2] = "National Institutes of Health, USA";
		text[3] = IJ.URL;
		text[4] = "Java "+System.getProperty("java.version")+(IJ.is64Bit()?" (64-bit)":" (32-bit)");
		text[5] = IJ.freeMemory();
		text[6] = " ";
		text[7] = "CytoSHOW capabilities integrated by";
		text[8] = "Bill Mohler";
		text[9] = "UConn Health Center";
		text[10] = "http://gloworm.org";
		text[11] = "ImageJ is in the public domain";
		ImageProcessor ip = null;
		ImageJ ij = IJ.getInstance();
		URL url;
		try {
			url = new URL("http://fsbill.cam.uchc.edu/gloworm/Xwords/Gloworm3.jpg");
			if (url!=null) {
				Image img = null;
				try {img = ij.createImage((ImageProducer)url.getContent());}
				catch(Exception e) {}
				if (img!=null) {
					ImagePlus imp = new ImagePlus("", img);
					ip = imp.getProcessor();
				}
			}
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (ip==null) 
			ip =  new ColorProcessor(110,90);
		ip = ip.resize(ip.getWidth()*2, ip.getHeight()*2);
		ip.setFont(new Font("SansSerif", Font.PLAIN, LARGE_FONT));
		ip.setAntialiasedText(true);
		int[] widths = new int[lines];
		widths[0] = ip.getStringWidth(text[0]);
		ip.setFont(new Font("SansSerif", Font.PLAIN, SMALL_FONT));
		for (int i=1; i<lines-1; i++)
			widths[i] = ip.getStringWidth(text[i]);
		int max = 0;
		for (int i=0; i<lines-1; i++) 
			if (widths[i]>max)
				max = widths[i];
		ip.setColor(new Color(255,255, 140));
		ip.setFont(new Font("SansSerif", Font.PLAIN, LARGE_FONT));
		int y  = 45;
		ip.drawString(text[0], x(text[0],ip,max), y);
		ip.setFont(new Font("SansSerif", Font.PLAIN, SMALL_FONT));
		y += 30;
		ip.drawString(text[1], x(text[1],ip,max), y);
		y += 18;
		ip.drawString(text[2], x(text[2],ip,max), y);
		y += 18;
		ip.drawString(text[3], x(text[3],ip,max), y);
		y += 18;
		ip.drawString(text[4], x(text[4],ip,max), y);
		if (IJ.maxMemory()>0L) {
			y += 18;
			ip.drawString(text[5], x(text[5],ip,max), y);
		}
		y += 18;
		ip.drawString(text[6], x(text[6],ip,max), y);
		y += 18;
		ip.drawString(text[7], x(text[7],ip,max), y);
		y += 18;
		ip.drawString(text[8], x(text[8],ip,max), y);
		y += 18;
		ip.drawString(text[9], x(text[9],ip,max), y);
		y += 18;
		ip.drawString(text[10], x(text[10],ip,max), y);

		ip.drawString(text[11], ip.getWidth()-ip.getStringWidth(text[11])-10, ip.getHeight()-3);
		ImageWindow.centerNextImage();
		new ImagePlus("About CytoSHOW", ip).show();
	}

	int x(String text, ImageProcessor ip, int max) {
		return ip.getWidth() - max + (max - ip.getStringWidth(text))/2 - 10;
	}

}

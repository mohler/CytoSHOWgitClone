package ij.util;
import ij.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

/**
This class contains static methods that use the Java 2 API. They are isolated 
here to prevent errors when ImageJ is running on Java 1.1 JVMs.
*/
public class Java2 {

	private static boolean lookAndFeelSet;

	public static void setAntialiased(Graphics g, boolean antialiased) {
			Graphics2D g2d = (Graphics2D)g;
			if (antialiased)
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			else
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public static void setAntialiasedText(Graphics g, boolean antialiasedText) {
			Graphics2D g2d = (Graphics2D)g;
			if (g2d!=null){
			if (antialiasedText)
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			else
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}
	}

	public static int getStringWidth(String s, FontMetrics fontMetrics, Graphics g) {
			java.awt.geom.Rectangle2D r = fontMetrics.getStringBounds(s, g);
			return (int)r.getWidth();
	}

	public static void setBilinearInterpolation(Graphics g, boolean bilinearInterpolation) {
			Graphics2D g2d = (Graphics2D)g;
			if (bilinearInterpolation)
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			else
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}
	
	/** Sets the Swing look and feel to the system look and feel (Windows only). */
	public static void setSystemLookAndFeel() {
		if (lookAndFeelSet || !IJ.isWindows()) 
			return;
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, fall back to cross-platform
		    try {
		        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		    } catch (Exception ex) {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch(Throwable t) {}
		    }
		}
//		try {
////			UIManager.setLookAndFeel(UIManager.getInstalledLookAndFeels()[(int)Math.random()*(UIManager.getInstalledLookAndFeels().length-1)].getClassName());
//		} catch(Throwable t) {}
		lookAndFeelSet = true;
		IJ.register(Java2.class);
	}

}


package org.vcell.gloworm;

import java.io.File;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class ObjEditor implements PlugIn {

	@Override
	public void run(String arg) {
		String[] args = arg.split("\\|");
		double[] scalesAndShifts = new double[args.length];
		String objDir = "";
		String outDir = "";
		boolean autofilled = false;
		for (int a=0; a<args.length; a++) {
			if (a<args.length-2) {
				scalesAndShifts[a] = Double.parseDouble(args[a]);
			} else if (a==args.length-2) {
				objDir = args[a];
			} else if (a==args.length-1) {
				outDir = args[a];
			}
		}
		autofilled = args.length==8;
		double scaleX = autofilled?scalesAndShifts[0]:0;
		double scaleY = autofilled?scalesAndShifts[1]:0;
		double scaleZ = autofilled?scalesAndShifts[2]:0;
		double shiftX = autofilled?scalesAndShifts[3]:0;
		double shiftY = autofilled?scalesAndShifts[4]:0;
		double shiftZ = autofilled?scalesAndShifts[5]:0;
		if (!autofilled) {
			GenericDialog gd = new GenericDialog("Specify scaling and translations for objs");
			gd.addNumericField("scaleX", 1.0, 0, 0, "");
			gd.addNumericField("scaleY", 1.0, 0, 0, "");
			gd.addNumericField("scaleZ", 1.0, 0, 0, "");
			gd.addNumericField("shiftX", 0.0, 0, 0, "");
			gd.addNumericField("shiftY", 0.0, 0, 0, "");
			gd.addNumericField("shiftZ", 0.0, 0, 0, "");
			gd.showDialog();
			 scaleX = gd.getNextNumber();
			 scaleY = gd.getNextNumber();
			 scaleZ = gd.getNextNumber();
			 shiftX = gd.getNextNumber();
			 shiftY = gd.getNextNumber();
			 shiftZ = gd.getNextNumber();

			objDir = IJ.getDirectory("which file or folder of objs to edit?");
			outDir = IJ.getDirectory("Which folder to save edited objs?");
		}
		
		String[] objDirList = null;
		if (new File (objDir).isDirectory()) {
			objDirList = new File(objDir).list();
		} else {
			objDirList = new String[]{new File(objDir).getName()};
			objDir = new File(objDir).getParent();
		}

		for (String objName:objDirList) {
			if (objName.toLowerCase().endsWith(".obj")) {
				String[] objLines = IJ.openAsString(objDir+File.separator+objName).split("\n");
				File outFile = new File(outDir+objName.replace(".obj", "Fix.obj"));
				if (outFile.exists())
					outFile.delete();
				for (String objLine:objLines) {
					if (objLine.startsWith("v ")) {
						String[] objVertChunks = objLine.split(" ");
						Double objVertX = Double.parseDouble(objVertChunks[1]);
						Double objVertY = Double.parseDouble(objVertChunks[2]);
						Double objVertZ = Double.parseDouble(objVertChunks[3]);
						String objNewLine = "v " + ((objVertX*scaleX)+shiftX) + " " + ((objVertY*scaleY)+shiftY) + " " + ((objVertZ*scaleZ)+shiftZ);
						IJ.append(objNewLine, outDir+objName.replace(".obj", "Fix.obj"));
					} else {
						IJ.append(objLine, outDir+objName.replace(".obj", "Fix.obj"));
					}
				}
				//IJ.saveString(objOutPutString, outDir+objName);
			}
		}

	}

}


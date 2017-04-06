package org.vcell.gloworm;

import java.awt.Button;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.image.ColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.VirtualStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.SelectKeyChannelDialog;
import ij.gui.StackWindow;
import ij.gui.YesNoCancelDialog;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.TiffDecoder;
import ij.macro.MacroRunner;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.FFT;
import ij.plugin.FileInfoVirtualStack;
import ij.plugin.FolderOpener;
import ij.plugin.MultiFileInfoVirtualStack;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.SyncWindows;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

public class DISPIM_Monitor implements PlugIn {

	//Info from proximal tiff header in MMomeTIFF vvvvvvv
	 private double diSPIM_MM_LaserExposure_ms;
	 private String diSPIM_MM_MVRotations;
	 private String diSPIM_MM_SPIMtype;
	 private String diSPIM_MM_UUID;
//	 private int diSPIM_MM_SPIMAcqSettings;
	 private String diSPIM_MM_spimMode;
	 private boolean diSPIM_MM_isStageScanning;
	 private boolean diSPIM_MM_useTimepoints;
	 private int diSPIM_MM_numTimepoints;
	 private double diSPIM_MM_timepointInterval;
	 private boolean diSPIM_MM_useMultiPositions;
	 private boolean diSPIM_MM_useChannels;
	 private String diSPIM_MM_channelMode;
	 private int diSPIM_MM_numChannels;
//	 private int diSPIM_MM_channels;
	 private boolean[] diSPIM_MM_useChannel;
	 private String[] diSPIM_MM_group;
	 private String[] diSPIM_MM_config;
	 private String diSPIM_MM_channelGroup;
	 private boolean diSPIM_MM_useAutofocus;
	 private int diSPIM_MM_numSides;
	 private boolean diSPIM_MM_firstSideIsA;
	 private double diSPIM_MM_delayBeforeSide;
	 private int diSPIM_MM_numSlices;
	 private double diSPIM_MM_stepSizeUm;
	 private boolean diSPIM_MM_minimizeSlicePeriod;
	 private double diSPIM_MM_desiredSlicePeriod;
	 private double diSPIM_MM_desiredLightExposure;
	 private boolean diSPIM_MM_centerAtCurrentZ;
//	 private int diSPIM_MM_sliceTiming;
	 private double diSPIM_MM_scanDelay;
	 private int diSPIM_MM_scanNum;
	 private double diSPIM_MM_scanPeriod;
	 private double diSPIM_MM_laserDelay;
	 private double diSPIM_MM_laserDuration;
	 private double diSPIM_MM_cameraDelay;
	 private double diSPIM_MM_cameraDuration;
	 private double diSPIM_MM_cameraExposure;
	 private double diSPIM_MM_sliceDuration;
	 private boolean diSPIM_MM_valid;
	 private String diSPIM_MM_cameraMode;
	 private boolean diSPIM_MM_useHardwareTimepoints;
	 private boolean diSPIM_MM_useSeparateTimepoints;
	 private String diSPIM_MM_Position_X;
	 private String diSPIM_MM_Position_Y;
	 private String diSPIM_MM_Date;
	 private int diSPIM_MM_MetadataVersion;
	 private int diSPIM_MM_Width;
	 private double diSPIM_MM_PixelAspect;
	 private String[] diSPIM_MM_ChNames;
	 private int diSPIM_MM_Height;
	 private String diSPIM_MM_SlicePeriod_ms;
	 private int diSPIM_MM_GridColumn;
	 private double diSPIM_MM_PixelSize_um;
	 private int diSPIM_MM_Frames;
	 private String diSPIM_MM_Source;
	 private int diSPIM_MM_Channels;
	 private String diSPIM_MM_AcqusitionName;
	 private int diSPIM_MM_NumberOfSides;
	 private String diSPIM_MM_SPIMmode;
	 private int[] diSPIM_MM_ChColors;
	 private int diSPIM_MM_Slices;
	 private String diSPIM_MM_UserName;
	 private int diSPIM_MM_Depth;
	 private String diSPIM_MM_PixelType;
	 private String diSPIM_MM_Time;
	 private String diSPIM_MM_FirstSide;
	 private double diSPIM_MM_zStep_um;
	 private boolean diSPIM_MM_SlicesFirst;
	 private int[] diSPIM_MM_ChContrastMin;
	 private String diSPIM_MM_StartTime;
	 private String diSPIM_MM_MVRotationAxis;
	 private String diSPIM_MM_MicroManagerVersion;
	 private int diSPIM_MM_IJType;
	 private int diSPIM_MM_GridRow;
	 private String diSPIM_MM_VolumeDuration;
	 private int diSPIM_MM_NumComponents;
	 private String diSPIM_MM_Position_SPIM_Head;
	 private int diSPIM_MM_BitDepth;
	 private String diSPIM_MM_ComputerName;
	 private String diSPIM_MM_CameraMode;
	 private boolean diSPIM_MM_TimeFirst;
	 private int[] diSPIM_MM_ChContrastMax;
	 private int diSPIM_MM_Positions;
		//Info from proximal tiff header in MMomeTIFF ^^^^^^

	
	private boolean doDecon;
	private int keyChannel;
	private int slaveChannel;
	private int oldLength;
	private int cDim;
	private int zDim;
	private int tDim;
	private int vDim;
	private int pDim = 1;
	private String[] diSPIM_MM_ChColorStrings;
	
	

	public boolean isDoDecon() {
		return doDecon;
	}

	public void setDoDecon(boolean doDecon) {
		this.doDecon = doDecon;
	}

	public void run(String arg) {
		String[] args = arg.split("\\|");
		IJ.log(arg);
		int minA = 0;
		int maxA = 255;
		String channelsA = "11";
		int modeA = CompositeImage.COMPOSITE;
		int modeB = CompositeImage.COMPOSITE;
		double vWidth = 0.1625;
		double vHeight = 0.1625;
		double vDepthRaw = 1.000;
		double vDepthDecon = 0.1625;
		String vUnit = "micron";
		doDecon = true;
		int cropWidth = 325;
		int cropHeight = 425;
		boolean stackDualViewTimePoints = false;
		boolean singleImageTiffs = false;
		boolean omeTiffs = false;
		boolean stackLabviewTimePoints = false;
		boolean stageScan = false;
		String dirOrOMETiff = "";
		dirOrOMETiff = args[0];
		IJ.log(dirOrOMETiff);
		// waitForUser("");
		String keyString ="";

		while (!(new File(dirOrOMETiff)).isDirectory()
				&& !dirOrOMETiff.endsWith(".tif")) {
			if (arg.contains("newMM")) {
				dirOrOMETiff = IJ
						.getDirectory("Select a timepoint directory with MM diSPIM raw data");
				keyString = (new File(dirOrOMETiff)).getName().split("_")[0];
				dirOrOMETiff = (new File(dirOrOMETiff)).getParent()+File.separator;
				stackDualViewTimePoints = true;
				singleImageTiffs = false;
				omeTiffs = true;
				stackLabviewTimePoints = false;
				stageScan = false;
			} else if (arg.contains("sstMM")) {
				;
				dirOrOMETiff = IJ
						.getFilePath("Select a file with WG MM diSPIM raw data");
				stackDualViewTimePoints = false;
				singleImageTiffs = true;
				omeTiffs = false;
				stackLabviewTimePoints = false;
				stageScan = false;
			} else if (arg.contains("megaTiffMM")) {
				;
				dirOrOMETiff = IJ
						.getFilePath("Select a file with old megaTiff MM diSPIM raw data");
				stackDualViewTimePoints = false;
				singleImageTiffs = false;
				omeTiffs = true;
				stackLabviewTimePoints = false;
				stageScan = false;
			} else if (arg.contains("stageScanMM")) {
				dirOrOMETiff = IJ
						.getDirectory("Select a timepoint directory with stage-scanned MM diSPIM raw data");
				keyString = (new File(dirOrOMETiff)).getName().split("_")[0];
				dirOrOMETiff = (new File(dirOrOMETiff)).getParent()+File.separator;
				stackDualViewTimePoints = true;
				singleImageTiffs = false;
				omeTiffs = true;
				stackLabviewTimePoints = false;
				stageScan = true;
			} else if (arg.contains("stageScansstMM")) {
				dirOrOMETiff = IJ
						.getFilePath("Select a file with stage-scanned WG MM diSPIM raw data");
				stackDualViewTimePoints = false;
				singleImageTiffs = true;
				omeTiffs = false;
				stackLabviewTimePoints = false;
				stageScan = true;
			} else if (arg.contains("scanStageLabView")) {
				dirOrOMETiff = IJ
						.getDirectory("Select master directory with stage-scanned LabView diSPIM raw data");
				stackDualViewTimePoints = false;
				singleImageTiffs = false;
				omeTiffs = false;
				stackLabviewTimePoints = true;
				stageScan = true;
			} else {
				dirOrOMETiff = IJ
						.getDirectory("Select master directory with LabView diSPIM raw data");
				stackDualViewTimePoints = false;
				singleImageTiffs = false;
				omeTiffs = false;
				stackLabviewTimePoints = true;
				stageScan = false;
			}
		}
		IJ.log(dirOrOMETiff);
		File dirOrOMETiffFile = new File(dirOrOMETiff);
		String savePath = dirOrOMETiffFile.getParentFile().getParent()
				+ File.separator + dirOrOMETiffFile.getParentFile().getName()
				+ "_" + dirOrOMETiffFile.getName().split("_")[0] + "_";
		if (dirOrOMETiffFile.isDirectory())
			savePath = dirOrOMETiff;
		final String tempDir = IJ.getDirectory("temp");
		String[] fileListA = { "" };
		String[] fileListB = { "" };
		// fileListB = newArray("");
		String[] fileRanksA = { "" };
		String[] fileRanksB = { "" };
		String[] fileNumsA = { "" };
		String[] fileNumsB = { "" };
		String[] fileSortA = { "" };
		String[] fileSortB = { "" };
		String[] newTifListA = { "" };
		String[] newTifListB = { "" };
		String[] listA = { "" };
		String[] listB = { "" };
		String[] deconFileList1 = { "" };
		String[] deconFileList2 = { "" };
		String[] deconList1 = { "" };
		String[] deconList2 = { "" };
		String big5DFileListAString = ("");
		String big5DFileListBString = ("");
		ImagePlus impA = null;
		ImagePlus impB = null;
		ImagePlus impDF1 = null;
		ImagePlus impDF2 = null;
		CompositeImage ciDF1 = null;
		CompositeImage ciDF2 = null;
		ImagePlus[] impAs = new ImagePlus[1];
		ImagePlus[] impBs  = new ImagePlus[1];
		ImagePlus[] impDF1s  = new ImagePlus[1];
		ImagePlus[] impDF2s  = new ImagePlus[1];
		CompositeImage[] ciDF1s  = new CompositeImage[1];
		CompositeImage[] ciDF2s  = new CompositeImage[1];

		ImageWindow win = null;

		String[] dirOrOMETiffChunks = dirOrOMETiff
				.split(IJ.isWindows() ? "\\\\" : "/");
		// String dirOrOMETiffName =
		// dirOrOMETiffChunks[dirOrOMETiffChunks.length-1];

		if ((new File(dirOrOMETiff)).isDirectory() && !omeTiffs) {
			IJ.saveString("", dirOrOMETiff + "Big5DFileListA.txt");
			while (!(new File(dirOrOMETiff + "Big5DFileListA.txt")).exists())
				IJ.wait(100);
			IJ.saveString("", dirOrOMETiff + "Big5DFileListB.txt");
			while (!(new File(dirOrOMETiff + "Big5DFileListB.txt")).exists())
				IJ.wait(100);
			IJ.saveString("", dirOrOMETiff + "BigMAXFileListA.txt");
			while (!(new File(dirOrOMETiff + "BigMAXFileListA.txt")).exists())
				IJ.wait(100);
			IJ.saveString("", dirOrOMETiff + "BigMAXFileListB.txt");
			while (!(new File(dirOrOMETiff + "BigMAXFileListB.txt")).exists())
				IJ.wait(100);
		}

		int wavelengths = 1;
		int zSlices = 1;
		if (args.length > 2) {
			wavelengths = Integer.parseInt(args[1]);
			zSlices = Integer.parseInt(args[2]);
		} else if (!omeTiffs || !dirOrOMETiffFile.isDirectory()) {
			GenericDialog gd = new GenericDialog("Data Set Parameters?");
			gd.addNumericField("Wavelengths", 2, 0);
			gd.addNumericField("Z Slices/Stack", 50, 0);
			gd.showDialog();
			;
			wavelengths = (int) gd.getNextNumber();
			zSlices = (int) gd.getNextNumber();
		}
		dirOrOMETiffFile = new File(dirOrOMETiff);
		if (dirOrOMETiffFile.isDirectory()) {
			if (omeTiffs) {
				fileListA = new File("" + dirOrOMETiff).list();

//				cDim = 0;
//				zDim = 0;
//				tDim = 0;
//				vDim = 0;
//				GenericDialog gd = new GenericDialog("Dimensions of HyperStacks");
//				gd.addNumericField("Channels (c):", 2, 0);
//				gd.addNumericField("Slices (z):", 50, 0);
////				gd.addNumericField("Frames (t):", dirOrOMETiffFile.list().length, 0);
//				gd.addNumericField("Views (v):", 2, 0);
//				gd.addNumericField("Positions (p):", 1, 0);
//
//				gd.showDialog();
//				if (gd.wasCanceled()) return;
//				if (cDim == 0 || tDim == 0 || tDim == 0) {
//					cDim = (int) gd.getNextNumber();
//					wavelengths = cDim;
//					zDim = (int) gd.getNextNumber();
////					tDim = (int) gd.getNextNumber();
//					vDim = (int) gd.getNextNumber();
//					pDim = (int) gd.getNextNumber();
//				}

				if (keyString =="")
					keyString = dirOrOMETiffFile.list()[dirOrOMETiffFile.list().length/2].split("_")[0];

				//Reading in diSPIM header from MM tiffs vvvvvv
				String diSPIMheader = "";
				for (String fileName:dirOrOMETiffFile.list()) {
					File nextFile = new File(dirOrOMETiffFile+File.separator+fileName);

					if(nextFile.isDirectory() && nextFile.list().length>0) {
						for (String listFile:nextFile.list()){
							if (listFile.contains("MMStack")) {
								diSPIMheader = open_diSPIMheaderAsString(nextFile.getPath()+File.separator+listFile);
								tDim++;
								break;
							}
						}
					}
				}
				IJ.log(diSPIMheader);
				
				String squareSetsSearchPattern=".*";
				int squareSetsCount=0;
				for(int i = 0; i < diSPIMheader.length(); i++){
					if (diSPIMheader.charAt(i)=='[') {
						squareSetsSearchPattern = squareSetsSearchPattern+"(\\[.*\\]).*";
						squareSetsCount++;
					}
				}
				
				for(int capture=2;capture<=squareSetsCount;capture++) {
					diSPIMheader = diSPIMheader.replace((diSPIMheader.replaceAll(squareSetsSearchPattern, "$"+capture)),
														(diSPIMheader.replaceAll(squareSetsSearchPattern, "$"+capture).replace(",",";")));
				}
				
					String[] diSPIMheaderChunks = diSPIMheader
												.replace("\\\"", "\"")
												.replace(",", "\\n")
												.replace("}", "\\n}\\n")
												.replace("{", "\\n{\\n")
//												.replace("]", "\\n]\\n")
//												.replace("[", "\\n[\\n")
												.split("\\\\n");
				int indents = 1;
				String allVars = "";
				int diSPIM_MM_channel_use_index=0; 
				int diSPIM_MM_channel_group_index=0; 
				int diSPIM_MM_channel_config_index=0; 
				int diSPIM_MM_channel_name_index=0; 

				for(String chunk:diSPIMheaderChunks) {
					if (chunk.startsWith("}") || chunk.startsWith("]")) {
						indents--;
					}
					for (int i=0;i<indents;i++)
						chunk = "        "+chunk;
					IJ.log(chunk);
					if (chunk.contains("{") || chunk.contains("[")) {
						indents++;
					}
					
					if (chunk.trim().startsWith("\"LaserExposure_ms\":")) {
						diSPIM_MM_LaserExposure_ms= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"MVRotations\":")) {
						diSPIM_MM_MVRotations= chunk.split(":")[1].replace("\"", "").trim();
					}
					if (chunk.trim().startsWith("\"SPIMtype\":")) {
						diSPIM_MM_SPIMtype= chunk.split(":")[1].replace("\"", "").trim();
					}
					if (chunk.trim().startsWith("\"UUID\":")) {
						diSPIM_MM_UUID= chunk.split(":")[1].replace("\"", "").trim();
					}					 									

					if (chunk.trim().startsWith("\"spimMode\":")) {
						diSPIM_MM_spimMode= chunk.split(":")[1].replace("\"", "").trim();
					}
					if (chunk.trim().startsWith("\"isStageScanning\":")) {
						diSPIM_MM_isStageScanning= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}
					if (chunk.trim().startsWith("\"useTimepoints\":")) {
						diSPIM_MM_useTimepoints= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}
					if (chunk.trim().startsWith("\"numTimepoints\":")) {
						diSPIM_MM_numTimepoints= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"timepointInterval\":")) {
						diSPIM_MM_timepointInterval= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"useMultiPositions\":")) {
						diSPIM_MM_useMultiPositions= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}
					if (chunk.trim().startsWith("\"useChannels\":")) {
						diSPIM_MM_useChannels= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}
					if (chunk.trim().startsWith("\"channelMode\":")) {
						diSPIM_MM_channelMode= chunk.split(":")[1].replace("\"", "").trim();
					}
					if (chunk.trim().startsWith("\"numChannels\":")) {
						diSPIM_MM_numChannels= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
						cDim = diSPIM_MM_numChannels;
					}					 					
					diSPIM_MM_useChannel= new boolean[diSPIM_MM_numChannels];
						if (chunk.trim().startsWith("\"useChannel_\":")) {
							diSPIM_MM_useChannel[diSPIM_MM_channel_use_index]= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
							diSPIM_MM_channel_use_index++;					
						}
						diSPIM_MM_group= new String[diSPIM_MM_numChannels];
						if (chunk.trim().startsWith("\"group_\":")) {
							diSPIM_MM_group[diSPIM_MM_channel_group_index]= chunk.split(":")[1].replace("\"", "").trim();
							diSPIM_MM_channel_group_index++;
						}
						diSPIM_MM_config= new String[diSPIM_MM_numChannels];
						if (chunk.trim().startsWith("\"config_\":")) {
							diSPIM_MM_config[diSPIM_MM_channel_config_index]= chunk.split(":")[1].replace("\"", "").trim();
							diSPIM_MM_channel_config_index++;
						}
					
					
					if (chunk.trim().startsWith("\"channelGroup\":")) {
						diSPIM_MM_channelGroup= chunk.split(":")[1].replace("\"", "").trim();
					}
					if (chunk.trim().startsWith("\"useAutofocus\":")) {
						diSPIM_MM_useAutofocus= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}
					if (chunk.trim().startsWith("\"numSides\":")) {
						diSPIM_MM_numSides= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
						vDim = diSPIM_MM_numSides;
					}
					if (chunk.trim().startsWith("\"firstSideIsA\":")) {
						diSPIM_MM_firstSideIsA= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}
					if (chunk.trim().startsWith("\"delayBeforeSide\":")) {
						diSPIM_MM_delayBeforeSide= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"numSlices\":")) {
						diSPIM_MM_numSlices= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
						zDim = diSPIM_MM_numSlices;
					}
					if (chunk.trim().startsWith("\"stepSizeUm\":")) {
						diSPIM_MM_stepSizeUm= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"minimizeSlicePeriod\":")) {
						diSPIM_MM_minimizeSlicePeriod= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}
					if (chunk.trim().startsWith("\"desiredSlicePeriod\":")) {
						diSPIM_MM_desiredSlicePeriod= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"desiredLightExposure\":")) {
						diSPIM_MM_desiredLightExposure= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"centerAtCurrentZ\":")) {
						diSPIM_MM_centerAtCurrentZ= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}					 									

					if (chunk.trim().startsWith("\"scanDelay\":")) {
						diSPIM_MM_scanDelay= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"scanNum\":")) {
						diSPIM_MM_scanNum= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"scanPeriod\":")) {
						diSPIM_MM_scanPeriod= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"laserDelay\":")) {
						diSPIM_MM_laserDelay= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"laserDuration\":")) {
						diSPIM_MM_laserDuration= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"cameraDelay\":")) {
						diSPIM_MM_cameraDelay= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"cameraDuration\":")) {
						diSPIM_MM_cameraDuration= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"cameraExposure\":")) {
						diSPIM_MM_cameraExposure= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"sliceDuration\":")) {
						diSPIM_MM_sliceDuration= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"valid\":")) {
						diSPIM_MM_valid= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}
					if (chunk.trim().startsWith("\"cameraMode\":")) {
						diSPIM_MM_cameraMode= chunk.split(":")[1].replace("\"", "").trim();
					}
					if (chunk.trim().startsWith("\"hardwareTimepoints\":")) {
						diSPIM_MM_useHardwareTimepoints= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}
					if (chunk.trim().startsWith("\"separateTimepoints\":")) {
						diSPIM_MM_useSeparateTimepoints= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
					}
					if (chunk.trim().startsWith("\"Position_X\":")) {
						diSPIM_MM_Position_X= chunk.split(":")[1].replace("\"", "").trim();
					}
					if (chunk.trim().startsWith("\"Position_Y\":")) {
						diSPIM_MM_Position_Y= chunk.split(":")[1].replace("\"", "").trim();
					}
					if (chunk.trim().startsWith("\"Date\":")) {
						diSPIM_MM_Date= chunk.split(":")[1].replace("\"", "").trim();
					}
					if (chunk.trim().startsWith("\"MetadataVersion\":")) {
						diSPIM_MM_MetadataVersion= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"Width\":")) {
						diSPIM_MM_Width= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
					}
					if (chunk.trim().startsWith("\"PixelAspect\":")) {
						diSPIM_MM_PixelAspect= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
					}

						if (chunk.trim().startsWith("\"ChNames\":")) {
							diSPIM_MM_ChNames= chunk.split(":")[1].replace("[", "").replace("]", "").replace("\"", "").split(";");
							for(String s:diSPIM_MM_ChNames)
								IJ.log(s);
						}
					
					
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_Height= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_SlicePeriod_ms= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_GridColumn= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_PixelSize_um= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
//						vWidth = diSPIM_MM_PixelSize_um;
//						vHeight = diSPIM_MM_PixelSize_um;
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_Frames= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_Source= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_Channels= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_AcqusitionName= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_NumberOfSides= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_SPIMmode= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					
//					
						if (chunk.trim().startsWith("\"ChColors\":")) {
							diSPIM_MM_ChColorStrings= chunk.split(":")[1].replace("[", "").replace("]", "").replace("\"", "").split(";");
							diSPIM_MM_ChColors = new int[diSPIM_MM_ChColorStrings.length];
							for(int ccs=0;ccs<diSPIM_MM_ChColorStrings.length;ccs++) {
								IJ.log(diSPIM_MM_ChColorStrings[ccs]);
								diSPIM_MM_ChColors[ccs] = Integer.parseInt(diSPIM_MM_ChColorStrings[ccs]);
							}
						}
					
//							diSPIM_MM_ChColors[0]= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//				
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_Slices= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_UserName= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_Depth= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_PixelType= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_Time= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_FirstSide= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_zStep_um= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
//						vDepthRaw = diSPIM_MM_zStep_um;
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_SlicesFirst= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
//					}
//					
//					for (int diSPIM_MM_channel=0; diSPIM_MM_channel<diSPIM_MM_numChannels; diSPIM_MM_channel++) {
//						if (chunk.trim().startsWith("\"spimMode\":")) {
//							diSPIM_MM_ChContrastMin[diSPIM_MM_channel]= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//						}
//					}
//					
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_StartTime= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_MVRotationAxis= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_MicroManagerVersion= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_IJType= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_GridRow= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_VolumeDuration= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_NumComponents= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_Position_SPIM_Head= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_BitDepth= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_ComputerName= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_CameraMode= chunk.split(":")[1].replace("\"", "").trim();
//					}
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_TimeFirst= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
//					}
//
//					for (int diSPIM_MM_channel=0; diSPIM_MM_channel<diSPIM_MM_numChannels; diSPIM_MM_channel++) {
//						if (chunk.trim().startsWith("\"spimMode\":")) {
//							diSPIM_MM_ChContrastMax[diSPIM_MM_channel]= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//						}
//					}
//					
//					if (chunk.trim().startsWith("\"spimMode\":")) {
//						diSPIM_MM_Positions= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
//						pDim = diSPIM_MM_Positions;
//					}

					//					if (chunk.trim().split(":").length>1)
					//						allVars = allVars+"\n diSPIM_MM_"+chunk.trim().split(":")[1];
				}
//				IJ.log(allVars);
				
				//Reading in diSPIM header from MM tiffs ^^^^^^

				impAs = new ImagePlus[pDim];
				impBs = new ImagePlus[pDim];

				MultiFileInfoVirtualStack[] stackAs = new MultiFileInfoVirtualStack[pDim];
				MultiFileInfoVirtualStack[] stackBs = new MultiFileInfoVirtualStack[pDim];

				for (int pos=0; pos<pDim; pos++) {
					impAs[pos] = new ImagePlus();
					impAs[pos].setTitle(dirOrOMETiffFile.getName() +"_Pos"+pos+ ": SPIMA");
					impBs[pos] = new ImagePlus();
					impBs[pos].setTitle(dirOrOMETiffFile.getName() +"_Pos"+pos+ ": SPIMB");

					impAs[pos].setFileInfo(new FileInfo());
					impAs[pos].getOriginalFileInfo().fileName = dirOrOMETiff;
					impAs[pos].getOriginalFileInfo().directory = dirOrOMETiff;

					impBs[pos].setFileInfo(new FileInfo());
					impBs[pos].getOriginalFileInfo().fileName = dirOrOMETiff;
					impBs[pos].getOriginalFileInfo().directory = dirOrOMETiff;

					stackAs[pos] = new MultiFileInfoVirtualStack(
							dirOrOMETiff, keyString, cDim, zDim, tDim, vDim, pos,
							false, false);
					stackBs[pos] = new MultiFileInfoVirtualStack(
							dirOrOMETiff, keyString, cDim, zDim, tDim, vDim, pos,
							true, false);
					
					impAs[pos].setStack(stackAs[pos]);
					Calibration calA = impAs[pos].getCalibration();
					calA.pixelWidth = vWidth;
					calA.pixelHeight = vHeight;
					calA.pixelDepth = vDepthRaw;
					calA.setUnit(vUnit);

					stackAs[pos].setDimOrder(diSPIM_MM_channelMode=="VOLUME_HW"?"xyczt":"xyczt");
					if (stageScan)
						stackAs[pos].setSkewXperZ(
							calA.pixelDepth / calA.pixelWidth);
					impAs[pos].setOpenAsHyperStack(true);
					impAs[pos].setDimensions(cDim, zDim, stackAs[pos].getSize()/(cDim*zDim));
					impAs[pos] = new CompositeImage(impAs[pos]);
					while (!impAs[pos].isComposite()) {
						IJ.wait(100);
					}
					((CompositeImage)impAs[pos]).setMode(CompositeImage.COMPOSITE);


					impBs[pos].setStack(stackBs[pos]);
					Calibration calB = impBs[pos].getCalibration();
					calB.pixelWidth = vWidth;
					calB.pixelHeight = vHeight;
					calB.pixelDepth = vDepthRaw;
					calB.setUnit(vUnit);

					stackBs[pos].setDimOrder(diSPIM_MM_channelMode=="VOLUME_HW"?"xyczt":"xyczt");
					if (stageScan)
						stackBs[pos].setSkewXperZ(
							-calB.pixelDepth / calB.pixelWidth);
					impBs[pos].setOpenAsHyperStack(true);
					impBs[pos].setDimensions(cDim, zDim, stackBs[pos].getSize()/(cDim*zDim));
					impBs[pos] = new CompositeImage(impBs[pos]);
					while (!impBs[pos].isComposite()) {
						IJ.wait(100);
					}
					((CompositeImage)impBs[pos]).setMode(CompositeImage.COMPOSITE);

					impAs[pos].show();
					impBs[pos].show();
				}
			} else {
				fileListA = new File("" + dirOrOMETiff + "SPIMA").list();
				fileListB = new File("" + dirOrOMETiff + "SPIMB").list();
				fileRanksA = Arrays.copyOf(fileListA, fileListA.length);
				fileRanksB = Arrays.copyOf(fileListB, fileListB.length);
				fileNumsA = Arrays.copyOf(fileListA, fileListA.length);
				fileNumsB = Arrays.copyOf(fileListB, fileListB.length);
				fileSortA = Arrays.copyOf(fileListA, fileListA.length);
				fileSortB = Arrays.copyOf(fileListB, fileListB.length);

				for (int a = 0; a < fileListA.length; a++) {
					if (!fileListA[a].endsWith(".roi")
							&& !fileListA[a].endsWith(".DS_Store")) {
						String sring = fileListA[a].replace("/", "");
						String subsring = sring;
						String prefix = "";
						double n = Double.NaN;
						try {
							n = Integer.parseInt(subsring);
						} catch (NumberFormatException e) {
							n = Double.NaN;
						}
						while (Double.isNaN(n)) {
							try {
								prefix = prefix + subsring.substring(0, 1);
								subsring = subsring.substring(1);
								n = Integer.parseInt(subsring.split(" ")[0]);
								IJ.log(subsring);
								IJ.log(prefix);
							} catch (NumberFormatException ne) {
								n = Double.NaN;
							} catch (StringIndexOutOfBoundsException se) {
								n = Double.NaN;
							}
						}
						if (prefix.toLowerCase().startsWith("t")
								|| prefix.toLowerCase().startsWith("f"))
							prefix = "aaaaa" + prefix;
						int numer = Integer.parseInt(subsring.split(" ")[0]);
						IJ.log(subsring + " " + numer);
						fileNumsA[a] = prefix + IJ.pad(numer, 6) + "|" + sring;
						fileNumsB[a] = prefix + IJ.pad(numer, 6) + "|" + sring;
					} else {
						fileNumsA[a] = "";
						fileNumsB[a] = "";
					}

				}
				Arrays.sort(fileNumsA);
				Arrays.sort(fileNumsB);

				for (int r = 0; r < fileNumsA.length; r++) {
					String[] splt = fileNumsA[r].split("\\|");
					if (splt.length > 1)
						fileSortA[r] = splt[1];
					else
						fileSortA[r] = "";
					IJ.log(r + " " + " " + fileNumsA[r] + " " + fileSortA[r]);
					splt = fileNumsB[r].split("\\|");
					if (splt.length > 1)
						fileSortB[r] = splt[1];
					else
						fileSortB[r] = "";

				}

				for (int d = 0; d < fileSortA.length; d++) {
					boolean skipIt = false;
					String nextPathA = dirOrOMETiff + "SPIMA" + File.separator
							+ fileSortA[d];
					String nextPathB = dirOrOMETiff + "SPIMB" + File.separator
							+ fileSortB[d];
					IJ.log(nextPathA);
					IJ.log(nextPathB);
					if ((new File(nextPathA)).isDirectory()
							&& (new File(nextPathB)).isDirectory()) {
						newTifListA = (new File(nextPathA)).list();
						newTifListB = (new File(nextPathB)).list();
						if (newTifListA.length != newTifListB.length
								|| newTifListA.length < wavelengths * zSlices)
							skipIt = true;
						if (!skipIt) {
							Arrays.sort(newTifListA);
							for (int f = 0; f < newTifListA.length; f++) {
								while (!(new File(dirOrOMETiff
										+ "Big5DFileListA.txt")).exists())
									IJ.wait(100);
								if (!newTifListA[f].endsWith(".roi")
										&& !newTifListA[f]
												.endsWith(".DS_Store")
										&& big5DFileListAString
												.indexOf(nextPathA
														+ File.separator
														+ newTifListA[f]) < 0)
									IJ.append(nextPathA + File.separator
											+ newTifListA[f], dirOrOMETiff
											+ "Big5DFileListA.txt");
							}
							Arrays.sort(newTifListB);
							for (int f = 0; f < newTifListB.length; f++) {
								while (!(new File(dirOrOMETiff
										+ "Big5DFileListB.txt")).exists())
									IJ.wait(100);
								if (!newTifListB[f].endsWith(".roi")
										&& !newTifListB[f]
												.endsWith(".DS_Store")
										&& big5DFileListBString
												.indexOf(nextPathB
														+ File.separator
														+ newTifListB[f]) < 0)
									IJ.append(nextPathB + File.separator
											+ newTifListB[f], dirOrOMETiff
											+ "Big5DFileListB.txt");
							}
						}
					}

				}

				IJ.log("" + WindowManager.getImageCount());

				if ((new File(dirOrOMETiff + "Big5DFileListA.txt")).length() > 0) {
					// IJ.run("Stack From List...",
					// "open="+dir+"Big5DFileListA.txt use");
					impA = impAs[0];
					impA.setStack(new ListVirtualStack(dirOrOMETiff
							+ "Big5DFileListA.txt"));

					int stkNSlices = impA.getNSlices();

					impA.setTitle("SPIMA: " + dirOrOMETiff);

					impA.setDimensions(wavelengths, zSlices, stkNSlices
							/ (wavelengths * zSlices));
					IJ.log(wavelengths + " " + zSlices + " " + stkNSlices
							/ (wavelengths * zSlices));
					if (wavelengths > 1) {
						impA = new CompositeImage(impA);
						while (!impA.isComposite()) {
							IJ.wait(100);
						}
					}
					Calibration cal = impA.getCalibration();
					cal.pixelWidth = vWidth;
					cal.pixelHeight = vHeight;
					cal.pixelDepth = vDepthRaw;
					cal.setUnit(vUnit);
					if (stageScan)
						impA.getStack().setSkewXperZ(
								-cal.pixelDepth / cal.pixelWidth);

					impA.setPosition(wavelengths, zSlices / 2, stkNSlices
							/ (wavelengths * zSlices));

					impA.setPosition(1, zSlices / 2, stkNSlices
							/ (wavelengths * zSlices));

					if (impA.isComposite())
						((CompositeImage) impA)
								.setMode(CompositeImage.COMPOSITE);
					impA.show();

				}

				if ((new File(dirOrOMETiff + "Big5DFileListB.txt")).length() > 0) {
					// IJ.run("Stack From List...",
					// "open="+dir+"Big5DFileListB.txt use");
					impB = impBs[0];
					impB.setStack(new ListVirtualStack(dirOrOMETiff
							+ "Big5DFileListB.txt"));
					int stkNSlices = impB.getNSlices();

					impB.setTitle("SPIMB: " + dirOrOMETiff);

					impB.setDimensions(wavelengths, zSlices, stkNSlices
							/ (wavelengths * zSlices));
					IJ.log(wavelengths + " " + zSlices + " " + stkNSlices
							/ (wavelengths * zSlices));
					if (wavelengths > 1) {
						impB = new CompositeImage(impB);
						while (!impB.isComposite()) {
							IJ.wait(100);
						}
					}
					Calibration cal = impB.getCalibration();
					cal.pixelWidth = vWidth;
					cal.pixelHeight = vHeight;
					cal.pixelDepth = vDepthRaw;
					cal.setUnit(vUnit);
					if (stageScan)
						impB.getStack().setSkewXperZ(
								-cal.pixelDepth / cal.pixelWidth);

					impB.setPosition(wavelengths, zSlices / 2, stkNSlices
							/ (wavelengths * zSlices));

					impB.setPosition(1, zSlices / 2, stkNSlices
							/ (wavelengths * zSlices));

					if (impB.isComposite())
						((CompositeImage) impB)
								.setMode(CompositeImage.COMPOSITE);
					impB.show();

				}
			}
		} else if (dirOrOMETiff.endsWith(".ome.tif")) {
			TiffDecoder tdA = new TiffDecoder("",dirOrOMETiff);
			TiffDecoder tdB = new TiffDecoder("",dirOrOMETiff);

			String mmPath = (new File(dirOrOMETiff)).getParent();			

			impA = new ImagePlus();
			impA.setStack(new MultiFileInfoVirtualStack(mmPath, "MMStack", false));
			//				impA.setStack(new FileInfoVirtualStack(tdB.getTiffInfo(), false));
			int stackSize = impA.getNSlices();
			int nChannels = wavelengths*2;
			int nSlices = zSlices;
			int nFrames = (int)Math.floor((double)stackSize/(nChannels*nSlices));
			dirOrOMETiff = ((MultiFileInfoVirtualStack)impA.getStack()).getFivStacks().get(0).getInfo()[0].directory +
					File.separator +
					((MultiFileInfoVirtualStack)impA.getStack()).getFivStacks().get(0).getInfo()[0].fileName;

			impA.setTitle("SPIMB: "+dirOrOMETiff);

			if (nChannels*nSlices*nFrames!=stackSize) {
				if (nChannels*nSlices*nFrames>stackSize) {
					for (int a=stackSize;a<nChannels*nSlices*nFrames;a++) {
						if (impA.getStack().isVirtual())
							((VirtualStack)impA.getStack()).addSlice("blank slice");
						else
							impA.getStack().addSlice(impA.getProcessor().createProcessor(impA.getWidth(), impA.getHeight()));
					}
				} else if (nChannels*nSlices*nFrames<stackSize) {
					for (int a=nChannels*nSlices*nFrames;a<stackSize;a++) {
						((MultiFileInfoVirtualStack)impA.getStack()).deleteSlice(nChannels*nSlices*nFrames);
						stackSize--;
					}
				}else {
					IJ.error("HyperStack Converter", "channels x slices x frames <> stack size");
					return;
				}
			}
			boolean channelSwitchVolume = dirOrOMETiff.contains("_CSV.ome.tif");
			if (channelSwitchVolume ) {
				for (int t=nFrames-1;t>=0;t--) {
					for (int c=nChannels-1;c>=1;c=c-2) {
						for (int s=c*nSlices-1;s>=(c-1)*nSlices;s--) {
							int target = t*nChannels*nSlices + s+1;
							((MultiFileInfoVirtualStack)impA.getStack()).deleteSlice(target);
						}
					}
				}
			} else {
				for (int t=nFrames-1;t>=0;t--) {
					for (int s=nSlices*nChannels-1;s>=0;s--) {
						int target = t*nChannels*nSlices + s+1;
						if (s<nSlices*nChannels/2) { 
							((MultiFileInfoVirtualStack)impA.getStack()).deleteSlice(target);
						}
					}
				}
			}
			impA.setStack(impA.getImageStack());

			impA.setDimensions(wavelengths, nSlices, nFrames);

			if (nChannels > 1){
				impA = new CompositeImage(impA);
				while (!impA.isComposite()) {
					IJ.wait(100);
				}
			}
			Calibration cal = impA.getCalibration();
			cal.pixelWidth = vWidth;
			cal.pixelHeight = vHeight;
			cal.pixelDepth = vDepthRaw;
			cal.setUnit(vUnit);

			impA.setPosition(wavelengths, nSlices, nFrames);	

			impA.setPosition(1, nSlices/2, nFrames/2);	

			if (impA.isComposite())
				((CompositeImage)impA).setMode(CompositeImage.COMPOSITE);
			impA.setFileInfo(new FileInfo());
			impA.getOriginalFileInfo().fileName = dirOrOMETiff;
			impA.getOriginalFileInfo().directory = dirOrOMETiff;
			impA.show();


			impB = new ImagePlus();
			impB.setStack(new MultiFileInfoVirtualStack(mmPath, "MMStack", false));
			//				impB.setStack(new FileInfoVirtualStack(tdA.getTiffInfo(), false));
			stackSize = impB.getStack().getSize();
			nChannels = wavelengths*2;
			nSlices = zSlices;
			nFrames = (int)Math.floor((double)stackSize/(nChannels*nSlices));

			impB.setTitle("SPIMA: "+dirOrOMETiff);

			if (nChannels*nSlices*nFrames!=stackSize) {
				if (nChannels*nSlices*nFrames>stackSize) {
					for (int a=stackSize;a<nChannels*nSlices*nFrames;a++) {
						if (impB.getStack().isVirtual())
							((VirtualStack)impB.getStack()).addSlice("blank slice");
						else
							impB.getStack().addSlice(impB.getProcessor().createProcessor(impB.getWidth(), impB.getHeight()));
					}
				} else if (nChannels*nSlices*nFrames<stackSize) {
					for (int a=nChannels*nSlices*nFrames;a<stackSize;a++) {
						((MultiFileInfoVirtualStack)impB.getStack()).deleteSlice(nChannels*nSlices*nFrames);
						stackSize = impB.getStack().getSize();					}
				}else {
					IJ.error("HyperStack Converter", "channels x slices x frames <> stack size");
					return;
				}
			}
			if (channelSwitchVolume ) {
				for (int t=nFrames-1;t>=0;t--) {
					for (int c=nChannels;c>=1;c=c-2) {
						for (int s=c*nSlices-1;s>=(c-1)*nSlices;s--) {
							int target = t*nChannels*nSlices + s+1;
							((MultiFileInfoVirtualStack)impB.getStack()).deleteSlice(target);
						}
					}
				}
			} else {
				for (int t=nFrames-1;t>=0;t--) {
					for (int s=nSlices*nChannels-1;s>=0;s--) {
						int target = t*nChannels*nSlices + s+1;
						if (s>=nSlices*nChannels/2) { 
							((MultiFileInfoVirtualStack)impB.getStack()).deleteSlice(target);
						}
					}
				}
			}

			impB.setStack(impB.getImageStack());

			impB.setDimensions(wavelengths, nSlices, nFrames);

			if (nChannels > 1){
				impB = new CompositeImage(impB);
				while (!impB.isComposite()) {
					IJ.wait(100);
				}
			}
			cal = impB.getCalibration();
			cal.pixelWidth = vWidth;
			cal.pixelHeight = vHeight;
			cal.pixelDepth = vDepthRaw;
			cal.setUnit(vUnit);

			impB.setPosition(wavelengths, nSlices, nFrames);	

			impB.setPosition(1, nSlices/2, nFrames/2);	

			if (impB.isComposite())
				((CompositeImage)impB).setMode(CompositeImage.COMPOSITE);
			impB.setFileInfo(new FileInfo());
			impB.getOriginalFileInfo().fileName = dirOrOMETiff;
			impB.getOriginalFileInfo().directory = dirOrOMETiff;
			impB.show();

		} else if (dirOrOMETiff.matches(".*_\\d{9}_\\d{3}_.*.tif")) {
			listB = new File(dirOrOMETiff).getParentFile().list();
			int newLength = 0;
			for (String newFileListItem : listB)
				if (newFileListItem.endsWith(".tif"))
					newLength++;

			while (Math.floor(newLength / (wavelengths * 2 * zSlices)) == 0) {

				IJ.wait(10);
				listB = new File(dirOrOMETiff).getParentFile().list();
				newLength = 0;
				for (String newFileListItem : listB)
					if (newFileListItem.endsWith(".tif"))
						newLength++;
			}
			IJ.run("Image Sequence...",
					"open=["
							+ dirOrOMETiff
							+ "] number="
							+ newLength
							+ " starting=1 increment=1 scale=100 file=Cam2 or=[] sort use");
			IJ.run("Stack to Hyperstack...",
					"order=xyczt(default) channels="
							+ wavelengths
							+ " slices="
							+ zSlices
							+ " frames="
							+ (Math.floor(newLength
									/ (wavelengths * 2 * zSlices)))
							+ " display=Composite");
			// IJ.getImage().setTitle("SPIMA: "+IJ.getImage().getTitle());
			impA = impAs[0] = WindowManager.getCurrentImage();
			Calibration calA = impA.getCalibration();
			calA.pixelWidth = vWidth;
			calA.pixelHeight = vHeight;
			calA.pixelDepth = vDepthRaw;
			calA.setUnit(vUnit);
			if (stageScan)
				impA.getStack()
						.setSkewXperZ(-calA.pixelDepth / calA.pixelWidth);
			impA.setTitle("SPIMA: " + impA.getTitle());

			IJ.run("Image Sequence...",
					"open=["
							+ dirOrOMETiff
							+ "] number="
							+ newLength
							+ " starting=1 increment=1 scale=100 file=Cam1 or=[] sort use");
			IJ.run("Stack to Hyperstack...",
					"order=xyczt(default) channels="
							+ wavelengths
							+ " slices="
							+ zSlices
							+ " frames="
							+ (Math.floor(newLength
									/ (wavelengths * 2 * zSlices)))
							+ " display=Composite");
			// IJ.getImage().setTitle("SPIMB: "+IJ.getImage().getTitle());
			impB = impBs[0] = WindowManager.getCurrentImage();
			Calibration calB = impB.getCalibration();
			calB.pixelWidth = vWidth;
			calB.pixelHeight = vHeight;
			calB.pixelDepth = vDepthRaw;
			calB.setUnit(vUnit);
			if (stageScan)
				impB.getStack().setSkewXperZ(calB.pixelDepth / calB.pixelWidth);
			impB.setTitle("SPIMB: " + impB.getTitle());

			oldLength = newLength;
		}

		IJ.run("Tile");
		IJ.log("" + WindowManager.getImageCount());

		SelectKeyChannelDialog d = new SelectKeyChannelDialog(
				IJ.getInstance(),
				"Deconvolve while aquiring?",
				"Would you like volumes to be deconvolved/fused \nas soon as they are captured?  \n\nChoose this option if you are ready \nto initiate time-lapse recording.");
		// d.setVisible(true);
		if (d.cancelPressed()) {
			doDecon = false;
		} else if (d.yesPressed()) {
			doDecon = true;
			keyChannel = d.getKeyChannel();
			slaveChannel = keyChannel == 1 ? 2 : 1;
		} else
			doDecon = false;

			if (doDecon) {
				
				for (int pos=0; pos<pDim; pos++) {

					impA = impAs[pos];
					impB = impBs[pos];

					Roi roiA = impA.getRoi();
					Roi roiB = impB.getRoi();

					while (roiA == null || roiB == null
							// || (roiA.getType() == Roi.RECTANGLE &&
							// roiA.getBounds().getHeight() > cropHeight)
							// || (roiB.getType() == Roi.RECTANGLE &&
							// roiB.getBounds().getHeight() > cropHeight)
							// || (roiA.getType() == Roi.RECTANGLE &&
							// roiA.getBounds().getWidth() > cropHeight)
							// || (roiB.getType() == Roi.RECTANGLE &&
							// roiB.getBounds().getWidth() > cropHeight)
							// || (roiA.getType() != Roi.RECTANGLE &&
							// roiA.getFeretValues()[0]>cropHeight*impA.getCalibration().pixelHeight)
							// || (roiB.getType() != Roi.RECTANGLE &&
							// roiB.getFeretValues()[0]>cropHeight*impB.getCalibration().pixelHeight)
							) {
						WindowManager.setTempCurrentImage(impA);
						if (roiA == null) {
							if (!((new File(savePath +  "Pos" + pos + "A_crop.roi")).canRead())) {
								IJ.makeRectangle(0, 0, cropWidth, cropHeight);
							} else {
								IJ.open(savePath +  "Pos" + pos + "A_crop.roi");
								roiA = impA.getRoi();
								cropWidth = roiA.getBounds().width;
								cropHeight = roiA.getBounds().height;
							}
						} else if (roiA.getType() != Roi.RECTANGLE
								&& roiA.getFeretValues()[0] > cropHeight
								* impA.getCalibration().pixelHeight
								|| (roiA.getType() == Roi.RECTANGLE && roiA.getBounds()
								.getHeight() > cropHeight)
								|| (roiA.getType() == Roi.RECTANGLE && roiA.getBounds()
								.getWidth() > cropHeight)) {
							impA.setRoi(roiA.getBounds().x
									+ (roiA.getBounds().width - cropWidth) / 2,
									roiA.getBounds().y
									+ (roiA.getBounds().height - cropHeight)
									/ 2, cropWidth, cropHeight);
							impA.setRoi(
									roiA.getBounds().x < 0 ? 0 : roiA.getBounds().x,
											roiA.getBounds().y < 0 ? 0 : roiA.getBounds().y,
													cropWidth, cropHeight);
						}
						WindowManager.setTempCurrentImage(impB);
						if (roiB == null) {
							if (!((new File(savePath + "Pos" + pos + "B_crop.roi")).canRead())) {
								IJ.makeRectangle(0, 0, cropWidth, cropHeight);
							} else {
								IJ.open(savePath +  "Pos" + pos + "B_crop.roi");
								roiB = impB.getRoi();
							}
						} else if (roiB.getType() != Roi.RECTANGLE
								&& roiB.getFeretValues()[0] > cropHeight
								* impB.getCalibration().pixelHeight
								|| (roiB.getType() == Roi.RECTANGLE && roiB.getBounds()
								.getWidth() > cropHeight)
								|| (roiB.getType() == Roi.RECTANGLE && roiB.getBounds()
								.getHeight() > cropHeight)) {
							impB.setRoi(roiB.getBounds().x
									+ (roiB.getBounds().width - cropWidth) / 2,
									roiB.getBounds().y
									+ (roiB.getBounds().height - cropHeight)
									/ 2, cropWidth, cropHeight);
							impB.setRoi(
									roiB.getBounds().x < 0 ? 0 : roiB.getBounds().x,
											roiB.getBounds().y < 0 ? 0 : roiB.getBounds().y,
													cropWidth, cropHeight);
						}
						WindowManager.setTempCurrentImage(null);

						IJ.runMacro("waitForUser(\"Select the regions containing the embryo"
								+ "\\\n for deconvolution/fusion processing."
								// + "\\\nAlso, set the minimum Brightness limit."
								// +
								// "\\\nWhen you are then ready, click OK here to commence processing."
								+ "\");");
						roiA = impA.getRoi();
						roiB = impB.getRoi();
					}

					// int[] minLimit = {(int) impA.getDisplayRangeMin(), (int)
					// impB.getDisplayRangeMin()};
					// if (impA.isComposite()) {
					// minLimit = new int[impA.getNChannels()*2];
					// for (int c=1; c<=impA.getNChannels(); c++) {
					// minLimit[c-1] = (int)
					// ((CompositeImage)impA).getProcessor(c).getMin();
					// minLimit[c+1] = (int)
					// ((CompositeImage)impB).getProcessor(c).getMin();
					// }
					// }

					IJ.saveAs(impA, "Selection", savePath +  "Pos" + pos + "A_crop.roi");
					IJ.saveAs(impB, "Selection", savePath +  "Pos" + pos + "B_crop.roi");

				}
				
				
				for (int pos=0; pos<pDim; pos++) {

					impA = impAs[pos];
					impB = impBs[pos];
					
					Roi roiA = impA.getRoi();
					Roi roiB = impB.getRoi();

					int wasFrameA = impA.getFrame();
					int wasFrameB = impB.getFrame();
					int wasSliceA = impA.getSlice();
					int wasSliceB = impB.getSlice();
					int wasChannelA = impA.getChannel();
					int wasChannelB = impB.getChannel();

					if ((new File(savePath)).canRead()) {
						if (impDF1 == null) {
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+"Pos"+pos+ "_Deconvolution1\");");
							if (wavelengths == 2) {
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+"Pos"+pos+ "_Deconvolution2\");");
							}
							MultiFileInfoVirtualStack deconmfivs = new MultiFileInfoVirtualStack(
									((new File(dirOrOMETiff)).isDirectory() ? dirOrOMETiff
											: (new File(dirOrOMETiff)).getParent())
											+ File.separator, "Deconvolution",
											false);
							if (deconmfivs.getSize() > 0) {
								impDF1 = new ImagePlus();
								impDF1.setStack(
										"Decon-Fuse"
												+ impA.getTitle().replace(
														impA.getTitle().split(":")[0],
														""), deconmfivs);
								impDF1.setFileInfo(new FileInfo());
								// impDF1.getOriginalFileInfo().directory = (new
								// File(dirOrOMETiff)).isDirectory()?dirOrOMETiff:((new
								// File(dirOrOMETiff)).getParent()+File.separator);
								impDF1.getOriginalFileInfo().directory = dirOrOMETiff;
								int stkNSlicesDF = impDF1.getStackSize();
								int zSlicesDF1 = deconmfivs.getFivStacks().get(0)
										.getSize();
								impDF1.setOpenAsHyperStack(true);
								impDF1.setStack(impDF1.getStack(), wavelengths,
										zSlicesDF1, stkNSlicesDF
										/ (wavelengths * zSlicesDF1));
								ciDF1 = new CompositeImage(impDF1);
								if (wavelengths > 1)
									ciDF1.setMode(CompositeImage.COMPOSITE);
								else
									ciDF1.setMode(CompositeImage.GRAYSCALE);
								ciDF1.show();
								win = ciDF1.getWindow();
							}
						}
					}

					String[] frameFileNames = new String[impA.getNFrames() + 1];

					for (int f = 1; f <= impA.getNFrames(); f++) {

						impA.setPositionWithoutUpdate(impA.getChannel(),
								impA.getSlice(), f);

						if (impA.getStack() instanceof ListVirtualStack)
							frameFileNames[f] = ((ListVirtualStack) impA.getStack())
							.getDirectory(impA.getCurrentSlice());
						else if (impA.getStack() instanceof FileInfoVirtualStack
								|| impA.getStack() instanceof MultiFileInfoVirtualStack)
							frameFileNames[f] = "t" + f;
						else
							frameFileNames[f] = "t" + f;
						String timecode = "" + (new Date()).getTime();

						if (!(new File(savePath + "Pos"+pos+ "_SPIMA_Ch1_processed"
								+ File.separator + frameFileNames[f] + File.separator
								+ frameFileNames[f] + ".tif")).canRead()
								|| (wavelengths == 2 && !(new File(savePath
										+ "Pos"+pos+ "_SPIMA_Ch2_processed" + File.separator
										+ frameFileNames[f] + File.separator
										+ frameFileNames[f] + ".tif")).canRead())
										|| !(new File(savePath + "Pos"+pos+ "_SPIMB_Ch1_processed"
												+ File.separator + frameFileNames[f]
														+ File.separator + frameFileNames[f] + ".tif"))
														.canRead()
														|| (wavelengths == 2 && !(new File(savePath
																+ "Pos"+pos+ "_SPIMA_Ch2_processed" + File.separator
																+ frameFileNames[f] + File.separator
																+ frameFileNames[f] + ".tif")).canRead())) {
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMA_Ch1_processed\");");
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMA_Ch1_processed\"+File.separator+\""
									+ frameFileNames[f] + "\");");
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMB_Ch1_processed\");");
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMB_Ch1_processed\"+File.separator+\""
									+ frameFileNames[f] + "\");");
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+"Pos"+pos+ "_Deconvolution1\");");
							if (wavelengths == 2) {
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMA_Ch2_processed\");");
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMA_Ch2_processed\"+File.separator+\""
										+ frameFileNames[f] + "\");");
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMB_Ch2_processed\");");
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMB_Ch2_processed\"+File.separator+\""
										+ frameFileNames[f] + "\");");
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+"Pos"+pos+ "_Deconvolution2\");");
							}

							// ImageStack stackA1 = new
							// ImageStack(cropHeight,cropWidth);
							// ImageStack stackA2 = new
							// ImageStack(cropHeight,cropWidth);
							ImageStack stackA1 = new ImageStack(cropWidth, cropHeight);
							ImageStack stackA2 = new ImageStack(cropWidth, cropHeight);
							impA.getWindow().setEnabled(false);
							for (int i = 1; i <= impA.getNSlices(); i++) {
								impA.setPositionWithoutUpdate(1, i, f);
								Roi impRoi = (Roi) roiA.clone();
								Polygon pA = new Polygon(impRoi.getPolygon().xpoints,
										impRoi.getPolygon().ypoints,
										impRoi.getPolygon().npoints);
								double fMax = impRoi.getBounds().width > impRoi
										.getBounds().height ? impRoi.getBounds().width
												: impRoi.getBounds().height;
										double angle = impRoi.getBounds().width > impRoi
												.getBounds().height ? 90 : 0;
										if (impRoi.getType() != Roi.RECTANGLE) {
											double[] fVals = impRoi.getFeretValues();
											fMax = fVals[0];
											angle = fVals[1];
										}
										// Polygon pAR = rotatePolygon(new
										// Polygon(pA.xpoints,pA.ypoints, pA.npoints),
										// -180+angle);
										Polygon pAR = pA;

										ImageProcessor ip1 = impA.getProcessor().duplicate();
										// ip1.fillOutside(impRoi);
										// ip1.setRoi((int)(pA.getBounds().getCenterX()-fMax/2),
										// (int)(pA.getBounds().getCenterY()-fMax/2),
										// (int)fMax,
										// (int)fMax);
										// ip1.rotate(-180+angle);

										// ip1.setRoi(Math.max((int)pAR.getBounds().x-(cropHeight-pAR.getBounds().width)/2,
										// 0),
										// Math.max((int)pAR.getBounds().y-(cropWidth-pAR.getBounds().height)/2,
										// 0),
										// cropHeight, cropWidth);
										ip1.setRoi(
												Math.max((int) pAR.getBounds().x
														- (cropWidth - pAR.getBounds().width)
														/ 2, 0),
														Math.max((int) pAR.getBounds().y
																- (cropHeight - pAR.getBounds().height)
																/ 2, 0), cropWidth, cropHeight);
										ip1 = ip1.crop();
										// ImageProcessor ip1r = ip1.createProcessor(cropHeight,
										// cropWidth);
										ImageProcessor ip1r = ip1.createProcessor(cropWidth,
												cropHeight);
										ip1r.insert(ip1, 0, 0);
										ip1 = ip1r;
										// ip1.subtract(minLimit[0]);
										stackA1.addSlice(ip1);
										if (wavelengths == 2) {
											impA.setPositionWithoutUpdate(2, i, f);
											ImageProcessor ip2 = impA.getProcessor()
													.duplicate();
											// ip2.fillOutside(impRoi);
											// ip2.setRoi((int)(pA.getBounds().getCenterX()-fMax/2),
											// (int)(pA.getBounds().getCenterY()-fMax/2),
											// (int)fMax,
											// (int)fMax);
											// ip2.rotate(-180+angle);

											// ip2.setRoi(Math.max((int)pAR.getBounds().x-(cropHeight-pAR.getBounds().width)/2,
											// 0),
											// Math.max((int)pAR.getBounds().y-(cropWidth-pAR.getBounds().height)/2,
											// 0),
											// cropHeight, cropWidth);
											ip2.setRoi(
													Math.max(
															(int) pAR.getBounds().x
															- (cropWidth - pAR
																	.getBounds().width)
																	/ 2, 0),
																	Math.max(
																			(int) pAR.getBounds().y
																			- (cropHeight - pAR
																					.getBounds().height)
																					/ 2, 0), cropWidth,
																					cropHeight);
											ip2 = ip2.crop();
											// ImageProcessor ip2r =
											// ip2.createProcessor(cropHeight, cropWidth);
											ImageProcessor ip2r = ip2.createProcessor(
													cropWidth, cropHeight);
											ip2r.insert(ip2, 0, 0);
											ip2 = ip2r;
											// ip2.subtract(minLimit[1]);
											stackA2.addSlice(ip2);
										}
							}
							impA.getWindow().setEnabled(true);
							ImagePlus impXA1 = new ImagePlus();
							impXA1.setStack(stackA1);
							impXA1.setCalibration(impA.getCalibration());
							// impXA1.getCalibration().pixelDepth =
							// impXA1.getCalibration().pixelWidth;
							IJ.saveAs(impXA1, "Tiff", savePath + "Pos"+pos+ "_SPIMA_Ch1_processed"
									+ File.separator + frameFileNames[f]
											+ File.separator + frameFileNames[f] + ".tif");
							if (wavelengths == 2) {
								ImagePlus impXA2 = new ImagePlus();
								impXA2.setStack(stackA2);
								impXA2.setCalibration(impA.getCalibration());
								// impXA2.getCalibration().pixelDepth =
								// impXA2.getCalibration().pixelWidth;
								IJ.saveAs(impXA2, "Tiff", savePath
										+ "Pos"+pos+ "_SPIMA_Ch2_processed" + File.separator
										+ frameFileNames[f] + File.separator
										+ frameFileNames[f] + ".tif");
							}

							// ImageStack stackB1 = new
							// ImageStack(cropHeight,cropWidth);
							// ImageStack stackB2 = new
							// ImageStack(cropHeight,cropWidth);
							ImageStack stackB1 = new ImageStack(cropWidth, cropHeight);
							ImageStack stackB2 = new ImageStack(cropWidth, cropHeight);
							impB.getWindow().setEnabled(false);
							for (int i = 1; i <= impB.getNSlices(); i++) {
								impB.setPositionWithoutUpdate(1, i, f);
								Roi impRoi = (Roi) roiB.clone();
								Polygon pB = new Polygon(impRoi.getPolygon().xpoints,
										impRoi.getPolygon().ypoints,
										impRoi.getPolygon().npoints);
								double fMax = impRoi.getBounds().width > impRoi
										.getBounds().height ? impRoi.getBounds().width
												: impRoi.getBounds().height;
										double angle = impRoi.getBounds().width > impRoi
												.getBounds().height ? 90 : 0;
										if (impRoi.getType() != Roi.RECTANGLE) {
											double[] fVals = impRoi.getFeretValues();
											fMax = fVals[0];
											angle = fVals[1];
										}
										// Polygon pBR = rotatePolygon(new
										// Polygon(pB.xpoints,pB.ypoints, pB.npoints),
										// -180+angle);
										Polygon pBR = pB;

										ImageProcessor ip1 = impB.getProcessor().duplicate();
										// ip1.fillOutside(impRoi);
										// ip1.setRoi((int)(pB.getBounds().getCenterX()-fMax/2),
										// (int)(pB.getBounds().getCenterY()-fMax/2),
										// (int)fMax,
										// (int)fMax);
										// ip1.rotate(-180+angle);

										// ip1.setRoi(Math.max((int)pBR.getBounds().x-(cropHeight-pBR.getBounds().width)/2,
										// 0),
										// Math.max((int)pBR.getBounds().y-(cropWidth-pBR.getBounds().height)/2,
										// 0),
										// cropHeight, cropWidth);
										ip1.setRoi(
												Math.max((int) pBR.getBounds().x
														- (cropWidth - pBR.getBounds().width)
														/ 2, 0),
														Math.max((int) pBR.getBounds().y
																- (cropHeight - pBR.getBounds().height)
																/ 2, 0), cropWidth, cropHeight);
										ip1 = ip1.crop();
										// ImageProcessor ip1r = ip1.createProcessor(cropHeight,
										// cropWidth);
										ImageProcessor ip1r = ip1.createProcessor(cropWidth,
												cropHeight);
										ip1r.insert(ip1, 0, 0);
										ip1 = ip1r;
										// ip1.subtract(minLimit[2]);
										stackB1.addSlice(ip1);
										if (wavelengths == 2) {
											impB.setPositionWithoutUpdate(2, i, f);
											ImageProcessor ip2 = impB.getProcessor()
													.duplicate();
											// ip2.fillOutside(impRoi);
											// ip2.setRoi((int)(pB.getBounds().getCenterX()-fMax/2),
											// (int)(pB.getBounds().getCenterY()-fMax/2),
											// (int)fMax,
											// (int)fMax);
											// ip2.rotate(-180+angle);

											// ip2.setRoi(Math.max((int)pBR.getBounds().x-(cropHeight-pBR.getBounds().width)/2,
											// 0),
											// Math.max((int)pBR.getBounds().y-(cropWidth-pBR.getBounds().height)/2,
											// 0),
											// cropHeight, cropWidth);
											ip2.setRoi(
													Math.max(
															(int) pBR.getBounds().x
															- (cropWidth - pBR
																	.getBounds().width)
																	/ 2, 0),
																	Math.max(
																			(int) pBR.getBounds().y
																			- (cropHeight - pBR
																					.getBounds().height)
																					/ 2, 0), cropWidth,
																					cropHeight);
											ip2 = ip2.crop();
											// ImageProcessor ip2r =
											// ip2.createProcessor(cropHeight, cropWidth);
											ImageProcessor ip2r = ip2.createProcessor(
													cropWidth, cropHeight);
											ip2r.insert(ip2, 0, 0);
											ip2 = ip2r;
											// ip2.subtract(minLimit[3]);
											stackB2.addSlice(ip2);
										}
							}
							impB.getWindow().setEnabled(true);
							ImagePlus impXB1 = new ImagePlus();
							impXB1.setStack(stackB1);
							impXB1.setCalibration(impB.getCalibration());
							// impXB1.getCalibration().pixelDepth =
							// impXB1.getCalibration().pixelWidth;
							IJ.saveAs(impXB1, "Tiff", savePath + "Pos"+pos+ "_SPIMB_Ch1_processed"
									+ File.separator + frameFileNames[f]
											+ File.separator + frameFileNames[f] + ".tif");
							if (wavelengths == 2) {
								ImagePlus impXB2 = new ImagePlus();
								impXB2.setStack(stackB2);
								impXB2.setCalibration(impB.getCalibration());
								// impXB2.getCalibration().pixelDepth =
								// impXB2.getCalibration().pixelWidth;
								IJ.saveAs(impXB2, "Tiff", savePath
										+ "Pos"+pos+ "_SPIMB_Ch2_processed" + File.separator
										+ frameFileNames[f] + File.separator
										+ frameFileNames[f] + ".tif");
							}

						}
					}

					final String[] frameFileNamesFinal = frameFileNames;

					impA.setPosition(wasChannelA, wasSliceA, wasFrameA);
					impB.setPosition(wasChannelB, wasSliceB, wasFrameB);

					for (int f = 1; f <= impA.getNFrames(); f++) {
						final int ff = f;

						final String timecode = "" + (new Date()).getTime();

						if (!(new File(savePath +"Pos"+pos+ "_Deconvolution1" + File.separator
								+ "Decon_" + frameFileNames[f] + ".tif")).canRead()
								|| (wavelengths == 2 && !(new File(savePath
										+"Pos"+pos+ "_Deconvolution2" + File.separator + "Decon_"
										+ frameFileNames[f] + ".tif")).canRead())) {
							String deconStringKey = "nibib.spim.PlugInDialogGenerateFusion(\"reg_one boolean false\", \"reg_all boolean true\", \"no_reg_2D boolean false\", \"reg_2D_one boolean false\", \"reg_2D_all boolean false\", \"rotate_begin list_float -10.0,-10.0,-10.0\", \"rotate_end list_float 10.0,10.0,10.0\", \"coarse_rate list_float 3.0,3.0,3.0\", \"fine_rate list_float 0.5,0.5,0.5\", \"save_arithmetic boolean false\", \"show_arithmetic boolean false\", \"save_geometric boolean false\", \"show_geometric boolean false\", \"do_interImages boolean false\", \"save_prefusion boolean false\", \"do_show_pre_fusion boolean false\", \"do_threshold boolean false\", \"save_max_proj boolean false\", \"show_max_proj boolean false\", \"x_max_box_selected boolean false\", \"y_max_box_selected boolean false\", \"z_max_box_selected boolean false\", \"do_smart_movement boolean false\", \"threshold_intensity double 10.0\", \"res_x double 0.1625\", \"res_y double 0.1625\", \"res_z double 1.0\", \"mtxFileDirectory string "
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMB_Ch"
									+ keyChannel
									+ "_processed"
									+ File.separator.replace("\\", "\\\\")
									+ frameFileNames[f]
											+ "\", \"spimAFileDir string "
											+ savePath.replace("\\", "\\\\")
											+ "Pos"+pos+ "_SPIMB_Ch"
											+ keyChannel
											+ "_processed"
											+ File.separator.replace("\\", "\\\\")
											+ frameFileNames[f]
													+ "\", \"spimBFileDir string "
													+ savePath.replace("\\", "\\\\")
													+ "Pos"+pos+ "_SPIMA_Ch"
													+ keyChannel
													+ "_processed"
													+ File.separator.replace("\\", "\\\\")
													+ frameFileNames[f]
															+ "\", \"baseImage string "
															+ frameFileNames[f]
																	+ "\", \"base_rotation int -1\", \"transform_rotation int 5\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
																	+ savePath.replace("\\", "\\\\")
																	+"Pos"+pos+ "_Deconvolution"
																	+ keyChannel
																	+ "\\\", \"deconv_show_results boolean false\", \"deconvolution_method int 1\", \"deconv_iterations int 10\", \"deconv_sigmaA list_float 3.5,3.5,9.6\", \"deconv_sigmaB list_float 9.6,3.5,3.5\", \"use_deconv_sigma_conversion_factor boolean true\", \"x_move int 0\", \"y_move int 0\", \"z_move int 0\", \"fusion_range string 1-1\")";
							IJ.wait(5000);

							new MacroRunner(
									"cpuPerformance = exec(\"cmd64\",\"/c\",\"typeperf \\\"\\\\Processor(_Total)\\\\% Processor Time\\\" -sc 1\");"
											+ "cpuChunks = split(cpuPerformance,\"\\\"\");"
											+ "x = parseFloat(cpuChunks[lengthOf(cpuChunks)-2]); "
											+ "while(x >30) {\n"
											+ "	wait(10000);"
											+ "	cpuPerformance = exec(\"cmd64\",\"/c\",\"typeperf \\\"\\\\Processor(_Total)\\\\% Processor Time\\\" -sc 1\");"
											+ "	cpuChunks = split(cpuPerformance,\"\\\"\");"
											+ "	x = parseFloat(cpuChunks[lengthOf(cpuChunks)-2]); "
											+ "}" + "print(\""
											+ frameFileNames[f]
													+ "_"
													+ keyChannel
													+ " processing...\");"
													+

									"			File.saveString(\'"
									+ deconStringKey
									+ "\', \""
									+ tempDir.replace("\\", "\\\\")
									+ "GenerateFusion1"
									+ frameFileNames[f]
											+ timecode
											+ ".sct\");"
											+

									"		    f = File.open(\""
									+ tempDir.replace("\\", "\\\\")
									+ "GenerateFusion1"
									+ frameFileNames[f]
											+ timecode
											+ ".bat\");\n"
											+ "		    batStringD = \"@echo off\";\n"
											+ "		    print(f,batStringD);\n"
											+ "		    batStringC = \"C\\:\";\n"
											+ "		    print(f,batStringC);\n"
											+ "		    batStringA = \"cd C:\\\\Program Files\\\\mipav\";\n"
											+ "		    print(f,batStringA);\n"
											+ "		    batStringB = \"cmd64 /c mipav -s \\\""
											+ tempDir.replace("\\", "\\\\")
											+ "GenerateFusion1"
											+ frameFileNames[f]
													+ timecode
													+ ".sct\\\" -hide\";\n"
													+ "		    print(f,batStringB);\n"
													+ "		    print(f,\"exit\");\n"
													+ "		    File.close(f);	    \n"
													+

									"batJob = exec(\"cmd64\", \"/c\", \"start\", \"/low\", \"/min\", \"/wait\", \""
									+ tempDir.replace("\\", "\\\\")
									+ "GenerateFusion1"
									+ frameFileNames[f]
											+ timecode + ".bat\");" + "");

							final String finalConvPath = savePath
									+"Pos"+pos+ "_Deconvolution1\\Decon_" + frameFileNames[f]
											+ ".tif";
							Thread convThread = new Thread(new Runnable() {
								public void run() {
									while (!(new File(finalConvPath)).canRead()) {
										IJ.wait(10000);
									}
									IJ.wait(30000);
									new MacroRunner("print(\""
											+ frameFileNamesFinal[ff] + "_"
											+ keyChannel + " complete.\");"
											+ "delBat = File.delete(\""
											+ tempDir.replace("\\", "\\\\")
											+ "GenerateFusion1"
											+ frameFileNamesFinal[ff] + timecode
											+ ".bat\");" + "delSct = File.delete(\""
											+ tempDir.replace("\\", "\\\\")
											+ "GenerateFusion1"
											+ frameFileNamesFinal[ff] + timecode
											+ ".sct\");");

									ImagePlus convImp = IJ.openImage(finalConvPath);
									if (convImp != null) {
										IJ.saveAs(convImp, "TIFF", finalConvPath);
										convImp.close();
									}
								}
							});
							convThread.start();

							if (wavelengths == 2) {
								String deconStringSlave = "nibib.spim.PlugInDialogGenerateFusion(\"reg_one boolean false\", \"reg_all boolean true\", \"no_reg_2D boolean false\", \"reg_2D_one boolean false\", \"reg_2D_all boolean false\", \"rotate_begin list_float -10.0,-10.0,-10.0\", \"rotate_end list_float 10.0,10.0,10.0\", \"coarse_rate list_float 3.0,3.0,3.0\", \"fine_rate list_float 0.5,0.5,0.5\", \"save_arithmetic boolean false\", \"show_arithmetic boolean false\", \"save_geometric boolean false\", \"show_geometric boolean false\", \"do_interImages boolean false\", \"save_prefusion boolean false\", \"do_show_pre_fusion boolean false\", \"do_threshold boolean false\", \"save_max_proj boolean false\", \"show_max_proj boolean false\", \"x_max_box_selected boolean false\", \"y_max_box_selected boolean false\", \"z_max_box_selected boolean false\", \"do_smart_movement boolean false\", \"threshold_intensity double 10.0\", \"res_x double 0.1625\", \"res_y double 0.1625\", \"res_z double 1.0\", \"mtxFileDirectory string "
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMB_Ch"
										+ keyChannel
										+ "_processed"
										+ File.separator.replace("\\", "\\\\")
										+ frameFileNames[f]
												+ "\", \"spimAFileDir string "
												+ savePath.replace("\\", "\\\\")
												+ "Pos"+pos+ "_SPIMB_Ch"
												+ slaveChannel
												+ "_processed"
												+ File.separator.replace("\\", "\\\\")
												+ frameFileNames[f]
														+ "\", \"spimBFileDir string "
														+ savePath.replace("\\", "\\\\")
														+ "Pos"+pos+ "_SPIMA_Ch"
														+ slaveChannel
														+ "_processed"
														+ File.separator.replace("\\", "\\\\")
														+ frameFileNames[f]
																+ "\", \"baseImage string "
																+ frameFileNames[f]
																		+ "\", \"base_rotation int -1\", \"transform_rotation int 5\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
																		+ savePath.replace("\\", "\\\\")
																		+"Pos"+pos+ "_Deconvolution"
																		+ slaveChannel
																		+ "\\\", \"deconv_show_results boolean false\", \"deconvolution_method int 1\", \"deconv_iterations int 10\", \"deconv_sigmaA list_float 3.5,3.5,9.6\", \"deconv_sigmaB list_float 9.6,3.5,3.5\", \"use_deconv_sigma_conversion_factor boolean true\", \"x_move int 0\", \"y_move int 0\", \"z_move int 0\", \"fusion_range string 1-1\")";
								IJ.wait(5000);

								new MacroRunner(
										"print (\""
												+ savePath.replace("\\", "\\\\")
												+ "Pos"+pos+ "_SPIMB_Ch"
												+ keyChannel
												+ "_processed"
												+ File.separator.replace("\\", "\\\\")
												+ frameFileNames[f]
														+ File.separator.replace("\\", "\\\\")
														+ frameFileNames[f]
																+ "1_To_"
																+ frameFileNames[f]
																		+ ".mtx\");"
																		+ "while (!File.exists(\""
																		+ savePath.replace("\\", "\\\\")
																		+ "Pos"+pos+ "_SPIMB_Ch"
																		+ keyChannel
																		+ "_processed"
																		+ File.separator.replace("\\", "\\\\")
																		+ frameFileNames[f]
																				+ File.separator.replace("\\", "\\\\")
																				+ frameFileNames[f]
																						+ "1_To_"
																						+ frameFileNames[f]
																								+ ".mtx\")) {"
																								+ "wait(10000);"
																								+ "}"
																								+ "cpuPerformance = exec(\"cmd64\",\"/c\",\"typeperf \\\"\\\\Processor(_Total)\\\\% Processor Time\\\" -sc 1\");"
																								+ "cpuChunks = split(cpuPerformance,\"\\\"\");"
																								+ "x = parseFloat(cpuChunks[lengthOf(cpuChunks)-2]); "
																								+ "while(x >30) {\n"
																								+ "	wait(10000);"
																								+ "	cpuPerformance = exec(\"cmd64\",\"/c\",\"typeperf \\\"\\\\Processor(_Total)\\\\% Processor Time\\\" -sc 1\");"
																								+ "	cpuChunks = split(cpuPerformance,\"\\\"\");"
																								+ "	x = parseFloat(cpuChunks[lengthOf(cpuChunks)-2]); "
																								+ "}"
																								+ "print(\""
																								+ frameFileNames[f]
																										+ "_"
																										+ slaveChannel
																										+ " processing...\");"
																										+

										"			File.saveString(\'"
										+ deconStringSlave
										+ "\', \""
										+ tempDir.replace("\\", "\\\\")
										+ "GenerateFusion2"
										+ frameFileNames[f]
												+ timecode
												+ ".sct\");"
												+

										"		    f = File.open(\""
										+ tempDir.replace("\\", "\\\\")
										+ "GenerateFusion2"
										+ frameFileNames[f]
												+ timecode
												+ ".bat\");\n"
												+ "		    batStringD = \"@echo off\";\n"
												+ "		    print(f,batStringD);\n"
												+ "		    batStringC = \"C\\:\";\n"
												+ "		    print(f,batStringC);\n"
												+ "		    batStringA = \"cd C:\\\\Program Files\\\\mipav\";\n"
												+ "		    print(f,batStringA);\n"
												+ "		    batStringB = \"cmd64 /c mipav -s \\\""
												+ tempDir.replace("\\", "\\\\")
												+ "GenerateFusion2" + frameFileNames[f]
														+ timecode + ".sct\\\" -hide\";\n"
														+ "		    print(f,batStringB);\n"
														+ "		    print(f,\"exit\");\n"
														+ "		    File.close(f);	    \n" +

										"batJob = exec(\"cmd64\", \"/c\", \"start\", \"/low\", \"/min\", \"/wait\", \""
										+ tempDir.replace("\\", "\\\\")
										+ "GenerateFusion2" + frameFileNames[f]
												+ timecode + ".bat\");" + "");

								final String finalConvPath2 = savePath
										+"Pos"+pos+ "_Deconvolution2\\Decon_" + frameFileNames[f]
												+ ".tif";
								Thread convThread2 = new Thread(new Runnable() {
									public void run() {
										while (!(new File(finalConvPath2)).canRead()) {
											IJ.wait(10000);
										}
										IJ.wait(30000);
										new MacroRunner("print(\""
												+ frameFileNamesFinal[ff] + "_"
												+ slaveChannel + " complete.\");"
												+ "delBat = File.delete(\""
												+ tempDir.replace("\\", "\\\\")
												+ "GenerateFusion2"
												+ frameFileNamesFinal[ff] + timecode
												+ ".bat\");"
												+ "delSct = File.delete(\""
												+ tempDir.replace("\\", "\\\\")
												+ "GenerateFusion2"
												+ frameFileNamesFinal[ff] + timecode
												+ ".sct\");");

										ImagePlus convImp = IJ
												.openImage(finalConvPath2);
										if (convImp != null) {
											IJ.saveAs(convImp, "TIFF", finalConvPath2);
											convImp.close();
										}
									}
								});
								convThread2.start();
							}
						}
						// IJ.wait(15000);
					}
				}
		}
		
		while (true) {
			boolean focus = false;
			if ((new File(dirOrOMETiff)).isDirectory()) {
				if (omeTiffs) {

					fileListA = new File("" + dirOrOMETiff).list();
					String[] newlist = new String[fileListA.length];
					int yescount = 0;
					for (int fle=0; fle<fileListA.length; fle++) {
						if (fileListA[fle].contains(keyString)) {
							newlist[yescount] = fileListA[fle];
							yescount++;
						}
					}
					fileListA = Arrays.copyOf(newlist, yescount);
					listA = fileListA;
					deconFileList1 = (new File(dirOrOMETiff +"_Deconvolution1")).list();
					deconList1 = deconFileList1;
					deconFileList2 = (new File(dirOrOMETiff +"_Deconvolution2")).list();
					deconList2 = deconFileList2;

					while ((fileListA.length == listA.length)
							&& (!doDecon || ((deconList1 == null && deconList2 == null) || (!(deconList1 == null
							|| deconFileList1 == null || deconList1.length != deconFileList1.length) || !(deconList2 == null
							|| deconFileList2 == null || deconList2.length != deconFileList2.length))))) {
						if (IJ.escapePressed())
							if (!IJ.showMessageWithCancel(
									"Cancel diSPIM Monitor Updates?",
									"Monitoring of "
											+ dirOrOMETiff
											+ " paused by Escape.\nClick OK to resume."))
								return;
							else
								IJ.resetEscape();
						listA = new File("" + dirOrOMETiff).list();
						String[] newlist2 = new String[listA.length];
						int yescount2 = 0;
						for (int fle=0; fle<listA.length; fle++) {
							if (listA[fle].contains(keyString)) {
								newlist2[yescount2] = listA[fle];
								yescount2++;
							}
						}
						listA = Arrays.copyOf(newlist2, yescount2);
						deconList1 = (new File(dirOrOMETiff +"_Deconvolution1"))
								.list();
						deconList2 = (new File(dirOrOMETiff +"_Deconvolution2"))
								.list();
						IJ.wait(5000);
					}

					IJ.log("NEW DATA WRITTEN");
					IJ.wait(10000);
					fileListA = listA;
					deconFileList1 = deconList1;
					deconFileList2 = deconList2;

					long modDateA = 0;
					String recentestA = "";
					for (int a = 0; a < fileListA.length; a++) {
						if (!fileListA[a].endsWith(".roi")
								&& !fileListA[a].endsWith(".DS_Store")) {
							if (modDateA < (new File(dirOrOMETiff
									+ File.separator + fileListA[a]))
									.lastModified()) {
								modDateA = (new File(dirOrOMETiff
										+ File.separator + fileListA[a]))
										.lastModified();
								recentestA = dirOrOMETiff
										+ File.separator + fileListA[a];
							}
						}
					}
					IJ.log(recentestA + "\n" + modDateA);
					
					MultiFileInfoVirtualStack[] stackAs = new MultiFileInfoVirtualStack[pDim];
					MultiFileInfoVirtualStack[] stackBs = new MultiFileInfoVirtualStack[pDim];

					
					for (int pos=0; pos<pDim; pos++) {

						stackAs[pos] = new MultiFileInfoVirtualStack(
								dirOrOMETiff, keyString, cDim, zDim, dirOrOMETiffFile.list().length, vDim, pos,
								false, false);
						stackBs[pos] = new MultiFileInfoVirtualStack(
								dirOrOMETiff, keyString, cDim, zDim, dirOrOMETiffFile.list().length, vDim, pos,
								true, false);

						int cA = impAs[pos].getChannel();
						int zA = impAs[pos].getSlice();
						int tA = impAs[pos].getFrame();

						impAs[pos].setStack(stackAs[pos]);
						Calibration calA = impAs[pos].getCalibration();
						calA.pixelWidth = vWidth;
						calA.pixelHeight = vHeight;
						calA.pixelDepth = vDepthRaw;
						calA.setUnit(vUnit);

						stackAs[pos].setDimOrder("xyczt");
						if (stageScan)
							stackAs[pos].setSkewXperZ(
								calA.pixelDepth / calA.pixelWidth);
						impAs[pos].setDimensions(cDim, zDim, stackAs[pos].getSize()/(cDim*zDim));
						while (!impAs[pos].isComposite()) {
							IJ.wait(100);
						}
						((CompositeImage)impAs[pos]).setMode(CompositeImage.COMPOSITE);

						impAs[pos].setPosition(cA, zA,
								tA == impAs[pos].getNFrames() - 1 ? impAs[pos].getNFrames() : tA);
						impAs[pos].setWindow(WindowManager.getCurrentWindow());

						int cB = impBs[pos].getChannel();
						int zB = impBs[pos].getSlice();
						int tB = impBs[pos].getFrame();

						impBs[pos].setStack(stackBs[pos]);
						Calibration calB = impBs[pos].getCalibration();
						calB.pixelWidth = vWidth;
						calB.pixelHeight = vHeight;
						calB.pixelDepth = vDepthRaw;
						calB.setUnit(vUnit);

						stackBs[pos].setDimOrder("xyczt");
						if (stageScan)
							stackBs[pos].setSkewXperZ(
								-calB.pixelDepth / calB.pixelWidth);
						impBs[pos].setDimensions(cDim, zDim, stackBs[pos].getSize()/(cDim*zDim));
						while (!impBs[pos].isComposite()) {
							IJ.wait(100);
						}
						((CompositeImage)impBs[pos]).setMode(CompositeImage.COMPOSITE);

						impBs[pos].setPosition(cB, zB,
								tB == impBs[pos].getNFrames() - 1 ? impBs[pos].getNFrames() : tB);
						impBs[pos].setWindow(WindowManager.getCurrentWindow());

						boolean wasSynched = false;
						ArrayList<ImagePlus> synchedImpsArrayList = new ArrayList<ImagePlus>();
						if (SyncWindows.getInstance() != null) {
							int v = 0;
							while (SyncWindows.getInstance().getImageFromVector(v) != null) {
								wasSynched = true;
								synchedImpsArrayList.add(SyncWindows.getInstance()
										.getImageFromVector(v));
								v++;
							}
							SyncWindows.getInstance().close();
						}


						if (wasSynched) {
							SyncWindows sw = new SyncWindows();
							for (ImagePlus impS : synchedImpsArrayList) {
								sw.addImp(impS);
							}
						}
					}
				} else {

					listA = new File("" + dirOrOMETiff + "SPIMA").list();
					listB = new File("" + dirOrOMETiff + "SPIMB").list();
					big5DFileListAString = IJ.openAsString(dirOrOMETiff
							+ "Big5DFileListA.txt");
					big5DFileListBString = IJ.openAsString(dirOrOMETiff
							+ "Big5DFileListB.txt");
					deconList1 = (new File(dirOrOMETiff +"_Deconvolution1")).list();
					deconList2 = (new File(dirOrOMETiff +"_Deconvolution2")).list();

					while ((fileListA.length == listA.length || fileListB.length == listB.length)
							&& (!doDecon || ((deconList1 == null && deconList2 == null) || (!(deconList1 == null
							|| deconFileList1 == null || deconList1.length != deconFileList1.length) || !(deconList2 == null
							|| deconFileList2 == null || deconList2.length != deconFileList2.length))))) {
						if (IJ.escapePressed())
							if (!IJ.showMessageWithCancel(
									"Cancel diSPIM Monitor Updates?",
									"Monitoring of "
											+ dirOrOMETiff
											+ " paused by Escape.\nClick OK to resume."))
								return;
							else
								IJ.resetEscape();
						listA = new File("" + dirOrOMETiff + "SPIMA").list();
						listB = new File("" + dirOrOMETiff + "SPIMB").list();
						deconList1 = (new File(dirOrOMETiff +"Deconvolution1"))
								.list();
						deconList2 = (new File(dirOrOMETiff +"Deconvolution2"))
								.list();
						IJ.wait(5000);
					}
					//
					// if (isOpen("Display Channels")) {
					// selectWindow("Display Channels");
					// run("Close");
					// }
					//
					fileListA = new File("" + dirOrOMETiff + "SPIMA").list();
					fileListB = new File("" + dirOrOMETiff + "SPIMB").list();
					deconFileList1 = (new File(dirOrOMETiff +"_Deconvolution1"))
							.list();
					deconFileList2 = (new File(dirOrOMETiff +"_Deconvolution2"))
							.list();

					long modDateA = 0;
					String recentestA = "";
					for (int a = 0; a < fileListA.length; a++) {
						if (!fileListA[a].endsWith(".roi")
								&& !fileListA[a].endsWith(".DS_Store")) {
							if (modDateA < (new File(dirOrOMETiff + "SPIMA"
									+ File.separator + fileListA[a]))
									.lastModified()) {
								modDateA = (new File(dirOrOMETiff + "SPIMA"
										+ File.separator + fileListA[a]))
										.lastModified();
								recentestA = dirOrOMETiff + "SPIMA"
										+ File.separator + fileListA[a];
							}
						}
					}
					IJ.log(recentestA + "\n" + modDateA);
					if ((new File(recentestA)).isDirectory()) {
						String[] newTifList = { "" };
						while (newTifList.length < wavelengths * zSlices)
							newTifList = (new File(recentestA)).list();
						Arrays.sort(newTifList);
						for (int f = 0; f < newTifList.length; f++) {
							while (!(new File(dirOrOMETiff + "Big5DFileListA.txt")
							.exists()))
								IJ.wait(100);
							if (!newTifList[f].endsWith(".roi")
									&& !newTifList[f].endsWith(".DS_Store")
									&& big5DFileListAString.indexOf(recentestA
											+ newTifList[f]) < 0)
								IJ.append(recentestA + File.separator
										+ newTifList[f], dirOrOMETiff
										+ "Big5DFileListA.txt");
						}
					}

					fileListA = new File("" + dirOrOMETiff + "SPIMA").list();
					fileListB = new File("" + dirOrOMETiff + "SPIMB").list();

					long modDateB = 0;
					String recentestB = "";
					String recentestBname = "";
					for (int a = 0; a < fileListB.length; a++) {
						if (!fileListB[a].endsWith(".roi")
								&& !fileListB[a].endsWith(".DS_Store")) {
							if (modDateB < (new File(dirOrOMETiff + "SPIMB"
									+ File.separator + fileListB[a]))
									.lastModified()) {
								modDateB = (new File(dirOrOMETiff + "SPIMB"
										+ File.separator + fileListB[a]))
										.lastModified();
								recentestB = dirOrOMETiff + "SPIMB"
										+ File.separator + fileListB[a];
								recentestBname = fileListB[a];
							}
						}
					}
					IJ.log(recentestB + "\n" + modDateB);
					if (recentestBname.toLowerCase().startsWith("focus"))
						focus = true;
					if ((new File(recentestB)).isDirectory()) {
						String[] newTifList = { "" };
						while (newTifList.length < wavelengths * zSlices)
							newTifList = (new File(recentestB)).list();
						Arrays.sort(newTifList);
						for (int f = 0; f < newTifList.length; f++) {
							while (!(new File(dirOrOMETiff + "Big5DFileListB.txt")
							.exists()))
								IJ.wait(100);
							if (!newTifList[f].endsWith(".roi")
									&& !newTifList[f].endsWith(".DS_Store")
									&& big5DFileListBString.indexOf(recentestA
											+ newTifList[f]) < 0)
								IJ.append(recentestB + File.separator
										+ newTifList[f], dirOrOMETiff
										+ "Big5DFileListB.txt");
						}
					}
					boolean wasSynched = false;
					ArrayList<ImagePlus> synchedImpsArrayList = new ArrayList<ImagePlus>();
					if (SyncWindows.getInstance() != null) {
						int v = 0;
						while (SyncWindows.getInstance().getImageFromVector(v) != null) {
							wasSynched = true;
							synchedImpsArrayList.add(SyncWindows.getInstance()
									.getImageFromVector(v));
							v++;
						}
						SyncWindows.getInstance().close();
					}

					int cA = impA.getChannel();
					int zA = impA.getSlice();
					int tA = impA.getFrame();
					ListVirtualStack stackA = new ListVirtualStack(dirOrOMETiff
							+ "Big5DFileListA.txt");
					int stkNSlicesA = stackA.getSize();
					impA.setStack(stackA, wavelengths, zSlices, stkNSlicesA
							/ (wavelengths * zSlices));
					if (stageScan)
						impA.getStack().setSkewXperZ(
								-impA.getCalibration().pixelDepth
								/ impA.getCalibration().pixelWidth);
					impA.setPosition(cA, zA,
							tA == impA.getNFrames() - 1 ? impA.getNFrames() : tA);
					impA.setWindow(WindowManager.getCurrentWindow());

					int cB = impB.getChannel();
					int zB = impB.getSlice();
					int tB = impB.getFrame();
					ListVirtualStack stackB = new ListVirtualStack(dirOrOMETiff
							+ "Big5DFileListB.txt");
					int stkNSlicesB = stackB.getSize();
					impB.setStack(stackB, wavelengths, zSlices, stkNSlicesB
							/ (wavelengths * zSlices));
					if (stageScan)
						impB.getStack().setSkewXperZ(
								-impB.getCalibration().pixelDepth
								/ impB.getCalibration().pixelWidth);
					impB.setPosition(cB, zB,
							tB == impB.getNFrames() - 1 ? impB.getNFrames() : tB);
					impB.setWindow(WindowManager.getCurrentWindow());

					if (wasSynched) {
						SyncWindows sw = new SyncWindows();
						for (ImagePlus impS : synchedImpsArrayList) {
							sw.addImp(impS);
						}
					}
				}
			} else if (dirOrOMETiff.matches(".*_\\d{9}_\\d{3}_.*.tif")) {

				int newLength = oldLength;
				while (oldLength == newLength
						|| newLength % (wavelengths * 2 * zSlices) != 0) {

					IJ.wait(10);
					listB = new File(dirOrOMETiff).getParentFile().list();
					newLength = 0;
					for (String newFileListItem : listB)
						if (newFileListItem.endsWith(".tif"))
							newLength++;
				}
				oldLength = newLength;
				boolean wasSynched = false;
				ArrayList<ImagePlus> synchedImpsArrayList = new ArrayList<ImagePlus>();
				if (SyncWindows.getInstance() != null) {
					int v = 0;
					while (SyncWindows.getInstance().getImageFromVector(v) != null) {
						wasSynched = true;
						synchedImpsArrayList.add(SyncWindows.getInstance()
								.getImageFromVector(v));
						v++;
					}
					SyncWindows.getInstance().close();
				}

				int cA = impA.getChannel();
				int zA = impA.getSlice();
				int tA = impA.getFrame();
				int cB = impB.getChannel();
				int zB = impB.getSlice();
				int tB = impB.getFrame();
				if (impA.isComposite())
					modeA = ((CompositeImage) impA).getCompositeMode();
				if (impB.isComposite())
					modeB = ((CompositeImage) impB).getCompositeMode();

				// IJ.run("Image Sequence...",
				// "open=["+dirOrOMETiff+"] number="+ newLength
				// +" starting=1 increment=1 scale=100 file=Cam2 or=[] sort use");
				FolderOpener foA = new FolderOpener();
				foA.openAsVirtualStack(true);
				foA.sortFileNames(true);
				foA.setFilter("Cam2");
				ImagePlus impTmpA = foA.openFolder(new File(dirOrOMETiff)
						.getParent());

				// NOT WORKING YET!!!!
				ColorModel cmA = impA.getProcessor().getColorModel();
				double dminA = impA.getProcessor().getMin();
				double dmaxA = impA.getProcessor().getMax();
				impA.setStack(impTmpA.getStack(), wavelengths, zSlices, impTmpA
						.getStack().getSize() / (wavelengths * zSlices));
				impA.getProcessor().setColorModel(cmA);
				impA.getProcessor().setMinAndMax(dminA, dmaxA);
				if (stageScan)
					impA.getStack().setSkewXperZ(
							-impB.getCalibration().pixelDepth
									/ impB.getCalibration().pixelWidth);

				impA.setPosition(cA, zA,
						tA == impA.getNFrames() - 1 ? impA.getNFrames() : tA);
				impA.setWindow(WindowManager.getCurrentWindow());

				// IJ.run("Image Sequence...",
				// "open=["+dirOrOMETiff+"] number="+ newLength
				// +" starting=1 increment=1 scale=100 file=Cam1 or=[] sort use");
				FolderOpener foB = new FolderOpener();
				foB.openAsVirtualStack(true);
				foB.sortFileNames(true);
				foB.setFilter("Cam1");
				ImagePlus impTmpB = foB.openFolder(new File(dirOrOMETiff)
						.getParent());

				ColorModel cmB = impB.getProcessor().getColorModel();
				double dminB = impB.getProcessor().getMin();
				double dmaxB = impB.getProcessor().getMax();
				impB.setStack(impTmpB.getStack(), wavelengths, zSlices, impTmpB
						.getStack().getSize() / (wavelengths * zSlices));
				impB.getProcessor().setColorModel(cmB);
				impB.getProcessor().setMinAndMax(dminB, dmaxB);
				if (stageScan)
					impB.getStack().setSkewXperZ(
							impB.getCalibration().pixelDepth
									/ impB.getCalibration().pixelWidth);

				impB.setPosition(cB, zB,
						tB == impB.getNFrames() - 1 ? impB.getNFrames() : tB);
				impB.setWindow(WindowManager.getCurrentWindow());

				if (wasSynched) {
					SyncWindows sw = new SyncWindows();
					for (ImagePlus impS : synchedImpsArrayList) {
						sw.addImp(impS);
					}
				}

			} else {
				long fileOldMod = (new File(dirOrOMETiff)).lastModified();
				while (fileOldMod == (new File(dirOrOMETiff)).lastModified()) {
					if (IJ.escapePressed())
						if (!IJ.showMessageWithCancel(
								"Cancel diSPIM Monitor Updates?",
								"Monitoring of "
										+ dirOrOMETiff
										+ " paused by Escape.\nClick OK to resume."))
							return;
						else
							IJ.resetEscape();
					IJ.wait(5000);
				}

				boolean wasSynched = false;
				ArrayList<ImagePlus> synchedImpsArrayList = new ArrayList<ImagePlus>();
				if (SyncWindows.getInstance() != null) {
					int v = 0;
					while (SyncWindows.getInstance().getImageFromVector(v) != null) {
						wasSynched = true;
						synchedImpsArrayList.add(SyncWindows.getInstance()
								.getImageFromVector(v));
						v++;
					}
					SyncWindows.getInstance().close();
				}

				TiffDecoder tdA = new TiffDecoder("", dirOrOMETiff);
				int cA = impA.getChannel();
				int zA = impA.getSlice();
				int tA = impA.getFrame();

				try {
					impA.setStack(new FileInfoVirtualStack(tdA.getTiffInfo(),
							false));
					int stackSize = impA.getNSlices();
					int nChannels = wavelengths * 2;
					int nSlices = zSlices;
					int nFrames = (int) Math.floor((double) stackSize
							/ (nChannels * nSlices));

					impA.setTitle("SPIMA: " + dirOrOMETiff);

					if (nChannels * nSlices * nFrames != stackSize) {
						if (nChannels * nSlices * nFrames > stackSize) {
							for (int a = stackSize; a < nChannels * nSlices
									* nFrames; a++) {
								if (impA.getStack().isVirtual())
									((VirtualStack) impA.getStack())
											.addSlice("blank slice");
								else
									impA.getStack().addSlice(
											impA.getProcessor()
													.createProcessor(
															impA.getWidth(),
															impA.getHeight()));
							}
						} else if (nChannels * nSlices * nFrames < stackSize) {
							for (int a = nChannels * nSlices * nFrames; a < stackSize; a++) {
								impA.getStack().deleteSlice(
										nChannels * nSlices * nFrames);
							}
						} else {
							IJ.error("HyperStack Converter",
									"channels x slices x frames <> stack size");
							return;
						}
					}
					for (int t = nFrames - 1; t >= 0; t--) {
						for (int c = nChannels; c >= 1; c = c - 2) {
							for (int s = c * nSlices - 1; s >= (c - 1)
									* nSlices; s--) {
								int target = t * nChannels * nSlices + s + 1;
								impA.getStack().deleteSlice(target);
							}
						}
					}
					impA.setStack(impA.getImageStack());

					impA.setDimensions(wavelengths, nSlices, nFrames);

					if (nChannels > 1) {
						impA = new CompositeImage(impA);
						while (!impA.isComposite()) {
							IJ.wait(100);
							// selectWindow("SPIMB: "+dir);
						}
					}
					Calibration cal = impA.getCalibration();
					cal.pixelWidth = vWidth;
					cal.pixelHeight = vHeight;
					cal.pixelDepth = vDepthRaw;
					cal.setUnit(vUnit);
					if (stageScan)
						impA.getStack().setSkewXperZ(
								cal.pixelDepth / cal.pixelWidth);

					impA.setPosition(wavelengths, nSlices, nFrames);

					// impA.resetDisplayRange();
					impA.setPosition(1, nSlices / 2, nFrames / 2);
					// impA.resetDisplayRange();
					if (impA.isComposite())
						((CompositeImage) impA)
								.setMode(CompositeImage.COMPOSITE);
					impA.setFileInfo(new FileInfo());
					impA.getOriginalFileInfo().fileName = dirOrOMETiff;
					impA.getOriginalFileInfo().directory = dirOrOMETiff;

				} catch (IOException e) {
					e.printStackTrace();
				}

				impA.setPosition(cA, zA,
						tA == impA.getNFrames() - 1 ? impA.getNFrames() : tA);
				impA.setWindow(WindowManager.getCurrentWindow());

				TiffDecoder tdB = new TiffDecoder("", dirOrOMETiff);
				int cB = impB.getChannel();
				int zB = impB.getSlice();
				int tB = impB.getFrame();

				try {
					impB.setStack(new FileInfoVirtualStack(tdB.getTiffInfo(),
							false));
					int stackSize = impB.getNSlices();
					int nChannels = wavelengths * 2;
					int nSlices = zSlices;
					int nFrames = (int) Math.floor((double) stackSize
							/ (nChannels * nSlices));

					impB.setTitle("SPIMB: " + dirOrOMETiff);

					if (nChannels * nSlices * nFrames != stackSize) {
						if (nChannels * nSlices * nFrames > stackSize) {
							for (int a = stackSize; a < nChannels * nSlices
									* nFrames; a++) {
								if (impB.getStack().isVirtual())
									((VirtualStack) impB.getStack())
											.addSlice("blank slice");
								else
									impB.getStack().addSlice(
											impB.getProcessor()
													.createProcessor(
															impB.getWidth(),
															impB.getHeight()));
							}
						} else if (nChannels * nSlices * nFrames < stackSize) {
							for (int a = nChannels * nSlices * nFrames; a < stackSize; a++) {
								impB.getStack().deleteSlice(
										nChannels * nSlices * nFrames);
							}
						} else {
							IJ.error("HyperStack Converter",
									"channels x slices x frames <> stack size");
							return;
						}
					}
					for (int t = nFrames - 1; t >= 0; t--) {
						for (int c = nChannels; c >= 1; c = c - 2) {
							for (int s = c * nSlices - 1; s >= (c - 1)
									* nSlices; s--) {
								int target = t * nChannels * nSlices + s + 1;
								impB.getStack().deleteSlice(target);
							}
						}
					}
					impB.setStack(impB.getImageStack());

					impB.setDimensions(wavelengths, nSlices, nFrames);

					if (nChannels > 1) {
						impB = new CompositeImage(impB);
						while (!impB.isComposite()) {
							IJ.wait(100);
							// selectWindow("SPIMB: "+dir);
						}
					}
					Calibration cal = impB.getCalibration();
					cal.pixelWidth = vWidth;
					cal.pixelHeight = vHeight;
					cal.pixelDepth = vDepthRaw;
					cal.setUnit(vUnit);
					if (stageScan)
						impB.getStack().setSkewXperZ(
								-cal.pixelDepth / cal.pixelWidth);

					impB.setPosition(wavelengths, nSlices, nFrames);

					// impB.resetDisplayRange();
					impB.setPosition(1, nSlices / 2, nFrames / 2);
					// impB.resetDisplayRange();
					if (impB.isComposite())
						((CompositeImage) impB)
								.setMode(CompositeImage.COMPOSITE);
					impB.setFileInfo(new FileInfo());
					impB.getOriginalFileInfo().fileName = dirOrOMETiff;
					impB.getOriginalFileInfo().directory = dirOrOMETiff;

				} catch (IOException e) {
					e.printStackTrace();
				}

				impB.setPosition(cB, zB,
						tB == impB.getNFrames() - 1 ? impB.getNFrames() : tB);
				impB.setWindow(WindowManager.getCurrentWindow());

				if (wasSynched) {
					SyncWindows sw = new SyncWindows();
					for (ImagePlus impS : synchedImpsArrayList) {
						sw.addImp(impS);
					}
				}
			}

			if (focus) {
				// SAD THAT I HAVE TO FAKE THIS, BUT NOT WORKING IN MY ATTEMPTS
				// AT JAVA-ONLY...
				String fftMacroString = "		    dir = \""
						+ dirOrOMETiff.replace("\\", "\\\\")
						+ "\";\n"
						+ "			autoFPath = dir+\"AutoFocusCommaSpace.txt\";"
						+ "		    print(nImages);\n"
						+ "		    File.delete(autoFPath);\n"
						+ "			autoFocusString = \"\";\n"
						+ "			for (i=1;i<=nImages;i++){\n"
						+ "				print(nImages+\" \"+i);\n"
						+ "				\n"
						+ "				setBatchMode(true);\n"
						+ "				selectImage(i);\n"
						+ "		\n"
						+ "				source = getTitle();\n"
						+ "				Stack.getDimensions(width, height, channels, zDepth, frames);\n"
						+ "				Stack.getPosition(channel, slice, frame);\n"
						+ "				Stack.setPosition(channel, slice, frames);\n"
						+ "				for (z=0; z<zDepth; z++) { \n"
						+ "					Stack.setSlice(z+1);\n"
						+ "					run(\"FFT, no auto-scaling\");\n"
						+ "					if (z==0) {\n"
						+ "						rename(\"FFTstack\");	\n"
						+ "					} else {\n"
						+ "						run(\"Select All\");\n"
						+ "						run(\"Copy\");\n"
						+ "						close();\n"
						+ "						selectWindow(\"FFTstack\");\n"
						+ "						run(\"Add Slice\");\n"
						+ "						if (z>0)\n"
						+ "							Stack.setSlice(z+2);\n"
						+ "						run(\"Select All\");\n"
						+ "						run(\"Paste\");\n"
						+ "					}\n"
						+ "					selectWindow(source);\n"
						+ "				}\n"
						+ "				Stack.setPosition(channel, slice, frame);\n"
						+ "				selectWindow(\"FFTstack\");\n"
						+ "				makeOval(250, 250, 13, 13);\n"
						+ "				run(\"Clear\", \"stack\");\n"
						+ "				makeOval(220, 220, 73, 73);\n"
						+ "				run(\"Clear Outside\", \"stack\");\n"
						+ "				run(\"Plot Z-axis Profile\");\n"
						+ "				close();\n"
						+ "				selectWindow(\"FFTstack\");\n"
						+ "				close();\n"
						+ "				\n"
						+ "				sliceAvgs = newArray(zDepth);\n"
						+ "				List.clear;\n"
						+ "				for (z=0; z<zDepth; z++) { \n"
						+ "					sliceAvgs[z] = getResult(\"Mean\", z);\n"
						+ "					//print(sliceAvgs[z] );\n"
						+ "					List.set(sliceAvgs[z] , z);\n"
						+ "				}\n"
						+ "				\n"
						+ "				Array.sort(sliceAvgs);\n"
						+ "				print(source+\": Best focus in slice \"+(parseInt(List.get(sliceAvgs[zDepth-1]))+1));\n"
						+ "				autoFocusString = autoFocusString + (parseInt(List.get(sliceAvgs[zDepth-1]))+1)+\", \";\n"
						+ "				selectWindow(\"Results\");\n"
						+ "				run(\"Close\");\n"
						+ "				setBatchMode(false);\n"
						+ "				selectWindow(source);\n"
						+ "				Stack.setPosition(channel, slice, frame);\n"
						+ "				updateDisplay();\n"
						+ "			}\n"
						+ "			File.saveString(autoFocusString, autoFPath);			\n"
						+ "";
				IJ.runMacro(fftMacroString);
			}


			if (doDecon) {
				for (int pos=0; pos<pDim; pos++) {

					impA = impAs[pos];
					impB = impBs[pos];

					Roi roiA = impA.getRoi();
					Roi roiB = impB.getRoi();

					int wasFrameA = impA.getFrame();
					int wasFrameB = impB.getFrame();
					int wasFrameDF1 = 1;
					if (impDF1 != null)
						wasFrameDF1 = impDF1.getFrame();
					int wasFrameDF2 = 1;
					if (impDF2 != null)
						wasFrameDF2 = impDF2.getFrame();
					int wasSliceA = impA.getSlice();
					int wasSliceB = impB.getSlice();
					int wasSliceDF1 = 1;
					if (impDF1 != null)
						wasSliceDF1 = impDF1.getSlice();
					int wasSliceDF2 = 1;
					if (impDF2 != null)
						wasSliceDF2 = impDF2.getSlice();
					int wasChannelA = impA.getChannel();
					int wasChannelB = impB.getChannel();
					int wasChannelDF1 = 1;
					if (impDF1 != null)
						wasChannelDF1 = impDF1.getChannel();
					int wasChannelDF2 = 1;
					if (impDF2 != null)
						wasChannelDF2 = impDF2.getChannel();
					WindowManager.setTempCurrentImage(impA);
					IJ.open(savePath/* + dirOrOMETiffName */+  "Pos" + pos + "A_crop.roi");
					WindowManager.setTempCurrentImage(impB);
					IJ.open(savePath/* + dirOrOMETiffName */+  "Pos" + pos + "B_crop.roi");
					WindowManager.setTempCurrentImage(null);

					for (int f = 1; f <= impA.getNFrames(); f++) {

						impA.setPositionWithoutUpdate(impA.getChannel(),
								impA.getSlice(), f);

						String frameFileName = "";
						if (impA.getStack() instanceof ListVirtualStack)
							frameFileName = ((ListVirtualStack) impA.getStack())
							.getDirectory(impA.getCurrentSlice());
						else if (impA.getStack() instanceof FileInfoVirtualStack
								|| impA.getStack() instanceof MultiFileInfoVirtualStack)
							frameFileName = "t" + f;
						else
							frameFileName = "t" + f;
						final String frameFileNameFinal = frameFileName;
						final String timecode = "" + (new Date()).getTime();

						if (!(new File(savePath + "Pos"+pos+ "_SPIMA_Ch1_processed"
								+ File.separator + frameFileName + File.separator
								+ frameFileName + ".tif")).canRead()
								|| (wavelengths == 2 && !(new File(savePath
										+ "Pos"+pos+ "_SPIMA_Ch2_processed" + File.separator
										+ frameFileName + File.separator
										+ frameFileName + ".tif")).canRead())
										|| !(new File(savePath + "Pos"+pos+ "_SPIMB_Ch1_processed"
												+ File.separator + frameFileName
												+ File.separator + frameFileName + ".tif"))
												.canRead()
												|| (wavelengths == 2 && !(new File(savePath
														+ "Pos"+pos+ "_SPIMA_Ch2_processed" + File.separator
														+ frameFileName + File.separator
														+ frameFileName + ".tif")).canRead())) {
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMA_Ch1_processed\");");
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMA_Ch1_processed\"+File.separator+\""
									+ frameFileName + "\");");
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMB_Ch1_processed\");");
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMB_Ch1_processed\"+File.separator+\""
									+ frameFileName + "\");");
							IJ.runMacro("File.makeDirectory(\""
									+ savePath.replace("\\", "\\\\")
									+"Pos"+pos+ "_Deconvolution1\");");
							if (wavelengths == 2) {
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMA_Ch2_processed\");");
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMA_Ch2_processed\"+File.separator+\""
										+ frameFileName + "\");");
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMB_Ch2_processed\");");
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMB_Ch2_processed\"+File.separator+\""
										+ frameFileName + "\");");
								IJ.runMacro("File.makeDirectory(\""
										+ savePath.replace("\\", "\\\\")
										+"Pos"+pos+ "_Deconvolution2\");");
							}

							ImageStack stackA1 = new ImageStack(325, 425);
							ImageStack stackA2 = new ImageStack(325, 425);
							impA.getWindow().setEnabled(false);
							for (int i = 1; i <= impA.getNSlices(); i++) {
								impA.setPositionWithoutUpdate(1, i, f);
								stackA1.addSlice(impA.getProcessor().crop());
								if (wavelengths == 2) {
									impA.setPositionWithoutUpdate(2, i, f);
									stackA2.addSlice(impA.getProcessor().crop());
								}
							}
							impA.getWindow().setEnabled(true);
							ImagePlus impXA1 = new ImagePlus();
							impXA1.setStack(stackA1);
							impXA1.setCalibration(impA.getCalibration());
							// impXA1.getCalibration().pixelDepth =
							// impXA1.getCalibration().pixelWidth;
							IJ.saveAs(impXA1, "Tiff", savePath
									+ "Pos"+pos+ "_SPIMA_Ch1_processed" + File.separator
									+ frameFileName + File.separator
									+ frameFileName + ".tif");
							if (wavelengths == 2) {
								ImagePlus impXA2 = new ImagePlus();
								impXA2.setStack(stackA2);
								impXA2.setCalibration(impA.getCalibration());
								// impXA2.getCalibration().pixelDepth =
								// impXA2.getCalibration().pixelWidth;
								IJ.saveAs(impXA2, "Tiff", savePath
										+ "Pos"+pos+ "_SPIMA_Ch2_processed" + File.separator
										+ frameFileName + File.separator
										+ frameFileName + ".tif");
							}
							ImageStack stackB1 = new ImageStack(325, 425);
							ImageStack stackB2 = new ImageStack(325, 425);
							impB.getWindow().setEnabled(false);
							for (int i = 1; i <= impB.getNSlices(); i++) {
								impB.setPositionWithoutUpdate(1, i, f);
								stackB1.addSlice(impB.getProcessor().crop());
								if (wavelengths == 2) {
									impB.setPositionWithoutUpdate(2, i, f);
									stackB2.addSlice(impB.getProcessor().crop());
								}
							}
							impB.getWindow().setEnabled(true);
							ImagePlus impXB1 = new ImagePlus();
							impXB1.setStack(stackB1);
							impXB1.setCalibration(impB.getCalibration());
							// impXB1.getCalibration().pixelDepth =
							// impXB1.getCalibration().pixelWidth;
							IJ.saveAs(impXB1, "Tiff", savePath
									+ "Pos"+pos+ "_SPIMB_Ch1_processed" + File.separator
									+ frameFileName + File.separator
									+ frameFileName + ".tif");
							if (wavelengths == 2) {
								ImagePlus impXB2 = new ImagePlus();
								impXB2.setStack(stackB2);
								impXB2.setCalibration(impB.getCalibration());
								// impXB2.getCalibration().pixelDepth =
								// impXB2.getCalibration().pixelWidth;
								IJ.saveAs(impXB2, "Tiff", savePath
										+ "Pos"+pos+ "_SPIMB_Ch2_processed" + File.separator
										+ frameFileName + File.separator
										+ frameFileName + ".tif");
							}
						}

						if (!(new File(savePath +"Pos"+pos+ "_Deconvolution1" + File.separator
								+ "Decon_" + frameFileName + ".tif")).canRead()
								|| (wavelengths == 2 && !(new File(savePath
										+"Pos"+pos+ "_Deconvolution2" + File.separator
										+ "Decon_" + frameFileName + ".tif"))
										.canRead())) {

							String deconStringKey = "nibib.spim.PlugInDialogGenerateFusion(\"reg_one boolean false\", \"reg_all boolean true\", \"no_reg_2D boolean false\", \"reg_2D_one boolean false\", \"reg_2D_all boolean false\", \"rotate_begin list_float -10.0,-10.0,-10.0\", \"rotate_end list_float 10.0,10.0,10.0\", \"coarse_rate list_float 3.0,3.0,3.0\", \"fine_rate list_float 0.5,0.5,0.5\", \"save_arithmetic boolean false\", \"show_arithmetic boolean false\", \"save_geometric boolean false\", \"show_geometric boolean false\", \"do_interImages boolean false\", \"save_prefusion boolean false\", \"do_show_pre_fusion boolean false\", \"do_threshold boolean false\", \"save_max_proj boolean false\", \"show_max_proj boolean false\", \"x_max_box_selected boolean false\", \"y_max_box_selected boolean false\", \"z_max_box_selected boolean false\", \"do_smart_movement boolean false\", \"threshold_intensity double 10.0\", \"res_x double 0.1625\", \"res_y double 0.1625\", \"res_z double 1.0\", \"mtxFileDirectory string "
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMB_Ch"
									+ keyChannel
									+ "_processed"
									+ File.separator.replace("\\", "\\\\")
									+ frameFileName
									+ "\", \"spimAFileDir string "
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMB_Ch"
									+ keyChannel
									+ "_processed"
									+ File.separator.replace("\\", "\\\\")
									+ frameFileName
									+ "\", \"spimBFileDir string "
									+ savePath.replace("\\", "\\\\")
									+ "Pos"+pos+ "_SPIMA_Ch"
									+ keyChannel
									+ "_processed"
									+ File.separator.replace("\\", "\\\\")
									+ frameFileName
									+ "\", \"baseImage string "
									+ frameFileName
									+ "\", \"base_rotation int -1\", \"transform_rotation int 5\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
									+ savePath.replace("\\", "\\\\")
									+"Pos"+pos+ "_Deconvolution"
									+ keyChannel
									+ "\\\", \"deconv_show_results boolean false\", \"deconvolution_method int 1\", \"deconv_iterations int 10\", \"deconv_sigmaA list_float 3.5,3.5,9.6\", \"deconv_sigmaB list_float 9.6,3.5,3.5\", \"use_deconv_sigma_conversion_factor boolean true\", \"x_move int 0\", \"y_move int 0\", \"z_move int 0\", \"fusion_range string 1-1\")";
							IJ.wait(5000);

							new MacroRunner(
									"cpuPerformance = exec(\"cmd64\",\"/c\",\"typeperf \\\"\\\\Processor(_Total)\\\\% Processor Time\\\" -sc 1\");"
											+ "cpuChunks = split(cpuPerformance,\"\\\"\");"
											+ "x = parseFloat(cpuChunks[lengthOf(cpuChunks)-2]); "
											+ "while(x >30) {\n"
											+ "	wait(10000);"
											+ "	cpuPerformance = exec(\"cmd64\",\"/c\",\"typeperf \\\"\\\\Processor(_Total)\\\\% Processor Time\\\" -sc 1\");"
											+ "	cpuChunks = split(cpuPerformance,\"\\\"\");"
											+ "	x = parseFloat(cpuChunks[lengthOf(cpuChunks)-2]); "
											+ "}" + "print(\""
											+ frameFileName
											+ "_"
											+ keyChannel
											+ " processing...\");"
											+

										"			File.saveString(\'"
										+ deconStringKey
										+ "\', \""
										+ tempDir.replace("\\", "\\\\")
										+ "GenerateFusion1"
										+ frameFileName
										+ timecode
										+ ".sct\");"
										+

										"		    f = File.open(\""
										+ tempDir.replace("\\", "\\\\")
										+ "GenerateFusion1"
										+ frameFileName
										+ timecode
										+ ".bat\");\n"
										+ "		    batStringD = \"@echo off\";\n"
										+ "		    print(f,batStringD);\n"
										+ "		    batStringC = \"C\\:\";\n"
										+ "		    print(f,batStringC);\n"
										+ "		    batStringA = \"cd C:\\\\Program Files\\\\mipav\";\n"
										+ "		    print(f,batStringA);\n"
										+ "		    batStringB = \"cmd64 /c mipav -s \\\""
										+ tempDir.replace("\\", "\\\\")
										+ "GenerateFusion1"
										+ frameFileName
										+ timecode
										+ ".sct\\\" -hide\";\n"
										+ "		    print(f,batStringB);\n"
										+ "		    print(f,\"exit\");\n"
										+ "		    File.close(f);	    \n"
										+

										"batJob = exec(\"cmd64\", \"/c\", \"start\", \"/low\", \"/min\", \"/wait\", \""
										+ tempDir.replace("\\", "\\\\")
										+ "GenerateFusion1"
										+ frameFileName
										+ timecode + ".bat\");" + "");

							final String finalConvPath = savePath
									+"Pos"+pos+ "_Deconvolution1\\Decon_" + frameFileName
									+ ".tif";
							Thread convThread = new Thread(new Runnable() {
								public void run() {
									while (!(new File(finalConvPath)).canRead()) {
										IJ.wait(10000);
									}
									IJ.wait(30000);
									new MacroRunner("print(\"" + frameFileNameFinal
											+ "_" + keyChannel + " complete.\");"
											+ "delBat = File.delete(\""
											+ tempDir.replace("\\", "\\\\")
											+ "GenerateFusion1"
											+ frameFileNameFinal + timecode
											+ ".bat\");"
											+ "delSct = File.delete(\""
											+ tempDir.replace("\\", "\\\\")
											+ "GenerateFusion1"
											+ frameFileNameFinal + timecode
											+ ".sct\");");

									ImagePlus convImp = IJ.openImage(finalConvPath);
									if (convImp != null) {
										IJ.saveAs(convImp, "TIFF", finalConvPath);
										convImp.close();
									}
								}
							});
							convThread.start();

							if (wavelengths == 2) {
								String deconStringSlave = "nibib.spim.PlugInDialogGenerateFusion(\"reg_one boolean false\", \"reg_all boolean true\", \"no_reg_2D boolean false\", \"reg_2D_one boolean false\", \"reg_2D_all boolean false\", \"rotate_begin list_float -10.0,-10.0,-10.0\", \"rotate_end list_float 10.0,10.0,10.0\", \"coarse_rate list_float 3.0,3.0,3.0\", \"fine_rate list_float 0.5,0.5,0.5\", \"save_arithmetic boolean false\", \"show_arithmetic boolean false\", \"save_geometric boolean false\", \"show_geometric boolean false\", \"do_interImages boolean false\", \"save_prefusion boolean false\", \"do_show_pre_fusion boolean false\", \"do_threshold boolean false\", \"save_max_proj boolean false\", \"show_max_proj boolean false\", \"x_max_box_selected boolean false\", \"y_max_box_selected boolean false\", \"z_max_box_selected boolean false\", \"do_smart_movement boolean false\", \"threshold_intensity double 10.0\", \"res_x double 0.1625\", \"res_y double 0.1625\", \"res_z double 1.0\", \"mtxFileDirectory string "
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMB_Ch"
										+ keyChannel
										+ "_processed"
										+ File.separator.replace("\\", "\\\\")
										+ frameFileName
										+ "\", \"spimAFileDir string "
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMB_Ch"
										+ slaveChannel
										+ "_processed"
										+ File.separator.replace("\\", "\\\\")
										+ frameFileName
										+ "\", \"spimBFileDir string "
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMA_Ch"
										+ slaveChannel
										+ "_processed"
										+ File.separator.replace("\\", "\\\\")
										+ frameFileName
										+ "\", \"baseImage string "
										+ frameFileName
										+ "\", \"base_rotation int -1\", \"transform_rotation int 5\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
										+ savePath.replace("\\", "\\\\")
										+"Pos"+pos+ "_Deconvolution"
										+ slaveChannel
										+ "\\\", \"deconv_show_results boolean false\", \"deconvolution_method int 1\", \"deconv_iterations int 10\", \"deconv_sigmaA list_float 3.5,3.5,9.6\", \"deconv_sigmaB list_float 9.6,3.5,3.5\", \"use_deconv_sigma_conversion_factor boolean true\", \"x_move int 0\", \"y_move int 0\", \"z_move int 0\", \"fusion_range string 1-1\")";
								IJ.wait(5000);

								new MacroRunner(
										"print (\""
												+ savePath.replace("\\", "\\\\")
												+ "Pos"+pos+ "_SPIMB_Ch"
												+ keyChannel
												+ "_processed"
												+ File.separator.replace("\\",
														"\\\\")
														+ frameFileName
														+ File.separator.replace("\\",
																"\\\\")
																+ frameFileName
																+ "1_To_"
																+ frameFileName
																+ ".mtx\");"
																+ "while (!File.exists(\""
																+ savePath.replace("\\", "\\\\")
																+ "Pos"+pos+ "_SPIMB_Ch"
																+ keyChannel
																+ "_processed"
																+ File.separator.replace("\\",
																		"\\\\")
																		+ frameFileName
																		+ File.separator.replace("\\",
																				"\\\\")
																				+ frameFileName
																				+ "1_To_"
																				+ frameFileName
																				+ ".mtx\")) {"
																				+ "wait(10000);"
																				+ "}"
																				+ "cpuPerformance = exec(\"cmd64\",\"/c\",\"typeperf \\\"\\\\Processor(_Total)\\\\% Processor Time\\\" -sc 1\");"
																				+ "cpuChunks = split(cpuPerformance,\"\\\"\");"
																				+ "x = parseFloat(cpuChunks[lengthOf(cpuChunks)-2]); "
																				+ "while(x >30) {\n"
																				+ "	wait(10000);"
																				+ "	cpuPerformance = exec(\"cmd64\",\"/c\",\"typeperf \\\"\\\\Processor(_Total)\\\\% Processor Time\\\" -sc 1\");"
																				+ "	cpuChunks = split(cpuPerformance,\"\\\"\");"
																				+ "	x = parseFloat(cpuChunks[lengthOf(cpuChunks)-2]); "
																				+ "}"
																				+ "print(\""
																				+ frameFileName
																				+ "_"
																				+ slaveChannel
																				+ " processing...\");"
																				+

											"			File.saveString(\'"
											+ deconStringSlave
											+ "\', \""
											+ tempDir.replace("\\", "\\\\")
											+ "GenerateFusion2"
											+ frameFileName
											+ timecode
											+ ".sct\");"
											+

											"		    f = File.open(\""
											+ tempDir.replace("\\", "\\\\")
											+ "GenerateFusion2"
											+ frameFileName
											+ timecode
											+ ".bat\");\n"
											+ "		    batStringD = \"@echo off\";\n"
											+ "		    print(f,batStringD);\n"
											+ "		    batStringC = \"C\\:\";\n"
											+ "		    print(f,batStringC);\n"
											+ "		    batStringA = \"cd C:\\\\Program Files\\\\mipav\";\n"
											+ "		    print(f,batStringA);\n"
											+ "		    batStringB = \"cmd64 /c mipav -s \\\""
											+ tempDir.replace("\\", "\\\\")
											+ "GenerateFusion2" + frameFileName
											+ timecode + ".sct\\\" -hide\";\n"
											+ "		    print(f,batStringB);\n"
											+ "		    print(f,\"exit\");\n"
											+ "		    File.close(f);	    \n" +

											"batJob = exec(\"cmd64\", \"/c\", \"start\", \"/low\", \"/min\", \"/wait\", \""
											+ tempDir.replace("\\", "\\\\")
											+ "GenerateFusion2" + frameFileName
											+ timecode + ".bat\");" + "");

								final String finalConvPath2 = savePath
										+"Pos"+pos+ "_Deconvolution2\\Decon_" + frameFileName
										+ ".tif";
								Thread convThread2 = new Thread(new Runnable() {
									public void run() {
										while (!(new File(finalConvPath2))
												.canRead()) {
											IJ.wait(10000);
										}
										IJ.wait(30000);
										new MacroRunner("print(\""
												+ frameFileNameFinal + "_"
												+ slaveChannel + " complete.\");"
												+ "delBat = File.delete(\""
												+ tempDir.replace("\\", "\\\\")
												+ "GenerateFusion2"
												+ frameFileNameFinal + timecode
												+ ".bat\");"
												+ "delSct = File.delete(\""
												+ tempDir.replace("\\", "\\\\")
												+ "GenerateFusion2"
												+ frameFileNameFinal + timecode
												+ ".sct\");");

										ImagePlus convImp = IJ
												.openImage(finalConvPath2);
										if (convImp != null) {
											IJ.saveAs(convImp, "TIFF",
													finalConvPath2);
											convImp.close();
										}
									}
								});
								convThread2.start();

							}
						}
					}
					// IJ.wait(15000);

					impA.setPosition(wasChannelA, wasSliceA, wasFrameA);
					impB.setPosition(wasChannelB, wasSliceB, wasFrameB);

					if ((new File(dirOrOMETiff)).canRead()) {
						if (impDF1 == null) {
							MultiFileInfoVirtualStack deconmfivs = new MultiFileInfoVirtualStack(
									(new File(dirOrOMETiff)).isDirectory() ? dirOrOMETiff
											: (new File(dirOrOMETiff)).getParent()
											+ File.separator,
											"Deconvolution", false);
							if (deconmfivs.getSize() > 0) {
								impDF1 = new ImagePlus();
								impDF1.setStack(
										"Decon-Fuse"
												+ impA.getTitle()
												.replace(
														impA.getTitle()
														.split(":")[0],
														""), deconmfivs);
								impDF1.setFileInfo(new FileInfo());
								// impDF1.getOriginalFileInfo().directory = (new
								// File(dirOrOMETiff)).isDirectory()?dirOrOMETiff:((new
								// File(dirOrOMETiff)).getParent()+File.separator);
								impDF1.getOriginalFileInfo().directory = dirOrOMETiff;
								int stkNSlicesDF = impDF1.getStackSize();
								int zSlicesDF1 = deconmfivs.getFivStacks().get(0)
										.getSize();
								impDF1.setOpenAsHyperStack(true);
								impDF1.setStack(impDF1.getStack(), wavelengths,
										zSlicesDF1, stkNSlicesDF
										/ (wavelengths * zSlicesDF1));
								ciDF1 = new CompositeImage(impDF1);
								if (wavelengths > 1)
									ciDF1.setMode(CompositeImage.COMPOSITE);
								else
									ciDF1.setMode(CompositeImage.GRAYSCALE);
								ciDF1.show();
								win = ciDF1.getWindow();
							}
						} else {
							MultiFileInfoVirtualStack deconmfivs = new MultiFileInfoVirtualStack(
									(new File(dirOrOMETiff)).isDirectory() ? dirOrOMETiff
											: (new File(dirOrOMETiff)).getParent()
											+ File.separator,
											"Deconvolution", false);
							if (deconmfivs.getSize() > 0) {
								impDF2 = new ImagePlus();
								impDF2.setStack(
										"Decon-Fuse"
												+ impA.getTitle()
												.replace(
														impA.getTitle()
														.split(":")[0],
														""), deconmfivs);
								impDF2.setFileInfo(new FileInfo());
								// impDF2.getOriginalFileInfo().directory = (new
								// File(dirOrOMETiff)).isDirectory()?dirOrOMETiff:((new
								// File(dirOrOMETiff)).getParent()+File.separator);
								impDF2.getOriginalFileInfo().directory = dirOrOMETiff;
								int stkNSlicesDF = impDF2.getStackSize();
								int zSlicesDF1 = deconmfivs.getFivStacks().get(0)
										.getSize();
								impDF2.setOpenAsHyperStack(true);
								impDF2.setStack(impDF2.getStack(), wavelengths,
										zSlicesDF1, stkNSlicesDF
										/ (wavelengths * zSlicesDF1));
								ciDF2 = new CompositeImage(impDF2);
								if (wavelengths > 1)
									ciDF2.setMode(CompositeImage.COMPOSITE);
								else
									ciDF2.setMode(CompositeImage.GRAYSCALE);
								// THIS May? WORK!
								int oldW = win.getWidth();
								int oldH = win.getHeight();
								int oldC = win.getImagePlus().getChannel();
								int oldZ = win.getImagePlus().getSlice();
								int oldT = win.getImagePlus().getFrame();
								double oldMin = win.getImagePlus()
										.getDisplayRangeMin();
								double oldMax = win.getImagePlus()
										.getDisplayRangeMax();

								ciDF2.setWindow(win);
								win.updateImage(ciDF2);
								win.setSize(oldW, oldH);
								((StackWindow) win).addScrollbars(ciDF2);
								win.getImagePlus().updateAndRepaintWindow();
								win.getImagePlus().setPosition(oldC, oldZ, oldT);
								win.getImagePlus().setDisplayRange(oldMin, oldMax);
								win.setSize(win.getSize().width,
										win.getSize().height + 5);

								// *******************
							}
						}
					}
				}
			}
		}
	}

	public Polygon rotatePolygon(Polygon p1, double angle) {
		double theta = angle * Math.PI / 180;
		double xcenter = p1.getBounds().getCenterX();
		double ycenter = p1.getBounds().getCenterY();
		for (int v = 0; v < p1.xpoints.length; v++) {
			double dx = p1.xpoints[v] - xcenter;
			double dy = ycenter - p1.ypoints[v];
			double r = Math.sqrt(dx * dx + dy * dy);
			double a = Math.atan2(dy, dx);
			p1.xpoints[v] = (int) (xcenter + r * Math.cos(a + theta));
			p1.ypoints[v] = (int) (ycenter - r * Math.sin(a + theta));
		}
		return p1;
	}
	
//	{"LaserExposure_ms":"2.5","MVRotations":"0_90_0_90","SPIMtype":"diSPIM","UUID":"ea4d0baa-8f54-4937-82f7-4d1e62492be5","SPIMAcqSettings":"{\n  \"spimMode\": \"PIEZO_SLICE_SCAN\",\n  \"isStageScanning\": false,\n  \"useTimepoints\": true,\n  \"numTimepoints\": 5,\n  \"timepointInterval\": 15.0,\n  \"useMultiPositions\": true,\n  \"useChannels\": true,\n  \"channelMode\": \"VOLUME_HW\",\n  \"numChannels\": 2,\n  \"channels\": [\n    {\n      \"useChannel_\": true,\n      \"group_\": \"Channel\",\n      \"config_\": \"488 nm\"\n    },\n    {\n      \"useChannel_\": true,\n      \"group_\": \"Channel\",\n      \"config_\": \"561 nm\"\n    }\n  ],\n  \"channelGroup\": \"Channel\",\n  \"useAutofocus\": false,\n  \"numSides\": 2,\n  \"firstSideIsA\": true,\n  \"delayBeforeSide\": 50.0,\n  \"numSlices\": 50,\n  \"stepSizeUm\": 1.0,\n  \"minimizeSlicePeriod\": true,\n  \"desiredSlicePeriod\": 5.5,\n  \"desiredLightExposure\": 2.5,\n  \"centerAtCurrentZ\": false,\n  \"sliceTiming\": {\n    \"scanDelay\": 4.5,\n    \"scanNum\": 1,\n    \"scanPeriod\": 3.0,\n    \"laserDelay\": 5.5,\n    \"laserDuration\": 2.5,\n    \"cameraDelay\": 2.75,\n    \"cameraDuration\": 1.0,\n    \"cameraExposure\": 5.35,\n    \"sliceDuration\": 8.0,\n    \"valid\": true\n  },\n  \"cameraMode\": \"EDGE\",\n  \"hardwareTimepoints\": false,\n  \"separateTimepoints\": true\n}

	public String open_diSPIMheaderAsString(String path) {
		if (path==null || path.equals("")) {
			OpenDialog od = new OpenDialog("Open Text File", "");
			String directory = od.getDirectory();
			String name = od.getFileName();
			if (name==null) return null;
			path = directory + name;
		}
		String str = "";
		File file = new File(path);
		if (!file.exists())
			return "Error: file not found";
		try {
			StringBuffer sb = new StringBuffer(5000);
			BufferedReader r = new BufferedReader(new FileReader(file));
			String s ="";
			while (!s.contains("��4 �")) {
				s=r.readLine();
				if (s==null)
					break;
//				else
//					IJ.log(s);
//					sb.append(s+"\n");
			}
			r.close();
//			str = new String(sb);
			str = s;
		}
		catch (Exception e) {
			str = "Error: "+e.getMessage();
		}
		return str;
	}

}

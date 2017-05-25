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
	private String[] diSPIM_MM_ChContrastMinStrings;
	private String[] diSPIM_MM_ChContrastMaxStrings;
	private String dimOrder;
	private WG_Uploader wgUploadJob;
	private boolean uploadPending;
	private boolean uploadRunning;
	
	public boolean isDoDecon() {
		return doDecon;
	}

	public void setDoDecon(boolean doDecon) {
		this.doDecon = doDecon;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
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
		//		ImagePlus impA = null;
		//		ImagePlus impB = null;
		//		ImagePlus impDF1 = null;
		//		ImagePlus impDF2 = null;
		//		CompositeImage ciDF1 = null;
		//		CompositeImage ciDF2 = null;
		ImagePlus[] impAs = new ImagePlus[1];
		ImagePlus[] impBs  = new ImagePlus[1];
		ImagePlus[] impDF1s  = new ImagePlus[1];
		ImagePlus[] impDF2s  = new ImagePlus[1];
		CompositeImage[] ciDF1s  = new CompositeImage[1];
		CompositeImage[] ciDF2s  = new CompositeImage[1];
		Roi[] roiAs  = new Roi[1];
		Roi[] roiBs  = new Roi[1];
		int[] wasFrameA = new int[1];
		int[] wasFrameB = new int[1];
		int[] wasSliceA = new int[1];
		int[] wasSliceB = new int[1];
		int[] wasChannelA = new int[1];
		int[] wasChannelB = new int[1];


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

				if (keyString =="")
					keyString = dirOrOMETiffFile.list()[dirOrOMETiffFile.list().length/2].split("_")[0];

				//Reading in diSPIM header from MM tiffs vvvvvv

				readInMMdiSPIMheader(dirOrOMETiffFile);

				//Reading in diSPIM header from MM tiffs ^^^^^^

				dimOrder = (diSPIM_MM_channelMode.contains("VOLUME")?"xyzct":"xyczt");

				wavelengths = diSPIM_MM_numChannels;
				vWidth = diSPIM_MM_PixelSize_um;
				vHeight = diSPIM_MM_PixelSize_um;
				vDepthRaw = diSPIM_MM_zStep_um;

				impAs = new ImagePlus[pDim];
				impBs = new ImagePlus[pDim];
				roiAs = new Roi[pDim];
				roiBs = new Roi[pDim];
				impDF1s  = new ImagePlus[pDim];
				impDF2s  = new ImagePlus[pDim];
				ciDF1s  = new CompositeImage[pDim];
				ciDF2s  = new CompositeImage[pDim];
				wasFrameA = new int[pDim];
				wasFrameB = new int[pDim];
				wasSliceA = new int[pDim];
				wasSliceB = new int[pDim];
				wasChannelA = new int[pDim];
				wasChannelB = new int[pDim];

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
							dirOrOMETiff, dimOrder, keyString, cDim, zDim, tDim, vDim, pos,
							false, false);
					stackBs[pos] = new MultiFileInfoVirtualStack(
							dirOrOMETiff, dimOrder, keyString, cDim, zDim, tDim, vDim, pos,
							true, false);

					impAs[pos].setStack(stackAs[pos]);
					Calibration calA = impAs[pos].getCalibration();
					calA.pixelWidth = vWidth;
					calA.pixelHeight = vHeight;
					calA.pixelDepth = vDepthRaw;
					calA.setUnit(vUnit);


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
					//					impA = impAs[0];
					impAs[0].setStack(new ListVirtualStack(dirOrOMETiff
							+ "Big5DFileListA.txt"));

					int stkNSlices = impAs[0].getNSlices();

					impAs[0].setTitle("SPIMA: " + dirOrOMETiff);

					impAs[0].setDimensions(wavelengths, zSlices, stkNSlices
							/ (wavelengths * zSlices));
					IJ.log(wavelengths + " " + zSlices + " " + stkNSlices
							/ (wavelengths * zSlices));
					if (wavelengths > 1) {
						impAs[0] = new CompositeImage(impAs[0]);
						while (!impAs[0].isComposite()) {
							IJ.wait(100);
						}
					}
					Calibration cal = impAs[0].getCalibration();
					cal.pixelWidth = vWidth;
					cal.pixelHeight = vHeight;
					cal.pixelDepth = vDepthRaw;
					cal.setUnit(vUnit);
					if (stageScan)
						impAs[0].getStack().setSkewXperZ(
								-cal.pixelDepth / cal.pixelWidth);

					impAs[0].setPosition(wavelengths, zSlices / 2, stkNSlices
							/ (wavelengths * zSlices));

					impAs[0].setPosition(1, zSlices / 2, stkNSlices
							/ (wavelengths * zSlices));

					if (impAs[0].isComposite())
						((CompositeImage) impAs[0])
						.setMode(CompositeImage.COMPOSITE);
					impAs[0].show();

				}

				if ((new File(dirOrOMETiff + "Big5DFileListB.txt")).length() > 0) {
					// IJ.run("Stack From List...",
					// "open="+dir+"Big5DFileListB.txt use");
					//					impB = impBs[0];
					impBs[0].setStack(new ListVirtualStack(dirOrOMETiff
							+ "Big5DFileListB.txt"));
					int stkNSlices = impBs[0].getNSlices();

					impBs[0].setTitle("SPIMB: " + dirOrOMETiff);

					impBs[0].setDimensions(wavelengths, zSlices, stkNSlices
							/ (wavelengths * zSlices));
					IJ.log(wavelengths + " " + zSlices + " " + stkNSlices
							/ (wavelengths * zSlices));
					if (wavelengths > 1) {
						impBs[0] = new CompositeImage(impBs[0]);
						while (!impBs[0].isComposite()) {
							IJ.wait(100);
						}
					}
					Calibration cal = impBs[0].getCalibration();
					cal.pixelWidth = vWidth;
					cal.pixelHeight = vHeight;
					cal.pixelDepth = vDepthRaw;
					cal.setUnit(vUnit);
					if (stageScan)
						impBs[0].getStack().setSkewXperZ(
								-cal.pixelDepth / cal.pixelWidth);

					impBs[0].setPosition(wavelengths, zSlices / 2, stkNSlices
							/ (wavelengths * zSlices));

					impBs[0].setPosition(1, zSlices / 2, stkNSlices
							/ (wavelengths * zSlices));

					if (impBs[0].isComposite())
						((CompositeImage) impBs[0])
						.setMode(CompositeImage.COMPOSITE);
					impBs[0].show();

				}
			}
		} else if (dirOrOMETiff.endsWith(".ome.tif")) {
			TiffDecoder tdA = new TiffDecoder("",dirOrOMETiff);
			TiffDecoder tdB = new TiffDecoder("",dirOrOMETiff);

			String mmPath = (new File(dirOrOMETiff)).getParent();			

			//			impA = new ImagePlus();
			impAs[0].setStack(new MultiFileInfoVirtualStack(mmPath, "MMStack", false));
			//				impAs[0].setStack(new FileInfoVirtualStack(tdB.getTiffInfo(0), false));
			int stackSize = impAs[0].getNSlices();
			int nChannels = wavelengths*2;
			int nSlices = zSlices;
			int nFrames = (int)Math.floor((double)stackSize/(nChannels*nSlices));
			dirOrOMETiff = ((MultiFileInfoVirtualStack)impAs[0].getStack()).getFivStacks().get(0).getInfo()[0].directory +
					File.separator +
					((MultiFileInfoVirtualStack)impAs[0].getStack()).getFivStacks().get(0).getInfo()[0].fileName;

			impAs[0].setTitle("SPIMB: "+dirOrOMETiff);

			if (nChannels*nSlices*nFrames!=stackSize) {
				if (nChannels*nSlices*nFrames>stackSize) {
					for (int a=stackSize;a<nChannels*nSlices*nFrames;a++) {
						if (impAs[0].getStack().isVirtual())
							((VirtualStack)impAs[0].getStack()).addSlice("blank slice");
						else
							impAs[0].getStack().addSlice(impAs[0].getProcessor().createProcessor(impAs[0].getWidth(), impAs[0].getHeight()));
					}
				} else if (nChannels*nSlices*nFrames<stackSize) {
					for (int a=nChannels*nSlices*nFrames;a<stackSize;a++) {
						((MultiFileInfoVirtualStack)impAs[0].getStack()).deleteSlice(nChannels*nSlices*nFrames);
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
							((MultiFileInfoVirtualStack)impAs[0].getStack()).deleteSlice(target);
						}
					}
				}
			} else {
				for (int t=nFrames-1;t>=0;t--) {
					for (int s=nSlices*nChannels-1;s>=0;s--) {
						int target = t*nChannels*nSlices + s+1;
						if (s<nSlices*nChannels/2) { 
							((MultiFileInfoVirtualStack)impAs[0].getStack()).deleteSlice(target);
						}
					}
				}
			}
			impAs[0].setStack(impAs[0].getImageStack());

			impAs[0].setDimensions(wavelengths, nSlices, nFrames);

			if (nChannels > 1){
				impAs[0] = new CompositeImage(impAs[0]);
				while (!impAs[0].isComposite()) {
					IJ.wait(100);
				}
			}
			Calibration cal = impAs[0].getCalibration();
			cal.pixelWidth = vWidth;
			cal.pixelHeight = vHeight;
			cal.pixelDepth = vDepthRaw;
			cal.setUnit(vUnit);

			impAs[0].setPosition(wavelengths, nSlices, nFrames);	

			impAs[0].setPosition(1, nSlices/2, nFrames/2);	

			if (impAs[0].isComposite())
				((CompositeImage)impAs[0]).setMode(CompositeImage.COMPOSITE);
			impAs[0].setFileInfo(new FileInfo());
			impAs[0].getOriginalFileInfo().fileName = dirOrOMETiff;
			impAs[0].getOriginalFileInfo().directory = dirOrOMETiff;
			impAs[0].show();


			//			impB = new ImagePlus();
			impBs[0].setStack(new MultiFileInfoVirtualStack(mmPath, "MMStack", false));
			//				impBs[0].setStack(new FileInfoVirtualStack(tdA.getTiffInfo(0), false));
			stackSize = impBs[0].getStack().getSize();
			nChannels = wavelengths*2;
			nSlices = zSlices;
			nFrames = (int)Math.floor((double)stackSize/(nChannels*nSlices));

			impBs[0].setTitle("SPIMA: "+dirOrOMETiff);

			if (nChannels*nSlices*nFrames!=stackSize) {
				if (nChannels*nSlices*nFrames>stackSize) {
					for (int a=stackSize;a<nChannels*nSlices*nFrames;a++) {
						if (impBs[0].getStack().isVirtual())
							((VirtualStack)impBs[0].getStack()).addSlice("blank slice");
						else
							impBs[0].getStack().addSlice(impBs[0].getProcessor().createProcessor(impBs[0].getWidth(), impBs[0].getHeight()));
					}
				} else if (nChannels*nSlices*nFrames<stackSize) {
					for (int a=nChannels*nSlices*nFrames;a<stackSize;a++) {
						((MultiFileInfoVirtualStack)impBs[0].getStack()).deleteSlice(nChannels*nSlices*nFrames);
						stackSize = impBs[0].getStack().getSize();					}
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
							((MultiFileInfoVirtualStack)impBs[0].getStack()).deleteSlice(target);
						}
					}
				}
			} else {
				for (int t=nFrames-1;t>=0;t--) {
					for (int s=nSlices*nChannels-1;s>=0;s--) {
						int target = t*nChannels*nSlices + s+1;
						if (s>=nSlices*nChannels/2) { 
							((MultiFileInfoVirtualStack)impBs[0].getStack()).deleteSlice(target);
						}
					}
				}
			}

			impBs[0].setStack(impBs[0].getImageStack());

			impBs[0].setDimensions(wavelengths, nSlices, nFrames);

			if (nChannels > 1){
				impBs[0] = new CompositeImage(impBs[0]);
				while (!impBs[0].isComposite()) {
					IJ.wait(100);
				}
			}
			cal = impBs[0].getCalibration();
			cal.pixelWidth = vWidth;
			cal.pixelHeight = vHeight;
			cal.pixelDepth = vDepthRaw;
			cal.setUnit(vUnit);

			impBs[0].setPosition(wavelengths, nSlices, nFrames);	

			impBs[0].setPosition(1, nSlices/2, nFrames/2);	

			if (impBs[0].isComposite())
				((CompositeImage)impBs[0]).setMode(CompositeImage.COMPOSITE);
			impBs[0].setFileInfo(new FileInfo());
			impBs[0].getOriginalFileInfo().fileName = dirOrOMETiff;
			impBs[0].getOriginalFileInfo().directory = dirOrOMETiff;
			impBs[0].show();

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
			/*impA = */ impAs[0] = WindowManager.getCurrentImage();
			Calibration calA = impAs[0].getCalibration();
			calA.pixelWidth = vWidth;
			calA.pixelHeight = vHeight;
			calA.pixelDepth = vDepthRaw;
			calA.setUnit(vUnit);
			if (stageScan)
				impAs[0].getStack()
				.setSkewXperZ(-calA.pixelDepth / calA.pixelWidth);
			impAs[0].setTitle("SPIMA: " + impAs[0].getTitle());

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
			/*impB = */ impBs[0] = WindowManager.getCurrentImage();
			Calibration calB = impBs[0].getCalibration();
			calB.pixelWidth = vWidth;
			calB.pixelHeight = vHeight;
			calB.pixelDepth = vDepthRaw;
			calB.setUnit(vUnit);
			if (stageScan)
				impBs[0].getStack().setSkewXperZ(calB.pixelDepth / calB.pixelWidth);
			impBs[0].setTitle("SPIMB: " + impBs[0].getTitle());

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
		if (wgUploadJob == null) 
			wgUploadJob = new WG_Uploader();
		if (wgUploadJob.getNewUploadProcess() != null)
			uploadRunning = wgUploadJob.getNewUploadProcess().isAlive();
		if(!uploadRunning)	{
			wgUploadJob = new WG_Uploader();
			wgUploadJob.run(dirOrOMETiff);
		}


		if (doDecon) {

			for (int pos=0; pos<pDim; pos++) {

				//				impA = impAs[pos];
				//				impB = impBs[pos];

				roiAs[pos] = impAs[pos].getRoi();
				roiBs[pos] = impBs[pos].getRoi();

				while (roiAs[pos] == null || roiBs[pos] == null) {
					WindowManager.setTempCurrentImage(impAs[pos]);
					if (roiAs[pos] == null) {
						if (!((new File(savePath +  "Pos" + pos + "A_crop.roi")).canRead())) {
							IJ.makeRectangle(0, 0, cropWidth, cropHeight);
							roiAs[pos] = impAs[pos].getRoi();
						} else {
							IJ.open(savePath +  "Pos" + pos + "A_crop.roi");
							roiAs[pos] = impAs[pos].getRoi();
							cropWidth = roiAs[pos].getBounds().width;
							cropHeight = roiAs[pos].getBounds().height;
						}
					} else if (roiAs[pos].getType() != Roi.RECTANGLE
							&& roiAs[pos].getFeretValues()[0] > cropHeight
							* impAs[pos].getCalibration().pixelHeight
							|| (roiAs[pos].getType() == Roi.RECTANGLE && roiAs[pos].getBounds()
							.getHeight() > cropHeight)
							|| (roiAs[pos].getType() == Roi.RECTANGLE && roiAs[pos].getBounds()
							.getWidth() > cropHeight)) {
						impAs[pos].setRoi(roiAs[pos].getBounds().x
								+ (roiAs[pos].getBounds().width - cropWidth) / 2,
								roiAs[pos].getBounds().y
								+ (roiAs[pos].getBounds().height - cropHeight)
								/ 2, cropWidth, cropHeight);
						impAs[pos].setRoi(
								roiAs[pos].getBounds().x < 0 ? 0 : roiAs[pos].getBounds().x,
										roiAs[pos].getBounds().y < 0 ? 0 : roiAs[pos].getBounds().y,
												cropWidth, cropHeight);
					}
					WindowManager.setTempCurrentImage(impBs[pos]);
					if (roiBs[pos] == null) {
						if (!((new File(savePath + "Pos" + pos + "B_crop.roi")).canRead())) {
							IJ.makeRectangle(0, 0, cropWidth, cropHeight);
							roiBs[pos] = impBs[pos].getRoi();
						} else {
							IJ.open(savePath +  "Pos" + pos + "B_crop.roi");
							roiBs[pos] = impBs[pos].getRoi();
						}
					} else if (roiBs[pos].getType() != Roi.RECTANGLE
							&& roiBs[pos].getFeretValues()[0] > cropHeight
							* impBs[pos].getCalibration().pixelHeight
							|| (roiBs[pos].getType() == Roi.RECTANGLE && roiBs[pos].getBounds()
							.getWidth() > cropHeight)
							|| (roiBs[pos].getType() == Roi.RECTANGLE && roiBs[pos].getBounds()
							.getHeight() > cropHeight)) {
						impBs[pos].setRoi(roiBs[pos].getBounds().x
								+ (roiBs[pos].getBounds().width - cropWidth) / 2,
								roiBs[pos].getBounds().y
								+ (roiBs[pos].getBounds().height - cropHeight)
								/ 2, cropWidth, cropHeight);
						impBs[pos].setRoi(
								roiBs[pos].getBounds().x < 0 ? 0 : roiBs[pos].getBounds().x,
										roiBs[pos].getBounds().y < 0 ? 0 : roiBs[pos].getBounds().y,
												cropWidth, cropHeight);
					}
					WindowManager.setTempCurrentImage(null);
				}
			}

			IJ.runMacro("waitForUser(\"Select the regions containing the embryo"
					+ "\\\n for deconvolution/fusion processing."
					// + "\\\nAlso, set the minimum Brightness limit."
					// +
					// "\\\nWhen you are then ready, click OK here to commence processing."
					+ "\");");

			for (int pos=0; pos<pDim; pos++) {

				//					impA = impAs[pos];
				//					impB = impBs[pos];

				roiAs[pos] = impAs[pos].getRoi();
				roiBs[pos] = impBs[pos].getRoi();


				// int[] minLimit = {(int) impAs[pos].getDisplayRangeMin(), (int)
				// impBs[pos].getDisplayRangeMin()};
				// if (impAs[pos].isComposite()) {
				// minLimit = new int[impAs[pos].getNChannels()*2];
				// for (int c=1; c<=impAs[pos].getNChannels(); c++) {
				// minLimit[c-1] = (int)
				// ((CompositeImage)impAs[pos]).getProcessor(c).getMin();
				// minLimit[c+1] = (int)
				// ((CompositeImage)impBs[pos]).getProcessor(c).getMin();
				// }
				// }

				IJ.saveAs(impAs[pos], "Selection", savePath +  "Pos" + pos + "A_crop.roi");
				IJ.saveAs(impBs[pos], "Selection", savePath +  "Pos" + pos + "B_crop.roi");

			}


			for (int pos=0; pos<pDim; pos++) {

				//				impA = impAs[pos];
				//				impB = impBs[pos];

				roiAs[pos] = impAs[pos].getRoi();
				roiBs[pos] = impBs[pos].getRoi();

				wasFrameA[pos] = impAs[pos].getFrame();
				wasFrameB[pos] = impBs[pos].getFrame();
				wasSliceA[pos] = impAs[pos].getSlice();
				wasSliceB[pos] = impBs[pos].getSlice();
				wasChannelA[pos] = impAs[pos].getChannel();
				wasChannelB[pos] = impBs[pos].getChannel();

				if ((new File(savePath)).canRead()) {
					if (impDF1s[pos] == null) {
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
							impDF1s[pos] = new ImagePlus();
							impDF1s[pos].setStack(
									"Decon-Fuse"
											+ impAs[pos].getTitle().replace(
													impAs[pos].getTitle().split(":")[0],
													""), deconmfivs);
							impDF1s[pos].setFileInfo(new FileInfo());
							// impDF1s[pos].getOriginalFileInfo().directory = (new
							// File(dirOrOMETiff)).isDirectory()?dirOrOMETiff:((new
							// File(dirOrOMETiff)).getParent()+File.separator);
							impDF1s[pos].getOriginalFileInfo().directory = dirOrOMETiff;
							int stkNSlicesDF = impDF1s[pos].getStackSize();
							int zSlicesDF1 = deconmfivs.getFivStacks().get(0)
									.getSize();
							impDF1s[pos].setOpenAsHyperStack(true);
							impDF1s[pos].setStack(impDF1s[pos].getStack(), wavelengths,
									zSlicesDF1, stkNSlicesDF
									/ (wavelengths * zSlicesDF1));
							ciDF1s[pos] = new CompositeImage(impDF1s[pos]);
							if (wavelengths > 1)
								ciDF1s[pos].setMode(CompositeImage.COMPOSITE);
							else
								ciDF1s[pos].setMode(CompositeImage.GRAYSCALE);
							ciDF1s[pos].show();
							win = ciDF1s[pos].getWindow();
						}
					}
				}
			}


			String[] frameFileNames = new String[impAs[0].getNFrames() + 1];

			for (int f = 1; f <= impAs[0].getNFrames(); f++) {
				for (int pos=0; pos<pDim; pos++) {

					impAs[pos].setPositionWithoutUpdate(impAs[pos].getChannel(),
							impAs[pos].getSlice(), f);

					if (impAs[pos].getStack() instanceof ListVirtualStack)
						frameFileNames[f] = ((ListVirtualStack) impAs[pos].getStack())
						.getDirectory(impAs[pos].getCurrentSlice());
					else if (impAs[pos].getStack() instanceof FileInfoVirtualStack
							|| impAs[pos].getStack() instanceof MultiFileInfoVirtualStack)
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
						impAs[pos].getWindow().setEnabled(false);
						for (int i = 1; i <= impAs[pos].getNSlices(); i++) {
							impAs[pos].setPositionWithoutUpdate(1, i, f);
							Roi impRoi = (Roi) roiAs[pos].clone();
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

									ImageProcessor ip1 = impAs[pos].getProcessor().duplicate();
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
										impAs[pos].setPositionWithoutUpdate(2, i, f);
										ImageProcessor ip2 = impAs[pos].getProcessor()
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
						impAs[pos].getWindow().setEnabled(true);
						ImagePlus impXA1 = new ImagePlus();
						impXA1.setStack(stackA1);
						impXA1.setCalibration(impAs[pos].getCalibration());
						// impXA1.getCalibration().pixelDepth =
						// impXA1.getCalibration().pixelWidth;
						IJ.saveAs(impXA1, "Tiff", savePath + "Pos"+pos+ "_SPIMA_Ch1_processed"
								+ File.separator + frameFileNames[f]
										+ File.separator + frameFileNames[f] + ".tif");
						if (wavelengths == 2) {
							ImagePlus impXA2 = new ImagePlus();
							impXA2.setStack(stackA2);
							impXA2.setCalibration(impAs[pos].getCalibration());
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
						impBs[pos].getWindow().setEnabled(false);
						for (int i = 1; i <= impBs[pos].getNSlices(); i++) {
							impBs[pos].setPositionWithoutUpdate(1, i, f);
							Roi impRoi = (Roi) roiBs[pos].clone();
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

									ImageProcessor ip1 = impBs[pos].getProcessor().duplicate();
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
										impBs[pos].setPositionWithoutUpdate(2, i, f);
										ImageProcessor ip2 = impBs[pos].getProcessor()
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
						impBs[pos].getWindow().setEnabled(true);
						ImagePlus impXB1 = new ImagePlus();
						impXB1.setStack(stackB1);
						impXB1.setCalibration(impBs[pos].getCalibration());
						// impXB1.getCalibration().pixelDepth =
						// impXB1.getCalibration().pixelWidth;
						IJ.saveAs(impXB1, "Tiff", savePath + "Pos"+pos+ "_SPIMB_Ch1_processed"
								+ File.separator + frameFileNames[f]
										+ File.separator + frameFileNames[f] + ".tif");
						if (wavelengths == 2) {
							ImagePlus impXB2 = new ImagePlus();
							impXB2.setStack(stackB2);
							impXB2.setCalibration(impBs[pos].getCalibration());
							// impXB2.getCalibration().pixelDepth =
							// impXB2.getCalibration().pixelWidth;
							IJ.saveAs(impXB2, "Tiff", savePath
									+ "Pos"+pos+ "_SPIMB_Ch2_processed" + File.separator
									+ frameFileNames[f] + File.separator
									+ frameFileNames[f] + ".tif");
						}

					}


					final String[] frameFileNamesFinal = frameFileNames;

					impAs[pos].setPosition(wasChannelA[pos], wasSliceA[pos], wasFrameA[pos]);
					impBs[pos].setPosition(wasChannelB[pos], wasSliceB[pos], wasFrameB[pos]);

					final int ff = f;

					timecode = "" + (new Date()).getTime();
					final String ftimecode = timecode;

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
										+ "\", \"spimBFileDir string "
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMA_Ch"
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
														+ "\", \"baseImage string "
														+ frameFileNames[f]
																//																+ "\", \"base_rotation int -1\", \"transform_rotation int 5\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
																+ "\", \"base_rotation int -1\", \"transform_rotation int 4\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
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
										+ frameFileNamesFinal[ff] + ftimecode
										+ ".bat\");" + "delSct = File.delete(\""
										+ tempDir.replace("\\", "\\\\")
										+ "GenerateFusion1"
										+ frameFileNamesFinal[ff] + ftimecode
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
											+ "\", \"spimBFileDir string "
											+ savePath.replace("\\", "\\\\")
											+ "Pos"+pos+ "_SPIMA_Ch"
											+ slaveChannel
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
															+ "\", \"baseImage string "
															+ frameFileNames[f]
																	//																	+ "\", \"base_rotation int -1\", \"transform_rotation int 5\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
																	+ "\", \"base_rotation int -1\", \"transform_rotation int 4\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
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
											+ frameFileNamesFinal[ff] + ftimecode
											+ ".bat\");"
											+ "delSct = File.delete(\""
											+ tempDir.replace("\\", "\\\\")
											+ "GenerateFusion2"
											+ frameFileNamesFinal[ff] + ftimecode
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
						uploadPending = true;
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
						while (new File(recentestA).list().length < pDim) {
							IJ.wait(1000);
							IJ.log(".");
						}
						
						for (int nf =0; nf< pDim; nf++) {
							if (nf>0 && new File(new File(recentestA).list()[nf]).length() != new File(new File(recentestA).list()[0]).length())
								nf--;
						}
						IJ.log(recentestA + " complete");

						MultiFileInfoVirtualStack[] stackAs = new MultiFileInfoVirtualStack[pDim];
						MultiFileInfoVirtualStack[] stackBs = new MultiFileInfoVirtualStack[pDim];

						for (int pos=0; pos<pDim; pos++) {

							win = impAs[pos].getWindow();
							double zoomA = win.getCanvas().getMagnification();
							int cA = impAs[pos].getChannel();
							int zA = impAs[pos].getSlice();
							int tA = impAs[pos].getFrame();
							boolean tailing = tA==impAs[pos].getNFrames();
							tDim = listA.length;
							Calibration calA = impAs[pos].getCalibration();
							calA.pixelWidth = vWidth;
							calA.pixelHeight = vHeight;
							calA.pixelDepth = vDepthRaw;
							calA.setUnit(vUnit);

							stackAs[pos] = new MultiFileInfoVirtualStack(
									dirOrOMETiff, dimOrder, keyString, cDim, zDim, tDim, vDim, pos,
									false, false);

							if (stageScan)
								stackAs[pos].setSkewXperZ(
										calA.pixelDepth / calA.pixelWidth);
							
							impAs[pos].flush();
							impAs[pos] = new CompositeImage(new ImagePlus(impAs[pos].getTitle(), stackAs[pos]));
							impAs[pos].setOpenAsHyperStack(true);
							impAs[pos].setDimensions(cDim, zDim, tDim);
							win.setImage(impAs[pos]);
							impAs[pos].setPosition(cA, zA, tailing? impAs[pos].getNFrames() : tA);
							((CompositeImage)impAs[pos]).setMode(CompositeImage.COMPOSITE);
							win.getCanvas().setMagnification(zoomA);
							win.pack();
							
							win = impBs[pos].getWindow();
							double zoomB = win.getCanvas().getMagnification();
							int cB = impBs[pos].getChannel();
							int zB = impBs[pos].getSlice();
							int tB = impBs[pos].getFrame();
							tailing = tB==impBs[pos].getNFrames();
							tDim = listA.length;
							Calibration calB = impBs[pos].getCalibration();
							calB.pixelWidth = vWidth;
							calB.pixelHeight = vHeight;
							calB.pixelDepth = vDepthRaw;
							calB.setUnit(vUnit);

							stackBs[pos] = new MultiFileInfoVirtualStack(
									dirOrOMETiff, dimOrder, keyString, cDim, zDim, tDim, vDim, pos,
									true, false);

							if (stageScan)
								stackBs[pos].setSkewXperZ(
										calB.pixelDepth / calB.pixelWidth);
							
							impBs[pos].flush();
							impBs[pos] = new CompositeImage(new ImagePlus(impBs[pos].getTitle(), stackBs[pos]));
							impBs[pos].setOpenAsHyperStack(true);
							impBs[pos].setDimensions(cDim, zDim, tDim);
							win.setImage(impBs[pos]);

							impBs[pos].setPosition(cB, zB, tailing? impBs[pos].getNFrames() : tB);
							((CompositeImage)impBs[pos]).setMode(CompositeImage.COMPOSITE);
							win.getCanvas().setMagnification(zoomB);
							win.pack();

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


						if (wasSynched) {
							SyncWindows sw = new SyncWindows();
							for (ImagePlus impS : synchedImpsArrayList) {
								sw.addImp(impS);
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
						IJ.log("NEW DATA WRITTEN");
						uploadPending = true;

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

						for (int pos=0; pos<pDim; pos++) {

							win = impAs[pos].getWindow();
							int cA = impAs[pos].getChannel();
							int zA = impAs[pos].getSlice();
							int tA = impAs[pos].getFrame();
							ListVirtualStack stackA = new ListVirtualStack(dirOrOMETiff
									+ "Big5DFileListA.txt");
							int stkNSlicesA = stackA.getSize();
							impAs[pos].setStack(stackA, wavelengths, zSlices, stkNSlicesA
									/ (wavelengths * zSlices));
							if (stageScan)
								impAs[pos].getStack().setSkewXperZ(
										-impAs[pos].getCalibration().pixelDepth
										/ impAs[pos].getCalibration().pixelWidth);
							impAs[pos].setPosition(cA, zA,
									tA == impAs[pos].getNFrames() - 1 ? impAs[pos].getNFrames() : tA);
							//impAs[pos].setWindow(win);
							win.setImage(impAs[pos]);


							win = impBs[pos].getWindow();
							int cB = impBs[pos].getChannel();
							int zB = impBs[pos].getSlice();
							int tB = impBs[pos].getFrame();
							ListVirtualStack stackB = new ListVirtualStack(dirOrOMETiff
									+ "Big5DFileListB.txt");
							int stkNSlicesB = stackB.getSize();
							impBs[pos].setStack(stackB, wavelengths, zSlices, stkNSlicesB
									/ (wavelengths * zSlices));
							if (stageScan)
								impBs[pos].getStack().setSkewXperZ(
										-impBs[pos].getCalibration().pixelDepth
										/ impBs[pos].getCalibration().pixelWidth);
							impBs[pos].setPosition(cB, zB,
									tB == impBs[pos].getNFrames() - 1 ? impBs[pos].getNFrames() : tB);
//							impBs[pos].setWindow(win);
							win.setImage(impBs[pos]);

						}
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
					for (int pos=0; pos<pDim; pos++) {

						int cA = impAs[pos].getChannel();
						int zA = impAs[pos].getSlice();
						int tA = impAs[pos].getFrame();
						int cB = impBs[pos].getChannel();
						int zB = impBs[pos].getSlice();
						int tB = impBs[pos].getFrame();
						if (impAs[pos].isComposite())
							modeA = ((CompositeImage) impAs[pos]).getCompositeMode();
						if (impBs[pos].isComposite())
							modeB = ((CompositeImage) impBs[pos]).getCompositeMode();

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
						win = impAs[pos].getWindow();
						ColorModel cmA = impAs[pos].getProcessor().getColorModel();
						double dminA = impAs[pos].getProcessor().getMin();
						double dmaxA = impAs[pos].getProcessor().getMax();
						impAs[pos].setStack(impTmpA.getStack(), wavelengths, zSlices, impTmpA
								.getStack().getSize() / (wavelengths * zSlices));
						impAs[pos].getProcessor().setColorModel(cmA);
						impAs[pos].getProcessor().setMinAndMax(dminA, dmaxA);
						if (stageScan)
							impAs[pos].getStack().setSkewXperZ(
									-impBs[pos].getCalibration().pixelDepth
									/ impBs[pos].getCalibration().pixelWidth);

						impAs[pos].setPosition(cA, zA,
								tA == impAs[pos].getNFrames() - 1 ? impAs[pos].getNFrames() : tA);
						//impAs[pos].setWindow(win);
							win.setImage(impAs[pos]);


						// IJ.run("Image Sequence...",
						// "open=["+dirOrOMETiff+"] number="+ newLength
						// +" starting=1 increment=1 scale=100 file=Cam1 or=[] sort use");
						FolderOpener foB = new FolderOpener();
						foB.openAsVirtualStack(true);
						foB.sortFileNames(true);
						foB.setFilter("Cam1");
						ImagePlus impTmpB = foB.openFolder(new File(dirOrOMETiff)
						.getParent());

						win = impBs[pos].getWindow();
						ColorModel cmB = impBs[pos].getProcessor().getColorModel();
						double dminB = impBs[pos].getProcessor().getMin();
						double dmaxB = impBs[pos].getProcessor().getMax();
						impBs[pos].setStack(impTmpB.getStack(), wavelengths, zSlices, impTmpB
								.getStack().getSize() / (wavelengths * zSlices));
						impBs[pos].getProcessor().setColorModel(cmB);
						impBs[pos].getProcessor().setMinAndMax(dminB, dmaxB);
						if (stageScan)
							impBs[pos].getStack().setSkewXperZ(
									impBs[pos].getCalibration().pixelDepth
									/ impBs[pos].getCalibration().pixelWidth);

						impBs[pos].setPosition(cB, zB,
								tB == impBs[pos].getNFrames() - 1 ? impBs[pos].getNFrames() : tB);
						//impBs[pos].setWindow(win);
							win.setImage(impBs[pos]);

					}
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
					IJ.log("NEW DATA WRITTEN");
					uploadPending = true;

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
					for (int pos=0; pos<pDim; pos++) {

						TiffDecoder tdA = new TiffDecoder("", dirOrOMETiff);
						win = impAs[pos].getWindow();
						int cA = impAs[pos].getChannel();
						int zA = impAs[pos].getSlice();
						int tA = impAs[pos].getFrame();

						try {
							impAs[pos].setStack(new FileInfoVirtualStack(tdA.getTiffInfo(0),
									false));
							int stackSize = impAs[pos].getNSlices();
							int nChannels = wavelengths * 2;
							int nSlices = zSlices;
							int nFrames = (int) Math.floor((double) stackSize
									/ (nChannels * nSlices));

							impAs[pos].setTitle("SPIMA: " + dirOrOMETiff);

							if (nChannels * nSlices * nFrames != stackSize) {
								if (nChannels * nSlices * nFrames > stackSize) {
									for (int a = stackSize; a < nChannels * nSlices
											* nFrames; a++) {
										if (impAs[pos].getStack().isVirtual())
											((VirtualStack) impAs[pos].getStack())
											.addSlice("blank slice");
										else
											impAs[pos].getStack().addSlice(
													impAs[pos].getProcessor()
													.createProcessor(
															impAs[pos].getWidth(),
															impAs[pos].getHeight()));
									}
								} else if (nChannels * nSlices * nFrames < stackSize) {
									for (int a = nChannels * nSlices * nFrames; a < stackSize; a++) {
										impAs[pos].getStack().deleteSlice(
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
										impAs[pos].getStack().deleteSlice(target);
									}
								}
							}
							impAs[pos].setStack(impAs[pos].getImageStack());

							impAs[pos].setDimensions(wavelengths, nSlices, nFrames);

							if (nChannels > 1) {
								impAs[pos] = new CompositeImage(impAs[pos]);
								while (!impAs[pos].isComposite()) {
									IJ.wait(100);
									// selectWindow("SPIMB: "+dir);
								}
							}
							Calibration cal = impAs[pos].getCalibration();
							cal.pixelWidth = vWidth;
							cal.pixelHeight = vHeight;
							cal.pixelDepth = vDepthRaw;
							cal.setUnit(vUnit);
							if (stageScan)
								impAs[pos].getStack().setSkewXperZ(
										cal.pixelDepth / cal.pixelWidth);

							impAs[pos].setPosition(wavelengths, nSlices, nFrames);

							// impAs[pos].resetDisplayRange();
							impAs[pos].setPosition(1, nSlices / 2, nFrames / 2);
							// impAs[pos].resetDisplayRange();
							if (impAs[pos].isComposite())
								((CompositeImage) impAs[pos])
								.setMode(CompositeImage.COMPOSITE);
							impAs[pos].setFileInfo(new FileInfo());
							impAs[pos].getOriginalFileInfo().fileName = dirOrOMETiff;
							impAs[pos].getOriginalFileInfo().directory = dirOrOMETiff;

						} catch (IOException e) {
							e.printStackTrace();
						}

						impAs[pos].setPosition(cA, zA,
								tA == impAs[pos].getNFrames() - 1 ? impAs[pos].getNFrames() : tA);
						//impAs[pos].setWindow(win);
							win.setImage(impAs[pos]);


						TiffDecoder tdB = new TiffDecoder("", dirOrOMETiff);
						win = impBs[pos].getWindow();
						int cB = impBs[pos].getChannel();
						int zB = impBs[pos].getSlice();
						int tB = impBs[pos].getFrame();

						try {
							impBs[pos].setStack(new FileInfoVirtualStack(tdB.getTiffInfo(0),
									false));
							int stackSize = impBs[pos].getNSlices();
							int nChannels = wavelengths * 2;
							int nSlices = zSlices;
							int nFrames = (int) Math.floor((double) stackSize
									/ (nChannels * nSlices));

							impBs[pos].setTitle("SPIMB: " + dirOrOMETiff);

							if (nChannels * nSlices * nFrames != stackSize) {
								if (nChannels * nSlices * nFrames > stackSize) {
									for (int a = stackSize; a < nChannels * nSlices
											* nFrames; a++) {
										if (impBs[pos].getStack().isVirtual())
											((VirtualStack) impBs[pos].getStack())
											.addSlice("blank slice");
										else
											impBs[pos].getStack().addSlice(
													impBs[pos].getProcessor()
													.createProcessor(
															impBs[pos].getWidth(),
															impBs[pos].getHeight()));
									}
								} else if (nChannels * nSlices * nFrames < stackSize) {
									for (int a = nChannels * nSlices * nFrames; a < stackSize; a++) {
										impBs[pos].getStack().deleteSlice(
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
										impBs[pos].getStack().deleteSlice(target);
									}
								}
							}
							impBs[pos].setStack(impBs[pos].getImageStack());

							impBs[pos].setDimensions(wavelengths, nSlices, nFrames);

							if (nChannels > 1) {
								impBs[pos] = new CompositeImage(impBs[pos]);
								while (!impBs[pos].isComposite()) {
									IJ.wait(100);
									// selectWindow("SPIMB: "+dir);
								}
							}
							Calibration cal = impBs[pos].getCalibration();
							cal.pixelWidth = vWidth;
							cal.pixelHeight = vHeight;
							cal.pixelDepth = vDepthRaw;
							cal.setUnit(vUnit);
							if (stageScan)
								impBs[pos].getStack().setSkewXperZ(
										-cal.pixelDepth / cal.pixelWidth);

							impBs[pos].setPosition(wavelengths, nSlices, nFrames);

							// impBs[pos].resetDisplayRange();
							impBs[pos].setPosition(1, nSlices / 2, nFrames / 2);
							// impBs[pos].resetDisplayRange();
							if (impBs[pos].isComposite())
								((CompositeImage) impBs[pos])
								.setMode(CompositeImage.COMPOSITE);
							impBs[pos].setFileInfo(new FileInfo());
							impBs[pos].getOriginalFileInfo().fileName = dirOrOMETiff;
							impBs[pos].getOriginalFileInfo().directory = dirOrOMETiff;

						} catch (IOException e) {
							e.printStackTrace();
						}

						impBs[pos].setPosition(cB, zB,
								tB == impBs[pos].getNFrames() - 1 ? impBs[pos].getNFrames() : tB);
						//impBs[pos].setWindow(win);
							win.setImage(impBs[pos]);

					}
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


				uploadRunning = wgUploadJob.getNewUploadProcess().isAlive();
				if(!uploadRunning && uploadPending)	{
					uploadPending = false;
					wgUploadJob = new WG_Uploader();
					wgUploadJob.run(dirOrOMETiff);
				}


				if (doDecon) {

					//					impA = impAs[pos];
					//					impB = impBs[pos];
					for (int pos=0; pos<pDim; pos++) {

						roiAs[pos] = impAs[pos].getRoi();
						roiBs[pos] = impBs[pos].getRoi();

						wasFrameA[pos] = impAs[pos].getFrame();
						wasFrameB[pos] = impBs[pos].getFrame();
						int wasFrameDF1 = 1;
						if (impDF1s[pos] != null)
							wasFrameDF1 = impDF1s[pos].getFrame();
						int wasFrameDF2 = 1;
						if (impDF2s[pos] != null)
							wasFrameDF2 = impDF2s[pos].getFrame();
						wasSliceA[pos] = impAs[pos].getSlice();
						wasSliceB[pos] = impBs[pos].getSlice();
						int wasSliceDF1 = 1;
						if (impDF1s[pos] != null)
							wasSliceDF1 = impDF1s[pos].getSlice();
						int wasSliceDF2 = 1;
						if (impDF2s[pos] != null)
							wasSliceDF2 = impDF2s[pos].getSlice();
						wasChannelA[pos] = impAs[pos].getChannel();
						wasChannelB[pos] = impBs[pos].getChannel();
						int wasChannelDF1 = 1;
						if (impDF1s[pos] != null)
							wasChannelDF1 = impDF1s[pos].getChannel();
						int wasChannelDF2 = 1;
						if (impDF2s[pos] != null)
							wasChannelDF2 = impDF2s[pos].getChannel();
						WindowManager.setTempCurrentImage(impAs[pos]);
						IJ.open(savePath/* + dirOrOMETiffName */+  "Pos" + pos + "A_crop.roi");
						WindowManager.setTempCurrentImage(impBs[pos]);
						IJ.open(savePath/* + dirOrOMETiffName */+  "Pos" + pos + "B_crop.roi");
						WindowManager.setTempCurrentImage(null);

						for (int f = 1; f <= impAs[pos].getNFrames(); f++) {

							impAs[pos].setPositionWithoutUpdate(impAs[pos].getChannel(),
									impAs[pos].getSlice(), f);

							String frameFileName = "";
							if (impAs[pos].getStack() instanceof ListVirtualStack)
								frameFileName = ((ListVirtualStack) impAs[pos].getStack())
								.getDirectory(impAs[pos].getCurrentSlice());
							else if (impAs[pos].getStack() instanceof FileInfoVirtualStack
									|| impAs[pos].getStack() instanceof MultiFileInfoVirtualStack)
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
								impAs[pos].getWindow().setEnabled(false);
								for (int i = 1; i <= impAs[pos].getNSlices(); i++) {
									impAs[pos].setPositionWithoutUpdate(1, i, f);
									stackA1.addSlice(impAs[pos].getProcessor().crop());
									if (wavelengths == 2) {
										impAs[pos].setPositionWithoutUpdate(2, i, f);
										stackA2.addSlice(impAs[pos].getProcessor().crop());
									}
								}
								impAs[pos].getWindow().setEnabled(true);
								ImagePlus impXA1 = new ImagePlus();
								impXA1.setStack(stackA1);
								impXA1.setCalibration(impAs[pos].getCalibration());
								// impXA1.getCalibration().pixelDepth =
								// impXA1.getCalibration().pixelWidth;
								IJ.saveAs(impXA1, "Tiff", savePath
										+ "Pos"+pos+ "_SPIMA_Ch1_processed" + File.separator
										+ frameFileName + File.separator
										+ frameFileName + ".tif");
								if (wavelengths == 2) {
									ImagePlus impXA2 = new ImagePlus();
									impXA2.setStack(stackA2);
									impXA2.setCalibration(impAs[pos].getCalibration());
									// impXA2.getCalibration().pixelDepth =
									// impXA2.getCalibration().pixelWidth;
									IJ.saveAs(impXA2, "Tiff", savePath
											+ "Pos"+pos+ "_SPIMA_Ch2_processed" + File.separator
											+ frameFileName + File.separator
											+ frameFileName + ".tif");
								}
								ImageStack stackB1 = new ImageStack(325, 425);
								ImageStack stackB2 = new ImageStack(325, 425);
								impBs[pos].getWindow().setEnabled(false);
								for (int i = 1; i <= impBs[pos].getNSlices(); i++) {
									impBs[pos].setPositionWithoutUpdate(1, i, f);
									stackB1.addSlice(impBs[pos].getProcessor().crop());
									if (wavelengths == 2) {
										impBs[pos].setPositionWithoutUpdate(2, i, f);
										stackB2.addSlice(impBs[pos].getProcessor().crop());
									}
								}
								impBs[pos].getWindow().setEnabled(true);
								ImagePlus impXB1 = new ImagePlus();
								impXB1.setStack(stackB1);
								impXB1.setCalibration(impBs[pos].getCalibration());
								// impXB1.getCalibration().pixelDepth =
								// impXB1.getCalibration().pixelWidth;
								IJ.saveAs(impXB1, "Tiff", savePath
										+ "Pos"+pos+ "_SPIMB_Ch1_processed" + File.separator
										+ frameFileName + File.separator
										+ frameFileName + ".tif");
								if (wavelengths == 2) {
									ImagePlus impXB2 = new ImagePlus();
									impXB2.setStack(stackB2);
									impXB2.setCalibration(impBs[pos].getCalibration());
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
										+ "\", \"spimBFileDir string "
										+ savePath.replace("\\", "\\\\")
										+ "Pos"+pos+ "_SPIMA_Ch"
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
										+ "\", \"baseImage string "
										+ frameFileName
										+ "\", \"base_rotation int -1\", \"transform_rotation int 5\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
										+ "\", \"base_rotation int -1\", \"transform_rotation int 4\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
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
											+ "\", \"spimBFileDir string "
											+ savePath.replace("\\", "\\\\")
											+ "Pos"+pos+ "_SPIMA_Ch"
											+ slaveChannel
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
											+ "\", \"baseImage string "
											+ frameFileName
											+ "\", \"base_rotation int -1\", \"transform_rotation int 5\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
											+ "\", \"base_rotation int -1\", \"transform_rotation int 4\", \"concurrent_num int 1\", \"mode_num int 0\", \"save_type string Tiff\", \"do_deconv boolean true\", \"deconv_platform int 2\", \"deconvDirString string "
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
					}
					// IJ.wait(15000);
					for (int pos=0; pos<pDim; pos++) {

						impAs[pos].setPosition(wasChannelA[pos], wasSliceA[pos], wasFrameA[pos]);
						impBs[pos].setPosition(wasChannelB[pos], wasSliceB[pos], wasFrameB[pos]);

						if ((new File(dirOrOMETiff)).canRead()) {
							if (impDF1s[pos] == null) {
								MultiFileInfoVirtualStack deconmfivs = new MultiFileInfoVirtualStack(
										(new File(dirOrOMETiff)).isDirectory() ? dirOrOMETiff
												: (new File(dirOrOMETiff)).getParent()
												+ File.separator,
												"Deconvolution", false);
								if (deconmfivs.getSize() > 0) {
									impDF1s[pos] = new ImagePlus();
									impDF1s[pos].setStack(
											"Decon-Fuse"
													+ impAs[pos].getTitle()
													.replace(
															impAs[pos].getTitle()
															.split(":")[0],
															""), deconmfivs);
									impDF1s[pos].setFileInfo(new FileInfo());
									// impDF1s[pos].getOriginalFileInfo().directory = (new
									// File(dirOrOMETiff)).isDirectory()?dirOrOMETiff:((new
									// File(dirOrOMETiff)).getParent()+File.separator);
									impDF1s[pos].getOriginalFileInfo().directory = dirOrOMETiff;
									int stkNSlicesDF = impDF1s[pos].getStackSize();
									int zSlicesDF1 = deconmfivs.getFivStacks().get(0)
											.getSize();
									impDF1s[pos].setOpenAsHyperStack(true);
									impDF1s[pos].setStack(impDF1s[pos].getStack(), wavelengths,
											zSlicesDF1, stkNSlicesDF
											/ (wavelengths * zSlicesDF1));
									ciDF1s[pos] = new CompositeImage(impDF1s[pos]);
									if (wavelengths > 1)
										ciDF1s[pos].setMode(CompositeImage.COMPOSITE);
									else
										ciDF1s[pos].setMode(CompositeImage.GRAYSCALE);
									ciDF1s[pos].show();
								}
							} else {
								MultiFileInfoVirtualStack deconmfivs = new MultiFileInfoVirtualStack(
										(new File(dirOrOMETiff)).isDirectory() ? dirOrOMETiff
												: (new File(dirOrOMETiff)).getParent()
												+ File.separator,
												"Deconvolution", false);
								if (deconmfivs.getSize() > 0) {
									win = ciDF1s[pos].getWindow();
									impDF1s[pos].setStack(
											"Decon-Fuse"
													+ impAs[pos].getTitle()
													.replace(
															impAs[pos].getTitle()
															.split(":")[0],
															""), deconmfivs);
									impDF1s[pos].setFileInfo(new FileInfo());
									// impDF1s[pos].getOriginalFileInfo().directory = (new
									// File(dirOrOMETiff)).isDirectory()?dirOrOMETiff:((new
									// File(dirOrOMETiff)).getParent()+File.separator);
									impDF1s[pos].getOriginalFileInfo().directory = dirOrOMETiff;
									int stkNSlicesDF = impDF1s[pos].getStackSize();
									int zSlicesDF1 = deconmfivs.getFivStacks().get(0)
											.getSize();
									impDF1s[pos].setOpenAsHyperStack(true);
									impDF1s[pos].setStack(impDF1s[pos].getStack(), wavelengths,
											zSlicesDF1, stkNSlicesDF
											/ (wavelengths * zSlicesDF1));
									ciDF1s[pos] = new CompositeImage(impDF1s[pos]);
									if (wavelengths > 1)
										ciDF1s[pos].setMode(CompositeImage.COMPOSITE);
									else
										ciDF1s[pos].setMode(CompositeImage.GRAYSCALE);
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

									ciDF1s[pos].setWindow(win);
									win.updateImage(ciDF1s[pos]);
									win.setSize(oldW, oldH);
									((StackWindow) win).addScrollbars(ciDF1s[pos]);
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
//			}
		}
	}
	
	
	public void readInMMdiSPIMheader(File dirOrOMETiffFile)
			throws NumberFormatException {
		String diSPIMheader = "";
		for (String fileName:dirOrOMETiffFile.list()) {
			File nextFile = new File(dirOrOMETiffFile+File.separator+fileName);

			if(nextFile.isDirectory() && nextFile.list().length>0) {
				for (String listFile:nextFile.list()){
					if (listFile.contains("MMStack")) {
						IJ.log(nextFile.getPath()+File.separator+listFile);
						if (diSPIMheader == "")
							diSPIMheader = open_diSPIMheaderAsString(nextFile.getPath()+File.separator+listFile);
						tDim++;
						break;
					}
				}
			}
		}
//				IJ.log(diSPIMheader);
//				diSPIMheader = diSPIMheader.replaceAll("(.*\\$.\\# .  \\{)", "{");
				diSPIMheader = diSPIMheader.replaceAll("(.*\\$.\\#.*\\{\")(.*)", "\\{\"$2");
//				IJ.log(diSPIMheader);

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
		IJ.log("*************************");
		IJ.log(diSPIMheader);

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
			if (chunk.startsWith("}") ) {
				indents--;
			}
			for (int i=0;i<indents;i++)
				chunk = "        "+chunk;
			//			IJ.log(chunk);
			if (chunk.contains("{") ) {
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
					//					IJ.log(s)
					;
			}


			if (chunk.trim().startsWith("\"Height\":")) {
				diSPIM_MM_Height= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"SlicePeriod_ms\":")) {
				diSPIM_MM_SlicePeriod_ms= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"GridColumn\":")) {
				diSPIM_MM_GridColumn= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"PixelSize_um\":")) {
				diSPIM_MM_PixelSize_um= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
				//						vWidth = diSPIM_MM_PixelSize_um;
				//						vHeight = diSPIM_MM_PixelSize_um;
			}
			if (chunk.trim().startsWith("\"Frames\":")) {
				diSPIM_MM_Frames= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"Source\":")) {
				diSPIM_MM_Source= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"Channels\":")) {
				diSPIM_MM_Channels= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"AcqusitionName\":")) {
				diSPIM_MM_AcqusitionName= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"NumberOfSides\":")) {
				diSPIM_MM_NumberOfSides= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"SPIMmode\":")) {
				diSPIM_MM_SPIMmode= chunk.split(":")[1].replace("\"", "").trim();
			}


			if (chunk.trim().startsWith("\"ChColors\":")) {
				diSPIM_MM_ChColorStrings= chunk.split(":")[1].replace("[", "").replace("]", "").replace("\"", "").split(";");
				diSPIM_MM_ChColors = new int[diSPIM_MM_ChColorStrings.length];
				for(int ccs=0;ccs<diSPIM_MM_ChColorStrings.length;ccs++) {
					//					IJ.log(diSPIM_MM_ChColorStrings[ccs]);
					diSPIM_MM_ChColors[ccs] = Integer.parseInt(diSPIM_MM_ChColorStrings[ccs]);
				}
			}


			if (chunk.trim().startsWith("\"Slices\":")) {
				diSPIM_MM_Slices= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"UserName\":")) {
				diSPIM_MM_UserName= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"Depth\":")) {
				diSPIM_MM_Depth= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"PixelType\":")) {
				diSPIM_MM_PixelType= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"Time\":")) {
				diSPIM_MM_Time= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"FirstSide\":")) {
				diSPIM_MM_FirstSide= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"z-step_um\":")) {
				diSPIM_MM_zStep_um= Double.parseDouble(chunk.split(":")[1].replace("\"", "").trim());
				//						vDepthRaw = diSPIM_MM_zStep_um;
			}
			if (chunk.trim().startsWith("\"SlicesFirst\":")) {
				diSPIM_MM_SlicesFirst= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
			}

			if (chunk.trim().startsWith("\"ChContrastMin\":")) {
				diSPIM_MM_ChContrastMinStrings= chunk.split(":")[1].replace("[", "").replace("]", "").replace("\"", "").split(";");
				diSPIM_MM_ChContrastMin = new int[diSPIM_MM_ChContrastMinStrings.length];
				for(int ccmin=0;ccmin<diSPIM_MM_ChContrastMinStrings.length;ccmin++) {
					//					IJ.log(diSPIM_MM_ChContrastMinStrings[ccmin]);
					diSPIM_MM_ChContrastMin[ccmin] = Integer.parseInt(diSPIM_MM_ChContrastMinStrings[ccmin]);
				}
			}


			if (chunk.trim().startsWith("\"StartTime\":")) {
				diSPIM_MM_StartTime= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"MVRotationAxis\":")) {
				diSPIM_MM_MVRotationAxis= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"MicroManagerVersion\":")) {
				diSPIM_MM_MicroManagerVersion= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"IJType\":")) {
				diSPIM_MM_IJType= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"GridRow\":")) {
				diSPIM_MM_GridRow= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"VolumeDuration\":")) {
				diSPIM_MM_VolumeDuration= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"NumComponents\":")) {
				diSPIM_MM_NumComponents= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"Position_SPIM_Head\":")) {
				diSPIM_MM_Position_SPIM_Head= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"BitDepth\":")) {
				diSPIM_MM_BitDepth= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
			}
			if (chunk.trim().startsWith("\"ComputerName\":")) {
				diSPIM_MM_ComputerName= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"CameraMode\":")) {
				diSPIM_MM_CameraMode= chunk.split(":")[1].replace("\"", "").trim();
			}
			if (chunk.trim().startsWith("\"TimeFirst\":")) {
				diSPIM_MM_TimeFirst= chunk.split(":")[1].replace("\"", "").trim().toLowerCase().contains("true");
			}

			if (chunk.trim().startsWith("\"ChContrastMax\":")) {
				diSPIM_MM_ChContrastMaxStrings= chunk.split(":")[1].replace("[", "").replace("]", "").replace("\"", "").split(";");
				diSPIM_MM_ChContrastMax = new int[diSPIM_MM_ChContrastMaxStrings.length];
				for(int ccMax=0;ccMax<diSPIM_MM_ChContrastMaxStrings.length;ccMax++) {
					//					IJ.log(diSPIM_MM_ChContrastMaxStrings[ccMax]);
					diSPIM_MM_ChContrastMax[ccMax] = Integer.parseInt(diSPIM_MM_ChContrastMaxStrings[ccMax]);
				}
			}

			if (chunk.trim().startsWith("\"Positions\":")) {
				diSPIM_MM_Positions= Integer.parseInt(chunk.split(":")[1].replace("\"", "").trim());
				pDim = diSPIM_MM_Positions;
			}

//								if (chunk.trim().split(":").length>1)
//									allVars = allVars+"\n diSPIM_MM_"+chunk.trim().split(":")[1];
		}
//						IJ.log(allVars);
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
		IJ.log(path);
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
			while (!s.contains("\"Positions\":")) {
				//			for (int l=0;l<2;l++) {
				s=r.readLine();
				//				IJ.log(s);
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

package compute;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.Image;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.zip.ZipInputStream;

public interface Compute extends Remote {
	Object getQTPixels(int remoteImpID, int channel, int slice, int frame, double jpegQuality) throws RemoteException; 
	int[] getQTDimensions(int remoteImpID) throws RemoteException;
	int setUpMovie(String[] names, String[] movieSlices, int port)  throws RemoteException;
    BigDecimal doPi(int numdig) throws RemoteException;
	String closeRemoteImp(int remoteImpID)  throws RemoteException;
	String spawnNewServer(String string, String serverIP, String serverPort, String clientIP)  throws RemoteException;
	byte[] getFileInputByteArray(String pathlist) throws RemoteException;
	boolean resetServer() throws RemoteException;
	void restartLullClock() throws RemoteException;
	boolean startLullClock() throws RemoteException;
}


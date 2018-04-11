package com.example.android.printersettings;

import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class Globals {
	//public static MyAndroidProgressMonitor transferMonitor = new MyAndroidProgressMonitor();
	public static String server="192.168.1.2";
	public static String fileToUpload;
	public static long fileSize=0;
	public static ProgressBar mProgress=null;
	public static Handler mHandler=null;
	public static boolean actuallyDone=false;
	public static boolean actuallyDoneAndRenamed=false;
	public static boolean isBusy=false;
	public static boolean takingPicture=false;
	public static LinearLayout cameraPreview;
}

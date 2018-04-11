package com.pkgingo.santaprinterftp2;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;

public class MyTransferListener implements FTPDataTransferListener {
	private static long totalBytes=0;
	private static double a=0;
	private static double b=0;
	private static int p=0;
	private static int oldp=0;

	public void started() {
		// Transfer started
		totalBytes=0;
		Globals.actuallyDone=false;
	}

	public void transferred(int length) {
		// Yet other length bytes has been transferred since the last time this
		// method was called
		totalBytes+=length;
		
		a=totalBytes;
		b=Globals.fileSize;
		p=(int) ((a/b)*100);
		Globals.mHandler.post(new Runnable() {
            public void run() {
            	System.out.println(a + "/" + b + "  " +  oldp + "%");
            	Globals.mProgress.setProgress(oldp);
            }
        });
		oldp=p;
	}

	public void completed() {
		// Transfer completed
		Globals.actuallyDone=true;
		Globals.mHandler.post(new Runnable() {
            public void run() {
            	System.out.println(a + "/" + b + "  " +  oldp + "%");
            	Globals.mProgress.setProgress(100);
            }
        });
	}

	public void aborted() {
		// Transfer aborted
	}

	public void failed() {
		// Transfer failed
	}

}
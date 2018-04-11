package com.pkgingo.santaprinterftp2;


import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UploadActivity extends Activity {
	 private ProgressBar mProgress;
	 private Handler mHandler = new Handler();
	 private Context c = UploadActivity.this;
	 private boolean canceled=false;
	 public TextView info=null;
	 public Button btnCancel =null;
	 //public ftpThread r = new ftpThread(Globals.server, Globals.fileToUpload);
	 
	 
	 
	 
	 
	 private int server_port = 21; //port we are talking to
		private String host = null; //name of device or textual IP we want to resolve
		private File file=null;
		
		private volatile boolean operationDone=false;
		private volatile boolean operationError=false;
		private volatile boolean transferStarted=false;
		private volatile boolean transferComplete=false;
		
		private final int RETRY_THRESHOLD=1;
		
		private volatile String lastError="";
		private volatile FTPClient client = new FTPClient();
		
		
		

		public boolean run() {
			Globals.actuallyDone=false;
			Globals.actuallyDoneAndRenamed=false;
			if (operationError) return false;
			if (!connect()) return false;
			if (!login()) return false;
			if (!upload(0)) return false;
			if (canceled)
			{
				transferComplete=false;
				System.out.println("Transfer aborted!");
				if (!deleteFile()) {
					disconnect();
					return false;
				}
				
			}
			else
			{
				renameFile();
			}
			if (!disconnect()) return false;
			operationDone=true;
			return true;
			
		}
		
		private boolean renameFile()
		{
			String oldfilename=file.getName()+".part";
			String newfilename=file.getName();
			try {
				client.rename(oldfilename, newfilename);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return setupErrorStructure(e, "rename");
			}
			Globals.actuallyDoneAndRenamed=true;
			return true;
		}
		
		private boolean deleteFile()
		{
			try {
				client.deleteFile(file.getName());
			} catch (Exception e) {
				return setupErrorStructure(e, "delete");
			}
			return true;
		}
		
		private boolean connect()
		{
			try {
				client.connect(host, server_port);
			} catch (Exception e) {
				return setupErrorStructure(e, "connect");
			}
			return true;
		}
		
		private boolean login()
		{
			try{
				client.login("camera", "mypassword");
			}
			catch(Exception e)
			{
				return setupErrorStructure(e, "login");
			}
			return true;
		}
		
		private boolean upload(int retry)
		{
			if (canceled) return false;
			Globals.fileSize=file.length();
			System.out.println("Beginning transfer!");
			transferStarted=true;
			if (retry>RETRY_THRESHOLD) return false;
			
			try {
				String name=file.getName()+".part";
				client.upload(file, name,0, new MyTransferListener());
			} catch (IllegalStateException e) {
				if (retry>RETRY_THRESHOLD)
				{
					upload(retry++);
				}
				else
				{
					return setupErrorStructure(e, "upload");
				}
			} catch (FileNotFoundException e) {
				return setupErrorStructure(e, "upload");
			} catch (IOException e) {
				if (retry>RETRY_THRESHOLD)
				{
					upload(retry++);
				}
				else
				{
					return setupErrorStructure(e, "upload");
				}
			} catch (FTPIllegalReplyException e) {
				if (retry>RETRY_THRESHOLD)
				{
					upload(retry++);
				}
				else
				{
					return setupErrorStructure(e, "upload");
				}
			} catch (FTPException e) {
				if (retry>RETRY_THRESHOLD)
				{
					upload(retry++);
				}
				else
				{
					return setupErrorStructure(e, "upload");
				}
			} catch (FTPDataTransferException e) {
				if (retry>RETRY_THRESHOLD)
				{
					upload(retry++);
				}
				else
				{
					return setupErrorStructure(e, "upload");
				}
			} catch (FTPAbortedException e) {
				canceled=true;
				transferComplete=true;
				return false;
			}
			transferComplete=true;
			System.out.println("Transfer complete!");
			return true;
		}
		
		private boolean setupErrorStructure(Exception e, String type)
		{
			if (canceled && transferComplete) return true;
			System.out.println("There was a(n) "+ type +" error: " + e.getMessage());
			operationDone=true;
			operationError=true;
			lastError=(e.getLocalizedMessage());
			return false;
		}
		
		public boolean abortTransfer()
		{
			try {
				client.abortCurrentDataTransfer(true);
				//client.disconnect(true); 	
			} catch (Exception e) {
				try {
					client.abortCurrentDataTransfer(false);
					//client.disconnect(false);
				} catch (Exception e1) {
					return setupErrorStructure(e, "abort");
				}
				return setupErrorStructure(e, "abort2");
			}
			return true;
		}
		
		private boolean disconnect()
		{
			try {
				client.disconnect(true);
			} catch (Exception e) {
				try {
					client.disconnect(false);
				} catch (Exception e1) {
					return setupErrorStructure(e, "disconnect");
				}
				return setupErrorStructure(e, "disconnect2");
			}
			return true;
		}
		
		

	 
	 @Override
	 public void onBackPressed() {
	 }
	 
	 

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		canceled = false;
		Globals.fileSize = 0;

		setContentView(R.layout.upload);

		mProgress = (ProgressBar) findViewById(R.id.progDownload);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		Globals.mProgress = mProgress;
		Globals.mHandler = mHandler;
		info = (TextView) findViewById(R.id.infoText);

		btnCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				// Globals.transferMonitor.cancel();
				abortTransfer();
				canceled = true;
				showAlert("Canceled", "Transfer has been canceled.", "OK");
			}
		});
        new asyncTaskUpdateProgress().execute();
         /*
        Globals.transferMonitor.setup(mProgress, mHandler);
        new Thread(new Runnable() {
            public void run() {
            	
            	if (!Globals.mysftp.getFile(Globals.fileToDownload))
                {
            		showAlert("Error", "Error downloading: " + Globals.mysftp.getLastError(),"OK");
               	}
            }
        }).start();
         */
         
        
         
         
         
         

         // Start lengthy operation in a background thread
         
          
     }
     
     public void showAlert(String title, String message, String buttonText)
 	{
 		AlertDialog.Builder alertbox = new AlertDialog.Builder(c);
 		alertbox.setTitle(title);
 		alertbox.setMessage(message);
 		alertbox.setNeutralButton(buttonText, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				setResult(RESULT_OK);
           	 
				
			}
		});
 		
 	
 		/*
   	 alertbox.setOnDismissListener(new OnDismissListener() {
   		 
   	    public void onDismiss(final DialogInterface dialog) {
   	    	finish();
   	    }
   	    });*/
   	 
   	alertbox.setOnCancelListener(new OnCancelListener() {
  		 
   	    public void onCancel(final DialogInterface dialog) {
   	    	doneHere();
   	    }
   	    });
 		alertbox.show();
   	 
   	 
 	}
     
     public void showAlert2(String title, String message, String buttonText)
  	{
  		AlertDialog.Builder alertbox = new AlertDialog.Builder(c);
  		alertbox.setTitle(title);
  		alertbox.setMessage(message);
  		alertbox.setNeutralButton(buttonText, new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 				setResult(RESULT_OK);
            	 
 				//Intent intent = new Intent(UploadActivity.this, MainActivity.class);
				//UploadActivity.this.startActivity(intent);
 				doneHere();
 				
 			}
 		});
  		
  	
  		/*
    	 alertbox.setOnDismissListener(new OnDismissListener() {
    		 
    	    public void onDismiss(final DialogInterface dialog) {
    	    	finish();
    	    }
    	    });*/
    	 
    	alertbox.setOnCancelListener(new OnCancelListener() {
   		 
    	    public void onCancel(final DialogInterface dialog) {
 				//Intent intent = new Intent(UploadActivity.this, MainActivity.class);
				//UploadActivity.this.startActivity(intent);
    	    	doneHere();
    	    }
    	    });
  		alertbox.show();
    	 
    	 
  	}
     
     public void doneHere()
     {
    	 Globals.isBusy=false;
    	 Globals.cameraPreview.setVisibility(View.VISIBLE);
		finish();
     }
     
     
     
     
     public class asyncTaskUpdateProgress extends AsyncTask<Void, Void, Boolean>
     {

		@Override
		protected Boolean doInBackground(Void... arg0) {
	    //Globals.transferMonitor.setup(mProgress, mHandler);
	    
	    
	    host=Globals.server;
		file = new File(Globals.fileToUpload);
			 
		    
    	if (!run())
        {
    		//canceled due to error
    		//canceled=true;
    		return false;
       	}
    	
    	
        
		return true;
	}
		
	 
		protected void onPostExecute(Boolean result)
		{
			boolean showedSomething=false;
			

			String title = "", text="";
				if(!canceled)
				{
					//long p = Globals.transferMonitor.getPercent();
					if (Globals.actuallyDoneAndRenamed)
					{
						title="Complete";
						text="Transfer complete!";
					}
					else
					{
						title="Didnt finish";
						text="Some data wasn't transferred.";
						
					}
					showedSomething=true;
				}
				else
				{
					title="Transfer aborted!";
					text="User canceled transfer request!";
					if (client.isConnected())
					{
						System.out.println("Transfer aborted!");
						deleteFile();
						disconnect();
					}
					showAlert2(title, text,"OK");
					return;
				}
				if (operationError)
				{
					if (!showedSomething)
					{
						title="Error";
						text="Try eprint, error sending files to FTP server: " + lastError;
					}
				}
				showAlert2(title, text,"OK");

        }

    	
     }
     
}

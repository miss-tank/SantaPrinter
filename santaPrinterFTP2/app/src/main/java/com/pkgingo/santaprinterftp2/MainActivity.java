package com.pkgingo.santaprinterftp2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends Activity {

SantaPrinterSettings settings = new SantaPrinterSettings();
File STORAGE_DIRECTORY=Environment.getExternalStorageDirectory();
String DEVICE_NAME="";
BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
final String TS=""+(System.currentTimeMillis() / 1000L);
private Camera mCamera;
private CameraPreview mPreview;
private PictureCallback mPicture;
private Context myContext;
private LinearLayout cameraPreview;
private LinearLayout BG;
private static final int REQUEST_GET_ACCOUNT = 112;
private static final int PERMISSION_REQUEST_CODE = 200;
private static Context context;
public Bitmap GlobalFooter;

	public static boolean checkPermission(String source) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(MainActivity.context, Manifest.permission.GET_ACCOUNTS);
            int result1 = ContextCompat.checkSelfPermission(MainActivity.context, Manifest.permission.CAMERA);
            int result2 = ContextCompat.checkSelfPermission(MainActivity.context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int result3 = ContextCompat.checkSelfPermission(MainActivity.context, Manifest.permission.READ_EXTERNAL_STORAGE);
            int tresult = result1 & result2 & result3;
            //System.out.println("Checking permissions from: " + source + " - " + result + "," + result1 + "," + result2 + "," + result3 + "=" + tresult);
            return tresult >= 0;
        }
        return true;
	}

	private void requestPermission() {
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE+1);
		//ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
	}

	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_CODE:
				if (grantResults.length > 0) {

					boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean storageWAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean storageRAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;

					if (locationAccepted && cameraAccepted)
						Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access location data and camera", Toast.LENGTH_LONG).show();
					else {
						Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access location data and camera", Toast.LENGTH_LONG).show();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
							if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
								showMessageOKCancel("You need to allow access to the permissions",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
													requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
															PERMISSION_REQUEST_CODE);
												}
											}
										});
								return;
							}
						}

					}
				}

				break;
		}
	}


	public void getFirst()
	{


//        System.out.println("@@ cliking now");
//
//
//        String[] projection = {MediaStore.Images.Thumbnails._ID};
//        cursor = managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Thumbnails._ID + "");
//        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
//
//        System.out.println("coloumn Indexx "+ columnIndex );
//
//        //adapter.notifyDataSetChanged();


		File file = new File(STORAGE_DIRECTORY + "/SantaTrain/");


		Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		Uri u2 = Uri.fromFile(file);
		System.out.println("@@ this is teh URI "+ u.toString() +" "+ u2.toString());



		String[] projection = {MediaStore.Images.ImageColumns.DATA};
		Cursor c = null;
		SortedSet<String> dirList = new TreeSet<String>();
		ArrayList<String> resultIAV = new ArrayList<String>();
		String x ="";

		String[] directories = null;
		if (u != null)
		{
			c = managedQuery(u, projection, null, null, null);
		}





		if ((c != null) && (c.moveToFirst()))
		{
			do
			{
				String tempDir = c.getString(0);
				System.out.println("@@ tempDir -> " +tempDir);

				tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
				System.out.println("@@ tempDir next -> " +tempDir);


				try{
					if(tempDir.contains("Footer")) {
						dirList.add(tempDir);
					}
				}
				catch(Exception e)
				{

				}
			}
			while (c.moveToNext());
			directories = new String[dirList.size()];
			dirList.toArray(directories);

		}


		for(int i=0;i<dirList.size();i++)
		{
			System.out.println("@@ dirList -> " +dirList.toString());
		}

		for(int i=0;i<dirList.size();i++)
		{



			File imageDir = new File(directories[i]);
			File[] imageList = imageDir.listFiles();
			if(imageList == null)
				continue;
			for (File imagePath : imageList) {
				try {

					if(imagePath.isDirectory())
					{
						imageList = imagePath.listFiles();

					}
					if ( imagePath.getName().contains(".jpg")|| imagePath.getName().contains(".JPG")
							|| imagePath.getName().contains(".jpeg")|| imagePath.getName().contains(".JPEG")
							|| imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
							|| imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
							|| imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
							)
					{



						String path= imagePath.getAbsolutePath();
						resultIAV.add(path);

					}
				}
				//  }
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		for(int i=0;i<resultIAV.size();i++)
		{
			x= resultIAV.get(i);
			System.out.println("@@ resultIAV -> " +x);
		}


		getlastImageURI(x);



	}

	public void getlastImageURI(String path)
	{
		File imgFile = new  File(path);

		if(imgFile.exists()){

			GlobalFooter = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

			//ImageView myImage = (ImageView) findViewById(R.id.imageviewTest);

			//leftImageView.setImageBitmap(myBitmap);


		}
	}


	private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
				.setMessage(message)
				.setPositiveButton("OK", okListener)
				.setNegativeButton("Cancel", null)
				.create()
				.show();
	}

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
       MainActivity.context = getApplicationContext();
       Globals.takingPicture=false;
      setContentView(R.layout.activity_main);
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		myContext = this;
		
		Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();
		boolean isSharing=(Intent.ACTION_SEND.equals(action) && type != null);


			   if (!checkPermission("Create")) {
                   requestPermission();
                   isSharing = false;
               }


		initialize(isSharing);
		

	    if (isSharing)
	    {
	    	if (type.startsWith("image/")) {
	            handleSendImage(intent); // Handle single image being sent
	        }
	    }

	   System.out.println("Trying to get last");
	   getFirst();

   }
   
   public void handleSendImage(Intent i)
   {
	   Uri imageUri = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
	    if (imageUri != null) {
    		hideCameraPreview();
    		Globals.isBusy=true;
	        Globals.fileToUpload=getRealPathFromURI(myContext,imageUri);
	        Log.d("santaprinterftp","URI Intent: " + imageUri.getPath());
		   	Intent intent = new Intent(MainActivity.this, UploadActivity.class);
		   	MainActivity.this.startActivity(intent);
		   	finish();
	    }

   }
   


public String getRealPathFromURI(Context context, Uri contentUri) {
  Cursor cursor = null;
  try { 
    String[] proj = { MediaStore.Images.Media.DATA };
    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();
    return cursor.getString(column_index);
  } finally {
    if (cursor != null) {
      cursor.close();
    }
  }
}


	public void initialize() {
		initialize(false);
	}
	
	public void initialize(boolean isSharing) {
		cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
		BG = (LinearLayout) findViewById(R.id.background);
		Globals.cameraPreview=cameraPreview;
		if (!isSharing)
		{
			mPreview = new CameraPreview(myContext, mCamera);
			cameraPreview.addView(mPreview);
			cameraPreview.setOnClickListener(previewClickListener);
			cameraPreview.setOnLongClickListener(previewLongClickListener);
		}
		STORAGE_DIRECTORY=findExternalStorage();
	      if (myDevice!=null)
	      {
	    	  DEVICE_NAME=myDevice.getName();
	      }
	      getSettings();
	}

   
   private int findBackFacingCamera() {
		int cameraId = -1;
		//Search for the back facing camera
		//get the number of cameras
		int numberOfCameras = Camera.getNumberOfCameras();
		//for every camera check
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraId = i;
				break;
			}
		}
		return cameraId;
	}
   
   
   public void onResume() {
		super.onResume();
       //System.out.println("Resuming activity!");
           if (!checkPermission("Resume"))
           {

               requestPermission();
               //super.onStop();
                return;
           }


           if (!hasCamera(myContext)) {
               Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
               toast.show();
               finish();
           }
           if (mCamera == null) {
               //if the front facing camera does not exist
               mCamera = Camera.open(findBackFacingCamera());
               mPicture = getPictureCallback();
               mPreview.refreshCamera(mCamera);
           }


       //System.out.println("Taking a picture? " + Globals.takingPicture);
           if (Globals.takingPicture)
           {
               setBGToLastPhoto();
           }
           else
           {
               showCameraPreview();
           }


	}
   
   
   public void getSettings() {
	   File f = new File(STORAGE_DIRECTORY + "/SantaTrainSettings");
	   if(f.exists())
       {
    	   if(f.isDirectory())
    	   {
    		   //do checks for settings here
    		   f = new File(STORAGE_DIRECTORY + "/SantaTrainSettings/footer");
    		   if(f.exists())
    		   {
    			   if(f.isDirectory())//do we have a footer directory?
    	    	   {
    				   Globals.server=readSettingsFile("server.txt");
    				   if (Globals.server==null) Globals.server="192.168.1.2";
    				   settings.addFooter();
    				   settings.setFooterHeight(readSettingsFile("footersize.txt"));
    				   settings.footerColor().setA(readSettingsFile("color/a.txt"));
    				   settings.footerColor().setR(readSettingsFile("color/r.txt"));
    				   settings.footerColor().setG(readSettingsFile("color/g.txt"));
    				   settings.footerColor().setB(readSettingsFile("color/b.txt"));
    				   settings.msgFontColor().setA(readSettingsFile("msg/font/a.txt"));
    				   settings.msgFontColor().setR(readSettingsFile("msg/font/r.txt"));
    				   settings.msgFontColor().setG(readSettingsFile("msg/font/g.txt"));
    				   settings.msgFontColor().setB(readSettingsFile("msg/font/b.txt"));
    				   settings.setMessageTextFontSize(readSettingsFile("msg/font/size.txt"));
    				   settings.setMessageText(readSettingsFile("msg/text.txt"));
    				   settings.copyrightFontColor().setA(readSettingsFile("copyright/font/a.txt"));
    				   settings.copyrightFontColor().setR(readSettingsFile("copyright/font/r.txt"));
    				   settings.copyrightFontColor().setG(readSettingsFile("copyright/font/g.txt"));
    				   settings.copyrightFontColor().setB(readSettingsFile("copyright/font/b.txt"));
    				   settings.setCopyrightFontSize(readSettingsFile("copyright/font/size.txt"));
    				   settings.setCopyrightPrefixText(readSettingsFile("copyright/text.txt"));
    				   f = new File(STORAGE_DIRECTORY + "/SantaTrainSettings/footer/left.png");
    				   if (f.exists() && f.isFile())
    				   {
    					   settings.setLeftImage(BitmapFactory.decodeFile(f.getAbsolutePath()));
    				   }
    				   else
    				   {
    					   settings.setLeftImage(BitmapFactory.decodeResource(getResources(), R.drawable.border_image_left_150));
    				   }
    				   
    				   f = new File(STORAGE_DIRECTORY + "/SantaTrainSettings/footer/right.png");
    				   if (f.exists() && f.isFile())
    				   {
    					   settings.setRightImage(BitmapFactory.decodeFile(f.getAbsolutePath()));
    				   }
    				   else
    				   {
    					   settings.setRightImage(BitmapFactory.decodeResource(getResources(), R.drawable.border_image_right_150));
    				   }
    				   
    	    	   }
    		   }
    	   }
       }
	   else
	   {
		   settings.addFooter();
		   settings.setLeftImage(BitmapFactory.decodeResource(getResources(), R.drawable.border_image_left_150));
		   settings.setRightImage(BitmapFactory.decodeResource(getResources(), R.drawable.border_image_right_150));
		   Toast.makeText(getApplicationContext(), STORAGE_DIRECTORY + "/SantaTrainSettings doesn't exist! Using defaults!", Toast.LENGTH_LONG).show();
	   }
   }
   
   public String readSettingsFile(String URI)

   {
	   File f = new File(STORAGE_DIRECTORY + "/SantaTrainSettings/footer/" + URI);
	   String sCurrentLine=null;
	   if(f.exists())
	   {
		   if(f.isFile())//do we have a file?
		   {
			   BufferedReader br = null;
			   
				try {

					br = new BufferedReader(new FileReader(f));		 
					sCurrentLine = br.readLine();

				} catch (IOException e) {
					return null;
				} finally {
					try {
						if (br != null)
						{
							br.close();
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					
				}
				
		   }
		   
	   }
	   return sCurrentLine;
   }
   
   public Uri getImageUri( int offset) {
	   File f = new File(STORAGE_DIRECTORY + "/SantaTrain");
	   if(f.exists())
       {
		   SimpleDateFormat s = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
		   String sdir=s.format(new Date());
		   File f2 = new File(STORAGE_DIRECTORY + "/SantaTrain/"+sdir+"/");
		   if(f2.exists())
		   {
	    	   if(f2.isDirectory()) 
	    	   {
	    		   
	    		   File file = new File(STORAGE_DIRECTORY + "/SantaTrain/"+sdir+"/", sdir + "_" + DEVICE_NAME +  String.format("%05d", (f2.listFiles().length + offset))+ ".jpg");
	        	   Uri imgUri = Uri.fromFile(file);
	        	   return imgUri;
	    	   }
	    	   else
	    	   {
	    		   Toast.makeText(getApplicationContext(), f2.getPath() + " is not a directory!", Toast.LENGTH_LONG).show();
	    		   finish();
	    		   return null;
	    	   }
		   }
		   else
		   {
			   if (f2.mkdir())
			   {
				   Toast.makeText(getApplicationContext(), "making dir " + f2.getPath(), Toast.LENGTH_LONG).show();
				   return getImageUri(offset);
			   }
			   else
			   {
				   Toast.makeText(getApplicationContext(), "couldn't make dir " + f2.getPath(), Toast.LENGTH_LONG).show();
				   finish();
			   }
		   }
		   
       }
	   else
	   {
		   if (f.mkdir())
		   {
			   Toast.makeText(getApplicationContext(), "making dir " + f.getPath(), Toast.LENGTH_LONG).show();
			   return getImageUri(offset);
		   }
		   else
		   {
			   Toast.makeText(getApplicationContext(), "couldn't make dir " + f.getPath(), Toast.LENGTH_LONG).show();
			   finish();
		   }
	   }
	   return null;
   }




   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   super.onActivityResult(requestCode, resultCode, data);
	   if (requestCode==0)
	   {
		   //Toast.makeText(getApplicationContext(), theURI.getPath(), Toast.LENGTH_LONG).show();
		   if (resultCode == Activity.RESULT_OK)
		   {
			   
		   	}
		   
		   if (resultCode == Activity.RESULT_CANCELED)
		   {
			   Toast.makeText(getApplicationContext(), "Finished!", Toast.LENGTH_LONG).show();
			   finish();
		   }
		   
	   }
	   if (requestCode==1)
	   {
		      return;
	   }
      
   }
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      //getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }



	public boolean demotryFooter(byte[] b)
	{

		// Compress current 'Bitmap' to 'output' as JPEG format


		//width: 1800
		//height: 1200
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		// Decode the JPEG byte array from 'output' to 'Bitmap' object
		//Bitmap bmp = resize(myURI.getPath());
		Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
		if (bmp==null) return false;
		//samsung galaxy cam
		//bmp=Bitmap.createScaledBitmap (bmp, 2000,1125,true);
		//moto z2
		bmp=Bitmap.createScaledBitmap (bmp, 2000,1500,true);
		// Use 'Canvas' to draw text on 'Bitmap'
		Canvas cv = new Canvas(bmp);
		if (settings.hasAFooter())
		{
			// Create 'Paint' buckets that will have different properties
			Paint messagePaint = new Paint();
			Paint footerPaint = new Paint();







			//Get some info on the painted text


			cv.drawBitmap(GlobalFooter, 0 , 0, messagePaint);



		}

		// Compress current 'Bitmap' to 'output' as JPEG format
		bmp.compress(CompressFormat.JPEG, 95, output);
		Uri myURI = getImageUri(0);

		System.out.println("***myURI , " + myURI);

		try {
			FileOutputStream outfile = new FileOutputStream(myURI.getPath());
			System.out.println("***outfile , " + outfile);

			try {
				outfile.write(output.toByteArray());
				outfile.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "couldn't alter photo file " + myURI.getPath(), Toast.LENGTH_LONG).show();
			return false;
		}
		return true;

	}



   public boolean addText(byte[] b)
   {
	   //width: 1800
	   //height: 1200
	   ByteArrayOutputStream output = new ByteArrayOutputStream();
	// Decode the JPEG byte array from 'output' to 'Bitmap' object
	   //Bitmap bmp = resize(myURI.getPath());
	   Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
	   if (bmp==null) return false;
	   //samsung galaxy cam
       //bmp=Bitmap.createScaledBitmap (bmp, 2000,1125,true);
	   //moto z2
	   bmp=Bitmap.createScaledBitmap (bmp, 2000,1500,true);
	   // Use 'Canvas' to draw text on 'Bitmap'
	   Canvas cv = new Canvas(bmp);

	   if (settings.hasAFooter())
	   {
		   // Create 'Paint' buckets that will have different properties
		   Paint messagePaint = new Paint();

		   Paint datePaint = new Paint();
		   Paint footerPaint = new Paint();
		   
		   
		   // Set the paint bucket properties
			messagePaint.setARGB(settings.msgFontColor().getA(), settings
					.msgFontColor().getR(), settings.msgFontColor().getG(),
					settings.msgFontColor().getB());
		   messagePaint.setTextSize(settings.getMessageTextFontSize());
		   
		   footerPaint.setARGB(settings.footerColor().getA(), settings
					.footerColor().getR(), settings.footerColor().getG(),
					settings.footerColor().getB());

			datePaint.setARGB(settings.copyrightFontColor().getA(), settings
					.copyrightFontColor().getR(), settings.copyrightFontColor()
					.getG(), settings.copyrightFontColor().getB());
		   datePaint.setTextSize(settings.getCopyrightFontSize());
		   
		   
		   //Create Resources from the paint buckets
		   String messagePaintText=settings.getMessageText();
		   
		   int footerHeight=settings.getFooterHeight();
		   Rect whiteFooter = new Rect();
		   whiteFooter.set(0, bmp.getHeight()-footerHeight, bmp.getWidth(), bmp.getHeight());
		   
		   
		   String datePaintText=settings.getCopyrightText();
		   
		   
		   
	
		   
		   //Get some info on the painted text
		   Rect bounds = new Rect();
		   messagePaint.getTextBounds(messagePaintText, 0, messagePaintText.length(), bounds);
		   int messagePaintTextWidth = bounds.width();
		   int messagePaintTextHeight = bounds.height();
		   
		   datePaint.getTextBounds(datePaintText, 0, datePaintText.length(), bounds);
		   int datePaintTextWidth = bounds.width();
		   int datePaintTextHeight = bounds.height();
		   
		   //Set the Top and Left of painted text
		   //int messagePaintTextLeft=(bmp.getWidth()-messagePaintTextWidth)/2;
		   int messagePaintTextLeft=(bmp.getWidth()-messagePaintTextWidth)/2;
		   //int messagePaintTextTop=(bmp.getHeight()-((footerHeight-messagePaintTextHeight)/2))-30;
		   int messagePaintTextTop=(bmp.getHeight()-footerHeight)+((footerHeight-messagePaintTextHeight)/2)+60;
		   
		   int datePaintTextTop = (bmp.getHeight()-datePaintTextHeight)-0;
		   int datePaintTextLeft = (bmp.getWidth()-datePaintTextWidth)-10;
	
	
		   
		   
		   
		   
		   
		   //Put the paint on the canvas
		   cv.drawRect(whiteFooter, footerPaint);
	       cv.drawBitmap(settings.getLeftImage(), 0, (bmp.getHeight()-footerHeight)+((footerHeight-settings.getLeftImage().getHeight())/2), null); // 155 is the center of the bottom
	       cv.drawBitmap(settings.getRightImage(), bmp.getWidth()-settings.getRightImage().getWidth(), (bmp.getHeight()-footerHeight)+((footerHeight-settings.getRightImage().getHeight())/2), null); // 155 is the center of the bottom, 150 is the width of the holly
		   cv.drawText(messagePaintText, messagePaintTextLeft,  messagePaintTextTop, messagePaint);
		   cv.drawText(datePaintText, datePaintTextLeft,  datePaintTextTop, datePaint);
	   }

	   // Compress current 'Bitmap' to 'output' as JPEG format
	   bmp.compress(CompressFormat.JPEG, 95, output);
	   Uri myURI = getImageUri(0);

	   System.out.println("***myURI , " + myURI);

	   try {
		FileOutputStream outfile = new FileOutputStream(myURI.getPath());
		   System.out.println("***outfile , " + outfile);

		   try {
			outfile.write(output.toByteArray());
			outfile.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		Toast.makeText(getApplicationContext(), "couldn't alter photo file " + myURI.getPath(), Toast.LENGTH_LONG).show();
		return false;
	}
	   return true;
   }


   
   public boolean wronggaddText(Uri myURI)
   {
	   //width: 1800
	   //height: 1200
	   ByteArrayOutputStream output = new ByteArrayOutputStream();
	// Decode the JPEG byte array from 'output' to 'Bitmap' object
	   //Bitmap bmp = resize(myURI.getPath());
	   Bitmap bmp = BitmapFactory.decodeFile(myURI.getPath());
	   if (bmp==null) return false;
	   bmp=Bitmap.createScaledBitmap (bmp, 2000,1200,true);
	   // Use 'Canvas' to draw text on 'Bitmap'
	   Canvas cv = new Canvas(bmp);
	   if (settings.hasAFooter())
	   {
		   // Create 'Paint' buckets that will have different properties
		   Paint messagePaint = new Paint();
		   Paint datePaint = new Paint();
		   Paint footerPaint = new Paint();


		   // Set the paint bucket properties
			messagePaint.setARGB(settings.msgFontColor().getA(), settings
					.msgFontColor().getR(), settings.msgFontColor().getG(),
					settings.msgFontColor().getB());
		   messagePaint.setTextSize(settings.getMessageTextFontSize());

		   footerPaint.setARGB(settings.footerColor().getA(), settings
					.footerColor().getR(), settings.footerColor().getG(),
					settings.footerColor().getB());

			datePaint.setARGB(settings.copyrightFontColor().getA(), settings
					.copyrightFontColor().getR(), settings.copyrightFontColor()
					.getG(), settings.copyrightFontColor().getB());
		   datePaint.setTextSize(settings.getCopyrightFontSize());


		   //Create Resources from the paint buckets
		   String messagePaintText=settings.getMessageText();

		   int footerHeight=settings.getFooterHeight();
		   Rect whiteFooter = new Rect();
		   whiteFooter.set(0, bmp.getHeight()-footerHeight, bmp.getWidth(), bmp.getHeight());


		   String datePaintText=settings.getCopyrightText();





		   //Get some info on the painted text
		   Rect bounds = new Rect();
		   messagePaint.getTextBounds(messagePaintText, 0, messagePaintText.length(), bounds);
		   int messagePaintTextWidth = bounds.width();
		   int messagePaintTextHeight = bounds.height();

		   datePaint.getTextBounds(datePaintText, 0, datePaintText.length(), bounds);
		   int datePaintTextWidth = bounds.width();
		   int datePaintTextHeight = bounds.height();

		   //Set the Top and Left of painted text
		   //int messagePaintTextLeft=(bmp.getWidth()-messagePaintTextWidth)/2;
		   int messagePaintTextLeft=(bmp.getWidth()-messagePaintTextWidth)/2;
		   //int messagePaintTextTop=(bmp.getHeight()-((footerHeight-messagePaintTextHeight)/2))-30;
		   int messagePaintTextTop=(bmp.getHeight()-footerHeight)+((footerHeight-messagePaintTextHeight)/2)+60;

		   int datePaintTextTop = (bmp.getHeight()-datePaintTextHeight)-0;
		   int datePaintTextLeft = (bmp.getWidth()-datePaintTextWidth)-10;

		   //Put the paint on the canvas
		   cv.drawRect(whiteFooter, footerPaint);
	       cv.drawBitmap(settings.getLeftImage(), 0, (bmp.getHeight()-footerHeight)+((footerHeight-settings.getLeftImage().getHeight())/2), null); // 155 is the center of the bottom
	       cv.drawBitmap(settings.getRightImage(), bmp.getWidth()-settings.getRightImage().getWidth(), (bmp.getHeight()-footerHeight)+((footerHeight-settings.getRightImage().getHeight())/2), null); // 155 is the center of the bottom, 150 is the width of the holly
		   cv.drawText(messagePaintText, messagePaintTextLeft,  messagePaintTextTop, messagePaint);
		   cv.drawText(datePaintText, datePaintTextLeft,  datePaintTextTop, datePaint);
	   }

	   // Compress current 'Bitmap' to 'output' as JPEG format
	   bmp.compress(CompressFormat.JPEG, 95, output);
	   try {
		FileOutputStream outfile = new FileOutputStream(myURI.getPath());
		try {
			outfile.write(output.toByteArray());
			outfile.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		Toast.makeText(getApplicationContext(), "couldn't alter photo file " + myURI.getPath(), Toast.LENGTH_LONG).show();
		return false;
	}
	   return true;
   }
   
   private Bitmap resize(String path){
	 //width: 1800
	   //height: 1200
	   int maxWidth=1800;
	   int maxHeight=1200;
	// create the options
	    BitmapFactory.Options opts = new BitmapFactory.Options();

	//just decode the file
	    opts.inJustDecodeBounds = true;
	    Bitmap bp = BitmapFactory.decodeFile(path, opts);

	//get the original size
	    int orignalHeight = opts.outHeight;
	    int orignalWidth = opts.outWidth;
	//initialization of the scale
	    int resizeScale = 1;
	//get the good scale
	    if ( orignalWidth > maxWidth || orignalHeight > maxHeight ) {
	       final int heightRatio = Math.round((float) orignalHeight / (float) maxHeight);
	       final int widthRatio = Math.round((float) orignalWidth / (float) maxWidth);
	       resizeScale = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	//put the scale instruction (1 -> scale to (1/1); 8-> scale to 1/8)
	    opts.inSampleSize = resizeScale;
	    opts.inJustDecodeBounds = false;
	//get the futur size of the bitmap
	    //int bmSize = (orignalWidth / resizeScale) * (orignalHeight / resizeScale) * 4;
	//check if it's possible to store into the vm java the picture
	    bp = BitmapFactory.decodeFile(path, opts);
	    return bp;
	}
   
   private File findExternalStorage()
   {
	   String path;
	   String thePath=Environment.getExternalStorageDirectory().getAbsolutePath();
	   File file = new File("/system/etc/vold.fstab");
	    FileReader fr = null;
	    BufferedReader br = null;
	   
	    try {
	        fr = new FileReader(file);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } 
	   
	    try {
	        if (fr != null) {
	            br = new BufferedReader(fr);
	            String s = br.readLine();
	            while (s != null) {
	                if (s.startsWith("dev_mount")) {
	                    String[] tokens = s.split("\\s");
	                    path = tokens[2]; //mount_point
	                    if (!Environment.getExternalStorageDirectory().getAbsolutePath().equals(path)) {
	                    	//TEST THIS PATH to see if it works
	                    	if (testPath(path))
	                    	{
	                    		thePath=path;
	                    		break;
	                    	}
	                    }
	                }
	                s = br.readLine();
	            }
	        }            
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (fr != null) {
	                fr.close();
	            }            
	            if (br != null) {
	                br.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return new File (thePath);

   }
   
   //make the test file twice to test if it works
   private boolean testPath(String path)
   {
	   File file = new File(path + "/SantaPrinterTester");
		try {
			if (file.createNewFile())
			{
				//System.out.println("File is created!");
			} else
			{
				//System.out.println("File already exists.");
			}

		} catch (IOException e)
		{
			return false;
		}
		try {
			if (file.createNewFile())
			{
				return false;
			} else
			{
				return true;
			}

		} catch (IOException e)
		{
			return false;
		}
   }
   
   
   
   @Override
	protected void onPause() {
		super.onPause();
		//when on Pause, release camera in order to be used from other applications
		//releaseCamera();
       //System.out.println("OnPause called");

           if (!checkPermission("Pause"))
           {
               requestPermission();
               return;
           }



       setBGToGeneric();
       hideCameraPreview();
       }


   
   @Override
	protected void onStop() {
	   //releaseCamera();
	   super.onStop();
   }

	private boolean hasCamera(Context context) {
		//check if the device has camera
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	private PictureCallback getPictureCallback() {
		PictureCallback picture = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				//make a new picture file
				/*File pictureFile = getOutputMediaFile();
				
				if (pictureFile == null) {
					return;
				}
				try {
					//write the file
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
					Toast toast = Toast.makeText(myContext, "Picture saved: " + pictureFile.getName(), Toast.LENGTH_LONG);
					toast.show();

				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
				*/
				//refresh camera to continue preview
				mPreview.refreshCamera(mCamera);
				processPicture(data);
				

			}
		};
		return picture;
	}
	
	private void checkIfPicOK()
	{
        Globals.takingPicture=true;
		TextView content = new TextView(this);
        content.setText("Print this photo?");
        content.setTypeface(Typeface.SANS_SERIF);
        content.setTextSize((float) 10.0);
        
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		
	    //.setTitle("Use this?")
		alertDialogBuilder
		//.setMessage("Print this photo?")
		.setView(content)
	    .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	
	        	//Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_LONG).show();
				  //Intent intent = new Intent ("org.androidprinting.intent.action.PRINT");
				  //intent.setDataAndType( getImageUri(-1), "image/jpeg" );
				  //startActivityForResult(intent, 1);
                Globals.takingPicture=false;
			   	Globals.fileToUpload=getImageUri(-1).getPath();
			   	Intent intent = new Intent(MainActivity.this, UploadActivity.class);
			   	MainActivity.this.startActivity(intent);
				  //finish();
	        	
	        	
	        	
	        	
	        }
	     })
	    .setPositiveButton("No", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {

                Globals.takingPicture=false;
	        	Globals.isBusy=false;
	        	showCameraPreview();
	        	return;
	        	
	        	
	     }
	    });
		AlertDialog dialog =alertDialogBuilder.create();
		dialog.setOnCancelListener(new OnCancelListener() {
	  		 
	   	    public void onCancel(final DialogInterface dialog) {
                Globals.takingPicture=false;
	   	    	Globals.isBusy=false;
	        	showCameraPreview();
	        	return;
	   	    }
	   	    });
	   	 
	    
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	     WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

	     wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
	     dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
         dialog.setCanceledOnTouchOutside(false);
         dialog.setCancelable(false);
	     dialog.show();
	}
	
	private void processPicture(byte[] b)
	{
		if (demotryFooter(b))
		   {
			
			setBGToLastPhoto();
			checkIfPicOK();
			
		   }
		   else
		   {
			   new AlertDialog.Builder(this)
			    .setTitle("Error")
			    .setMessage("A problem was detected writing files. Possible space or integrity issue. What should I do?")
			    .setNegativeButton("Rename Pic Folder", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			        	File file = new File(STORAGE_DIRECTORY + "/SantaTrain/");
			        	file.renameTo(new File(STORAGE_DIRECTORY + "/SantaTrain"+(int) (System.currentTimeMillis() / 1000L)+"/"));
			        	finish();
			        }
			     })
			    .setPositiveButton("Delete Pic Folder", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			        	File file = new File(STORAGE_DIRECTORY + "/SantaTrain/");
			        	String[]entries = file.list();
			        	for(String s: entries){
			        	    File currentFile = new File(file.getPath(),s);
			        	    currentFile.delete();
			        	}
			        	file.delete();
			        	finish();
			        }
			     })
			    .setNeutralButton("Do nothing", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            // do nothing
			        	finish();
			        }
			     })
			    .setIcon(android.R.drawable.ic_dialog_alert)
			     .show();
		   }
	}

	private void takeAPicture()
	{
		if (!Globals.isBusy)
		{
			Globals.isBusy=true;
			setBGToGeneric();
			hideCameraPreview();
			Parameters params;
			if (android.os.Build.VERSION.SDK_INT<21)
			{
				params = mCamera.getParameters();
				params.setFlashMode(Parameters.FLASH_MODE_ON);
				params.setPictureSize(20000, 18000);
				params.setJpegQuality(75);
				mCamera.setParameters(params);
			}
			mCamera.takePicture(null, null, mPicture);
			if (android.os.Build.VERSION.SDK_INT<21)
			{
				
				params = mCamera.getParameters();
				params.setFlashMode(Parameters.FLASH_MODE_OFF);
				mCamera.setParameters(params);
			}
		}
	}
	//make picture and save to a folder
	private File getOutputMediaFile() {
		
		Uri a = getImageUri(0);
		return new File(a.getPath());
	}

	private void releaseCamera() {
		// stop and release camera
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}
	

	private int zoom=0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Camera.Parameters p = mCamera.getParameters();
		isFocused=false;
		p.set("mode", "smart-auto");
		mCamera.setParameters(p);
		p = mCamera.getParameters();
		zoom=p.getZoom();
		int maxzoom=p.getMaxZoom();
		
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			takeAPicture();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			if (p.isZoomSupported() == true) {
				if (zoom<maxzoom)
				{
					zoom++;
					p.setZoom(zoom);
					mCamera.setParameters(p);
					setAutoFocus();
				}
			}
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_ZOOM_IN) {

			if (p.isZoomSupported() == true) {
				p.set("zoom-action", "optical-tele-start");
				mCamera.setParameters(p);
				Log.d("santaprinterftp","ZOOM IN");
			}

			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			if (p.isZoomSupported() == true) {
				if (zoom>0)
				{
					zoom--;
					p.setZoom(zoom);
					mCamera.setParameters(p);
					setAutoFocus();
				}
				
			}
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_ZOOM_OUT) {

			// decrease your zoom

			if (p.isZoomSupported() == true) {
				p.set("zoom-action", "optical-wide-start");
				
				mCamera.setParameters(p);
				
			}
			return true;
		}
		if (keyCode==KeyEvent.KEYCODE_BACK && !Globals.isBusy)
		{
			setBGToGeneric();
			hideCameraPreview();
			releaseCamera();
			finish();
		}
		if (keyCode==KeyEvent.KEYCODE_HOME)
		{
			setBGToGeneric();
			hideCameraPreview();
			releaseCamera();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		 if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_ZOOM_IN || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_ZOOM_OUT) {
			 Camera.Parameters p = mCamera.getParameters();
			 p.set("zoom-action", "zoom-stop");
			 mCamera.setParameters(p);
			 Log.d("santaprinterftp","ZOOM STOP");
			 setAutoFocus();
			 return true;
		 }
		 return super.onKeyUp(keyCode, event);
	}
	
	private boolean isFocused=true;
	private boolean isAutoFocusing=false;
	public void setAutoFocus(){
	    if(mCamera == null){
	        return;
	    }

	    if(isFocused)
	        return;

	    if(isAutoFocusing){
	        mCamera.cancelAutoFocus();
	        isAutoFocusing = false;
	    }

	    isAutoFocusing = true;
	    mCamera.autoFocus(new Camera.AutoFocusCallback() {

	        @Override
	        public void onAutoFocus(boolean success, Camera camera) {
	            isAutoFocusing = false;
	            isFocused = success;
	        }
	    });

	}
	private void hideCameraPreview()
	{
		cameraPreview.setVisibility(View.GONE);
	}
	
	private void showCameraPreview()
	{
		cameraPreview.setVisibility(View.VISIBLE);
	}
	
	@SuppressLint("NewApi")
	private void setBGToLastPhoto()
	{
		if (android.os.Build.VERSION.SDK_INT<16) return;
		String filepath = getImageUri(-1).getPath();
		Drawable d = Drawable.createFromPath(filepath);
		BG.setBackground(d);
	}
	
	@SuppressLint("NewApi")
	private void setBGToByteArray(byte[] b)
	{
		if (android.os.Build.VERSION.SDK_INT<16) return;
		Drawable d = new BitmapDrawable(BitmapFactory.decodeByteArray(b, 0, b.length));
		BG.setBackground(d);
	}
	
	@SuppressLint("NewApi")
	private void setBGToGeneric()
	{
		if (android.os.Build.VERSION.SDK_INT<16) return;
		Drawable d = getResources().getDrawable( R.drawable.bg );
		BG.setBackground(d);
	}
	

	private OnClickListener previewClickListener = new OnClickListener() {
		
	    public void onClick(View v) {
	    	
			Toast toast = Toast.makeText(myContext, "Taking a picture...", Toast.LENGTH_SHORT);
			toast.show();
			takeAPicture();
	      }
	    
	  };
	  
	  private OnLongClickListener  previewLongClickListener = new OnLongClickListener() {
		
		  @Override
		  public boolean onLongClick(View v) {
				Toast toast = Toast.makeText(myContext, "Attempting to focus...", Toast.LENGTH_SHORT);
				toast.show();
				isFocused=false;
				setAutoFocus();
				return true;
			}
		  	
	  };

	
   
}
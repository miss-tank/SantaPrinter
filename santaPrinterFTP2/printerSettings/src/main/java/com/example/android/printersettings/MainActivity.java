package com.example.android.printersettings;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {




    private ImageView leftImageView;
    private ImageView rightImageView;

    private static final int PERMISSION_REQUEST_CODE = 200;
    private Cursor cursor;
    private  int columnIndex;
    Button  leftUpload;
    Button  rightUpload;

    Button showPreview;
    Button saveSettings;


    private static  final int PICK_LEFT = 1;
    private static  final int PICK_RIGHT = 2;

    private Button demo;





    private static Context context;
    Bitmap left;
    Bitmap right;



    String leftLink;
    String rightLink;

    EditText centerText;
    String centerStringMessage;

    File STORAGE_DIRECTORY=Environment.getExternalStorageDirectory();
    String DEVICE_NAME="";
    BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();


    PrinterSettings settings = new PrinterSettings();

    CheckBox copyrightText;




    static  boolean  PERMISSION_GRANTED = false;
    Bitmap FINAL_BITMAP_CREATED;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();

        if (!checkPermission("Create")) {
            requestPermission();
        }



        STORAGE_DIRECTORY=findExternalStorage();


        if (myDevice!=null)
        {
            DEVICE_NAME=myDevice.getName();
        }


        copyrightText= (CheckBox) findViewById(R.id.DateCheckbox);

        leftImageView=(ImageView) findViewById(R.id.leftImageView);
        leftUpload = (Button) findViewById(R.id.ButtonLeftupload);


        rightImageView=(ImageView) findViewById(R.id.rightImageView);
        rightUpload = (Button) findViewById(R.id.ButtonRightupload);


        showPreview = (Button) findViewById(R.id.previewImage);
        saveSettings = (Button) findViewById(R.id.saveSettings);

        centerText = (EditText) findViewById(R.id.centerEditText);


        //demo = (Button) findViewById(R.id.getfirstimage);


        leftUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PERMISSION_GRANTED)
                {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_LEFT);
                }
                else
                {
                    requestPermission();
                }



            }
         });






        rightUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_RIGHT);
                }
                else
                {
                    requestPermission();
                }
            }
        });


        showPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCanvas();

            }
        });



        saveSettings.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

               saveFotter(FINAL_BITMAP_CREATED);
            }


        } );





        SharedPreferences getSavedValues = getSharedPreferences("Prefs",0);
        String leftBmpLink = getSavedValues.getString("leftLink"," ");
        String rightBmpLink = getSavedValues.getString("rightLink"," ");
         Boolean x = restoreImage(leftBmpLink,leftImageView , "l");

        System.out.println("For x left " + x);


        x = x && restoreImage(rightBmpLink,rightImageView , "r");

        System.out.println("For x right " + x);

        if(x)
        {
            createCanvas();
        }

    }



    public boolean restoreImage(String link, ImageView view, String side)
    {

        System.out.println("## Link lenth "+ link.length() + "Link '"+ link+"'");
        if(!(link.length()<3))
        {
            Uri uri = Uri.parse(link);
            Log.i("$$", "Uri: " + uri.toString());
            try {
                Bitmap bitmap = getBitmapFromUri(uri);
                System.out.println("**** This is teh ool "+ link.contains("left") );

                if(side.equals("l"))
                {
                    System.out.println("** In left");
                    settings.setLeftImage(bitmap);

                }
                else {
                    System.out.println("** In right");
                    settings.setRightImage(bitmap);
                }
                view.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }


            return false;
    }




    //DONOT DELETE THIS
    private void defaultfooter() {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.bell);
        settings.setRightImage(icon);
        settings.setLeftImage(icon);



        int w = 100;
            int h = 60;

            centerStringMessage = centerText.getText().toString();
            settings.setMessageText(centerStringMessage);

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap bmp = Bitmap.createBitmap(w, h, conf);

            bmp=Bitmap.createScaledBitmap (bmp, 2000,2000,true);


            Canvas cv = new Canvas(bmp);
            ImageView canvasView;
            canvasView = (ImageView) findViewById(R.id.CanvasPreview1);

            Paint messagePaint = new Paint();
            Paint datePaint = new Paint();
            Paint footerPaint = new Paint();

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


            String messagePaintText=settings.getMessageText();

            int footerHeight=settings.getFooterHeight();
            Rect whiteFooter = new Rect();
            whiteFooter.set(0, bmp.getHeight()-footerHeight, bmp.getWidth(), bmp.getHeight());

            String datePaintText="";
            if(copyrightText.isChecked()) {
                 datePaintText = settings.getCopyrightText();
            }
            System.out.println("***** Date is " + datePaintText);

            Rect bounds = new Rect();
            messagePaint.getTextBounds(messagePaintText, 0, messagePaintText.length(), bounds);
            int messagePaintTextWidth = bounds.width();
            int messagePaintTextHeight = bounds.height();

            datePaint.getTextBounds(datePaintText, 0, datePaintText.length(), bounds);
            int datePaintTextWidth = bounds.width();
            int datePaintTextHeight = bounds.height();

            int messagePaintTextLeft=(bmp.getWidth()-messagePaintTextWidth)/2;
            int messagePaintTextTop=(bmp.getHeight()-footerHeight)+((footerHeight-messagePaintTextHeight)/2)+60;

            int datePaintTextTop = (bmp.getHeight()-datePaintTextHeight)-0;
            int datePaintTextLeft = (bmp.getWidth()-datePaintTextWidth)-10;

            cv.drawRect(whiteFooter, footerPaint);
            cv.drawBitmap(settings.getLeftImage(), 0, (bmp.getHeight()-footerHeight)+((footerHeight-settings.getLeftImage().getHeight())/2), null); // 155 is the center of the bottom
            cv.drawBitmap(settings.getRightImage(), bmp.getWidth()-settings.getRightImage().getWidth(), (bmp.getHeight()-footerHeight)+((footerHeight-settings.getRightImage().getHeight())/2), null); // 155 is the center of the bottom, 150 is the width of the holly
            cv.drawText(messagePaintText, messagePaintTextLeft,  messagePaintTextTop, messagePaint);
            cv.drawText(datePaintText, datePaintTextLeft,  datePaintTextTop, datePaint);

            bmp.compress(CompressFormat.JPEG, 95, output);

            canvasView.setImageBitmap(bmp);



    }



    protected void onStart()
    {
        super.onStart();


    }


    private void requestPermission() {
        System.out.println("PERMISSION_REQUEST_CODE code  before evrything: " + PERMISSION_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE+1);
        System.out.println("PERMISSION_REQUEST_CODE code  after request : " + PERMISSION_REQUEST_CODE);

    }

    private static boolean checkPermission(String s) {


        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        System.out.println("&& checkPermission "  + s);
            if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
                System.out.println("*** , it is a valid build SDK");
                int result2 = ContextCompat.checkSelfPermission(MainActivity.context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                System.out.println("&& result2 " + result2 );

                int result3 = ContextCompat.checkSelfPermission(MainActivity.context, Manifest.permission.READ_EXTERNAL_STORAGE);
                System.out.println("&& result3 " + result3 );

                int final_result = result2 & result3 ;
                System.out.println("&& final_result " + final_result );

                if(final_result==0)
                    PERMISSION_GRANTED=true;

            }

            return PERMISSION_GRANTED;
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

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        System.out.println("&& PERMISSION_REQUEST_CODE code while check: " + PERMISSION_REQUEST_CODE);
//
//        System.out.println("&& Request code : " + requestCode);
//
//        System.out.println("&& permissions code : " + permissions.toString());
//        System.out.println("&& grantResults code : " + grantResults.toString());


        //Permission granted
        if(requestCode!=PERMISSION_REQUEST_CODE)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                //System.out.println("&& SDK capable");

                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showMessageOKCancel("You need to allow access to External Storage to proceed.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                                PERMISSION_REQUEST_CODE);
                                        checkPermission("m");
                                    }
                                }
                            });
                    return;
                }
            }

        }

        checkPermission("m");

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
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
            super.onRestoreInstanceState(savedInstanceState);

        String stateSaved  = savedInstanceState.getString("Save_LeftBmp");
        try {
            Bitmap bitmap = getBitmapFromUri(Uri.parse(stateSaved));
            if(bitmap!=null)
                leftImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }




    }


    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("Save_LeftBmp", leftLink);

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {


        SharedPreferences saveValues = getSharedPreferences("Prefs",0);
        SharedPreferences.Editor editor = saveValues.edit();


        if (requestCode == PICK_LEFT && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                leftLink = uri.toString();
                editor.putString("leftLink", leftLink);
                Log.i("$$", "Uri: " + uri.toString());
                try {
                    Bitmap bitmap = getBitmapFromUri(uri);
                    left=bitmap;
                    leftImageView.setImageBitmap(bitmap);
                    settings.setLeftImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else  if (requestCode == PICK_RIGHT && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                rightLink = uri.toString();
                editor.putString("rightLink", rightLink);

                Log.i("$$", "Uri: " + uri.toString());
                try {
                    Bitmap bitmap = getBitmapFromUri(uri);
                    right=bitmap;
                    rightImageView.setImageBitmap(bitmap);
                    settings.setRightImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        editor.commit();

    }



    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public void createCanvas()
    {
//
//        Bitmap bmpp = BitmapFactory.decodeResource(context.getResources(),
//                R.drawable.bell);
//        settings.setRightImage(bmpp);
//        settings.setLeftImage(bmpp);

        int w = 100;
        int h = 60;

        centerStringMessage = centerText.getText().toString();
        settings.setMessageText(centerStringMessage);


        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(w, h, conf);

        bmp=Bitmap.createScaledBitmap (bmp, 2000,200,true);



        Canvas cv = new Canvas(bmp);
        ImageView canvasView;
        cv.drawColor(Color.TRANSPARENT);


        canvasView = (ImageView) findViewById(R.id.CanvasPreview1);

            Paint messagePaint = new Paint();
            Paint datePaint = new Paint();
            Paint footerPaint = new Paint();


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


            String messagePaintText=settings.getMessageText();

            int footerHeight=settings.getFooterHeight();
            Rect whiteFooter = new Rect();
            whiteFooter.set(0, bmp.getHeight()-footerHeight, bmp.getWidth(), bmp.getHeight());


             String datePaintText="";
                if(copyrightText.isChecked()) {
                 datePaintText = settings.getCopyrightText();
                }

            Rect bounds = new Rect();
            messagePaint.getTextBounds(messagePaintText, 0, messagePaintText.length(), bounds);
            int messagePaintTextWidth = bounds.width();
            int messagePaintTextHeight = bounds.height();

            datePaint.getTextBounds(datePaintText, 0, datePaintText.length(), bounds);
            int datePaintTextWidth = bounds.width();
            int datePaintTextHeight = bounds.height();

            int messagePaintTextLeft=(bmp.getWidth()-messagePaintTextWidth)/2;
            int messagePaintTextTop=(bmp.getHeight()-footerHeight)+((footerHeight-messagePaintTextHeight)/2)+60;

            int datePaintTextTop = (bmp.getHeight()-datePaintTextHeight)-0;
            int datePaintTextLeft = (bmp.getWidth()-datePaintTextWidth)-10;

            cv.drawRect(whiteFooter, footerPaint);
            cv.drawBitmap(settings.getLeftImage(), 0, (bmp.getHeight()-footerHeight)+((footerHeight-settings.getLeftImage().getHeight())/2), null); // 155 is the center of the bottom
            cv.drawBitmap(settings.getRightImage(), bmp.getWidth()-settings.getRightImage().getWidth(), (bmp.getHeight()-footerHeight)+((footerHeight-settings.getRightImage().getHeight())/2), null); // 155 is the center of the bottom, 150 is the width of the holly
            cv.drawText(messagePaintText, messagePaintTextLeft,  messagePaintTextTop, messagePaint);
            cv.drawText(datePaintText, datePaintTextLeft,  datePaintTextTop, datePaint);

        canvasView.setImageBitmap(bmp);


        System.out.println("This is teh dimension w: "+ bmp.getWidth() + " H: "+bmp.getHeight());
        FINAL_BITMAP_CREATED=bmp;


    }


    public void saveFotter(Bitmap bmp)
    {

//                bmp = BitmapFactory.decodeResource(context.getResources(),
//            R.drawable.bellpng);







        Uri myURI = getImageUri(0);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            FileOutputStream outfile = new FileOutputStream(myURI.getPath());
            bmp.compress(CompressFormat.PNG, 0, outfile);

            try {
               outfile.write(os.toByteArray());
                outfile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "unable to save" + myURI.getPath(), Toast.LENGTH_LONG).show();
        }
    }


    public Uri getImageUri( int offset) {
        File f = new File(STORAGE_DIRECTORY + "/SantaTrain");
        if(f.exists())
        {
            System.out.println("f exists");
            String sdir="Footer";

            File f2 = new File(STORAGE_DIRECTORY + "/SantaTrain/"+sdir+"/");
            if(f2.exists())
            {
                System.out.println("f2 exists");

                if(f2.isDirectory())
                {
                    System.out.println("f22 is a directory");
                    File file = new File(STORAGE_DIRECTORY + "/SantaTrain/"+sdir+"/", sdir + "_"+ ".png");
                    Uri imgUri = Uri.fromFile(file);
                    System.out.println("^^^ this the uri "+ imgUri );
                    System.out.println("f22 is a directory " + imgUri);
                    Toast.makeText(getApplicationContext(), f2.getPath() + " Saved Successfuly ", Toast.LENGTH_LONG).show();

                    MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String s, Uri u) {
                                    System.out.println("This is teh string "+ s);
                                    System.out.println("This is teh uri "+ u);

                                }
                            }
                    );
                    return imgUri;
                }
                else
                {
                    System.out.println("f is not a  directory");

                    Toast.makeText(getApplicationContext(), f2.getPath() + " is not a directory!", Toast.LENGTH_LONG).show();
                    finish();
                    return null;
                }
            }
            else
            {
                System.out.println("f2 doesnt exist");

                if (f2.mkdir())
                {
                    System.out.println("f2  making dir");

                    Toast.makeText(getApplicationContext(), "making dir " + f2.getPath(), Toast.LENGTH_LONG).show();
                    return getImageUri(offset);
                }
                else
                {
                    System.out.println("f2 not making dir");

                    Toast.makeText(getApplicationContext(), "couldn't make dir " + f2.getPath(), Toast.LENGTH_LONG).show();
                    finish();
                }
            }

        }
        else
        {
            System.out.println("f doesnt exist");
            if (f.mkdir())
            {
                System.out.println("f  making dir");

                Toast.makeText(getApplicationContext(), "making dir " + f.getPath(), Toast.LENGTH_LONG).show();
                return getImageUri(offset);
            }
            else
            {
                System.out.println("f not making dir");

                Toast.makeText(getApplicationContext(), "couldn't make dir " + f.getPath(), Toast.LENGTH_LONG).show();
                finish();
            }
        }
        return null;
    }




}

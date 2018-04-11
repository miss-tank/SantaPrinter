package com.pkgingo.santaprinterftp2;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import java.io.File;

public class MainActivity2 extends AppCompatActivity {

    File STORAGE_DIRECTORY= Environment.getExternalStorageDirectory();
    String DEVICE_NAME="";
    BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
    final String TS=""+(System.currentTimeMillis() / 1000L);
    private Camera mCamera;
    private CameraPreview mPreview;
    private Context myContext;
    private LinearLayout cameraPreview;
    private LinearLayout BG;
    private static final int REQUEST_GET_ACCOUNT = 112;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
}

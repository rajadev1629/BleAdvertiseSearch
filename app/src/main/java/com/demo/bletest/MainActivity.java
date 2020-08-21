package com.demo.bletest;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mText;
    private Button mAdvertiseButton;
    private Button mDiscoverButton;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public static final String CHANNEL_ID = "BLE_CHANNEL";

    private BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        mText = (TextView) findViewById(R.id.text);
        mDiscoverButton = (Button) findViewById(R.id.discover_btn);
        mAdvertiseButton = (Button) findViewById(R.id.advertise_btn);

        mDiscoverButton.setOnClickListener(this);
        mAdvertiseButton.setOnClickListener(this);

        mText.setText(MyPrefs.getString(this,"result", ""));
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(BleScannerService.MESSAGE);
                mText.setText(s);
                // do something here.
            }
        };

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "No bluetooth support on this device", Toast.LENGTH_SHORT).show();

        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled :)
            mBluetoothAdapter.enable();
        }

        ActivityCompat.requestPermissions(MainActivity.this, REQUESTED_PERMISSIONS, 101);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(BleScannerService.RESULT));
    }

    @Override
    protected void onPause() {

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }catch (Exception e){}
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied cant continue", Toast.LENGTH_SHORT).show();
                    System.exit(0);
                }
                return;
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {

        createNotificationChannel();
        if (v.getId() == R.id.discover_btn) {
            stopDiscoverService();
            startScanningService();
        } else if (v.getId() == R.id.advertise_btn) {
            mText.setText("");
            stopScanningService();
            startAdvertService();
        }
    }

    private void startScanningService() {
        Intent intentService = new Intent(MainActivity.this, BleScannerService.class);
        intentService.putExtra("putExtra", "Ble Service");
        startService(intentService);
    }

    private void stopScanningService() {
        Intent intentService = new Intent(MainActivity.this, BleScannerService.class);
        stopService(intentService);
    }

    private void startAdvertService() {
        Intent intentService = new Intent(MainActivity.this, BleAdveriseService.class);
        intentService.putExtra("putExtra", "Ble Service");
        startService(intentService);
    }

    private void stopDiscoverService() {
        Intent intentService = new Intent(MainActivity.this, BleAdveriseService.class);
        stopService(intentService);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "Corey Channel", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        };
    }
}

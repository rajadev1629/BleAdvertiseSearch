package com.demo.bletest;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class BleService extends Service {


    private BluetoothLeScanner mBluetoothLeScanner;
    public static final String TAG = "kkkkkkk";

    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        discover();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = "Ble Service";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


        Notification notification = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setContentTitle(input)
                .setContentText(input + " Running")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void discover() {
        List<ScanFilter> filters = new ArrayList<ScanFilter>();

        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid))))
                .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

       /* mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }, 10000);*/
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result == null
                    || result.getDevice() == null
                    || TextUtils.isEmpty(result.getDevice().getAddress()))
                return;

            Log.e(TAG, "onScanResult: " + result.getDevice().getAddress() + result.getScanRecord().getDeviceName());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BLE", "Discovery onScanFailed: " + errorCode);
            super.onScanFailed(errorCode);
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void advertise() {
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        /*AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(false)
                .setTimeout(10000)
                .build();*/

        AdvertisingSetParameters parameters = (new AdvertisingSetParameters.Builder())
                .setLegacyMode(true) // True by default, but set here as a reminder.
                .setConnectable(false)
                .setScannable(true)
                .setInterval(AdvertisingSetParameters.INTERVAL_LOW)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_HIGH)
                .build();

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid)));

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(pUuid)
                .build();

        /*AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };*/


        AdvertisingSetCallback callback = new AdvertisingSetCallback() {
            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                Log.i(TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                        + status);
            }

            @Override
            public void onAdvertisingDataSet(AdvertisingSet advertisingSet, int status) {
                Log.i(TAG, "onAdvertisingDataSet() :status:" + status);
            }

            @Override
            public void onScanResponseDataSet(AdvertisingSet advertisingSet, int status) {
                Log.i(TAG, "onScanResponseDataSet(): status:" + status);
            }

            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                Log.i(TAG, "onAdvertisingSetStopped():");
            }
        };
        advertiser.startAdvertisingSet(parameters, data, null, null, null, callback);


       /* if (currentAdvertisingSet != null) {
            // After onAdvertisingSetStarted callback is called, you can modify the
            // advertising data and scan response data:
            currentAdvertisingSet.setAdvertisingData(new AdvertiseData.Builder().
                    setIncludeDeviceName(true).setIncludeTxPowerLevel(true).build());
            // Wait for onAdvertisingDataSet callback...
            currentAdvertisingSet.setScanResponseData(new
                    AdvertiseData.Builder().addServiceUuid(new ParcelUuid(UUID.randomUUID())).build());
            // Wait for onScanResponseDataSet callback...

            // When done with the advertising:
            advertiser.stopAdvertisingSet(callback);
        }*/


        /* advertiser.startAdvertising( settings, data, advertisingCallback );*/
    }
}

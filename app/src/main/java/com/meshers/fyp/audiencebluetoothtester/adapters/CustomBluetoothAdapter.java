package com.meshers.fyp.audiencebluetoothtester.adapters;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;
import java.lang.reflect.Method;
/**
 * Created by sarahcs on 2/19/2017.
 */

public class CustomBluetoothAdapter {

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter customBluetoothAdapter;

    private Context context;

    private String BTName;

    public CustomBluetoothAdapter(Context context) {

        this.context = context;
        this.customBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public Context getContext() {
        return context;
    }

    public boolean isSupported() {
        return customBluetoothAdapter != null;
    }

    public boolean on(String BTName) {
        this.BTName = BTName;

        if (!customBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            activity.startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            return true;
        } else {
            return false;
        }

    }

    public void setName(String name) {
        BTName = name;
        if (!customBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            activity.startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
        } else {
//            if (customBluetoothAdapter.getScanMode() !=
//                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            customBluetoothAdapter.setName(name);
            makeDiscoverable(3000);
        }
    }

    public String getName() {
        return customBluetoothAdapter.getName();
    }

    public void makeDiscoverable(int timeOut) {
        Class<?> baClass = BluetoothAdapter.class;
        Method[] methods = baClass.getDeclaredMethods();
        Method mSetScanMode = null;
        for (Method method : methods) {
            if (method.getName().equals("setScanMode") && method.getParameterTypes().length == 2
                    && method.getParameterTypes()[0].equals(int.class)
                    && method.getParameterTypes()[1].equals(int.class)) {
                mSetScanMode = method;
                break;
            }
        }
        try {
            mSetScanMode.invoke(customBluetoothAdapter,
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeOut);
        } catch (Exception e) {
            Log.e("discoverable", e.getMessage());
            for (Class parameter : mSetScanMode.getParameterTypes()) {
                System.out.println("PARAM:" + parameter);
            }
        }
    }

    public boolean off() {
        return customBluetoothAdapter.disable();
    }


    public void find() {

        if (customBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            customBluetoothAdapter.cancelDiscovery();
        }
        customBluetoothAdapter.startDiscovery();
    }
}

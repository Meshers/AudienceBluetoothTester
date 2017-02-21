package com.meshers.fyp.audiencebluetoothtester.helpers;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.meshers.fyp.audiencebluetoothtester.MainActivity;
import com.meshers.fyp.audiencebluetoothtester.adapters.CustomBluetoothAdapter;
import com.meshers.fyp.audiencebluetoothtester.interfaces.DeviceDiscoveryHandler;
import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;

/**
 * Created by sarahcs on 2/19/2017.
 */

public class BTHelper {

    private CustomBluetoothAdapter mBluetoothAdapter;
    private DeviceDiscoveryHandler mDiscoveryHandler;

    private HashSet<Byte> teacherDeviceIds;

    private boolean mBtReceiverRegistered = false;
    private byte fromAddr;

    public void setFromAddress(byte fromAddr){
        this.fromAddr = fromAddr;
    }

    private BroadcastReceiver mBtReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            System.out.println("ACTION:" + intent.getAction());
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDiscoveryHandler.handleStarted();
            }

            // if our discovery has finished, time to start again!
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mDiscoveryHandler.handleFinished();
            }

            // When discovery finds a device
            if (!BluetoothDevice.ACTION_FOUND.equals(action)) return;
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (!LinkLayerPdu.isValidPdu(device.getName())) return;
            try {
                LinkLayerPdu pdu = new LinkLayerPdu(device.getName());

                // check if the received packet is from a teacher
                if(!teacherDeviceIds.contains(pdu.getFromAddress())) return;

                mDiscoveryHandler.handleDiscovery(pdu);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    public BTHelper(CustomBluetoothAdapter bluetoothAdapter, DeviceDiscoveryHandler discoveryHandler) {
        mBluetoothAdapter = bluetoothAdapter;
        mDiscoveryHandler = discoveryHandler;

        teacherDeviceIds = new HashSet<>();
        teacherDeviceIds.add((byte)1);
    }

    public void startDiscovery() {
        mBluetoothAdapter.find();
    }

    public void startListening() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // check started just for debugging purposes
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mBluetoothAdapter.getContext().registerReceiver(mBtReceiver, filter);
    }

    public String startSendingData(String data) {

        try{
            return startSendingData(data.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e){
            Log.e("LLPDU", "Failed to decode", e);
            return null;
        }
    }

    public String startSendingData(byte[] data){

        LinkLayerPdu packet = new LinkLayerPdu(fromAddr, data);
        mBluetoothAdapter.setName(packet.getPduAsString());

        if (!mBtReceiverRegistered) {
            startListening();
            mBtReceiverRegistered = true;
        }

        startDiscovery();
        return packet.getPduAsString();
    }

    public boolean stopSendingData(){

        if (!mBtReceiverRegistered) {
            return false;
        }
        stopListening();
        mBtReceiverRegistered = false;

        // set the discovery interval to 0
        //mBluetoothAdapter.makeDiscoverable(1);
        return mBluetoothAdapter.off();
    }

    public void stopListening() {
        mBluetoothAdapter.getContext().unregisterReceiver(mBtReceiver);
    }

    public String getName(){
        return mBluetoothAdapter.getName();
    }
}


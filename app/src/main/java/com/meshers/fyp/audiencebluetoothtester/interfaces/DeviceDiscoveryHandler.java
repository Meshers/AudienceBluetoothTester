package com.meshers.fyp.audiencebluetoothtester.interfaces;

import android.bluetooth.BluetoothDevice;

import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;

import java.io.Serializable;

/**
 * Created by sarahcs on 2/19/2017.
 */

public interface DeviceDiscoveryHandler {

    void handleDiscovery(LinkLayerPdu receivedPacket);
    void handleStarted();
    void handleFinished();
}

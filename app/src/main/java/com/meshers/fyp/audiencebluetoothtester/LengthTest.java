package com.meshers.fyp.audiencebluetoothtester;

import android.util.Log;

import com.meshers.fyp.audiencebluetoothtester.adapters.CustomBluetoothAdapter;
import com.meshers.fyp.audiencebluetoothtester.helpers.BTHelper;
import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;

import java.util.Arrays;

/**
 * Created by sarahcs on 2/19/2017.
 */

public class LengthTest {

    private int length;
    private BTHelper mBtHelper;
    private String description;

    public LengthTest(int length, BTHelper mBtHelper){
        this.length = length;
        this.mBtHelper = mBtHelper;

    }

    public void executeTest(){
        byte[] data = new byte[length];

        for(int i = 0; i< data.length; i++){
            data[i] = (byte) 1;
        }
        Log.e("DATA", Arrays.toString(data));

        mBtHelper.startSendingData(data);
    }

    public boolean checkStatus(){

        byte[] data = mBtHelper.getName().getBytes();

        if((data.length - 1) == length){
            description = "TEST 1: SUCCESS - Name of " + data.length + "successfully set.";
            return true;
        }
        else{
            description = "TEST 1: ERROR - Name set to " + data.length + "characters.";
            return false;
        }
    }
}

package com.meshers.fyp.audiencebluetoothtester;

import android.util.Log;

import com.meshers.fyp.audiencebluetoothtester.helpers.BTHelper;
import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;

import java.util.IllegalFormatException;

/**
 * Created by sarahcs on 2/20/2017.
 */

public class AckTest {

    private int ackByte;
    private BTHelper btHelper;
    private String description;

    public AckTest(int fromAddr, BTHelper btHelper){

        this.ackByte = fromAddr;
        this.btHelper = btHelper;
    }

    public void executeTest(LinkLayerPdu pdu){
        if(checkAckByte(pdu)){
            btHelper.stopSendingData();
        }
    }

    public boolean checkAckByte(LinkLayerPdu pdu){

        byte[] data = pdu.getData();

        if (ackByte >= data.length){
            Log.e("RESULT", String.valueOf(false));
            return false;
        }

        if(data[ackByte] == (byte) 1){
            Log.e("RESULT", String.valueOf(true));
            return true;
        }
        else{
            Log.e("RESULT1", String.valueOf(false));
            return false;
        }

    }


}

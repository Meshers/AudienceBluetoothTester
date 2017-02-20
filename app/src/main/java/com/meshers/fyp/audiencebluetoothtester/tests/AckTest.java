package com.meshers.fyp.audiencebluetoothtester.tests;

import android.util.Log;

import com.meshers.fyp.audiencebluetoothtester.helpers.BTHelper;
import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;

import java.util.BitSet;
import java.util.IllegalFormatException;

/**
 * Created by sarahcs on 2/20/2017.
 */

public class AckTest {

    private int ackBit;
    private BTHelper btHelper;
    private boolean complete = false;
    private String description;

    public AckTest(int fromAddr, BTHelper btHelper){

        this.ackBit = fromAddr;
        this.btHelper = btHelper;
    }

    public void executeTest(LinkLayerPdu pdu){
//        if(checkAckByte(pdu)){
//            complete = btHelper.stopSendingData();
//        }

        String[] bitsSet = pdu.getDataAsString().replaceAll("\\{", "").replaceAll("\\}", "").split(",");
        byte bit;
        for(String s: bitsSet){
            s = s.trim();
            if (!s.isEmpty()) {
                bit = (byte) Integer.parseInt(s);

                if(bit == ackBit){
                    complete = btHelper.stopSendingData();
                }

            } else {
                break;
            }
        }
    }

    public String getResult(){
        if(complete){
            description = "TEST 3: SUCCESS - Bluetooth Adapter Disabled.";
        }
        else{
            description = "TEST 3: ERROR - Bluetooth Adapter cannot be Disabled.";
        }

        return description;
    }

    public boolean checkAckByte(LinkLayerPdu pdu){

        BitSet data = fromByteArray(pdu.getData());

        if (ackBit >= data.length()){
            Log.e("RESULT", String.valueOf(false));
            return false;
        }

        if(data.get(ackBit)){
            Log.e("RESULT", String.valueOf(true));
            return true;
        }
        else{
            Log.e("RESULT", String.valueOf(false));
            return false;
        }

    }


    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }


}

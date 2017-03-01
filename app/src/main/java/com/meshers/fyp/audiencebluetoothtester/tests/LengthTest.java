package com.meshers.fyp.audiencebluetoothtester.tests;

import android.os.CountDownTimer;
import android.util.Log;

import com.meshers.fyp.audiencebluetoothtester.adapters.CustomBluetoothAdapter;
import com.meshers.fyp.audiencebluetoothtester.helpers.BTHelper;
import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;

import java.util.Arrays;
import java.util.Timer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sarahcs on 2/19/2017.
 */

public class LengthTest {

    private int length;
    private BTHelper mBtHelper;
    private String description;
    private boolean complete = false;

    CountDownTimer timer;

    public LengthTest(int length, BTHelper mBtHelper){
        this.length = length;
        this.mBtHelper = mBtHelper;

    }

    public void executeTest(){

        final byte[] data = new byte[length];
        for(int i = 0; i< data.length; i++){
            data[i] = (byte) 1;
        }

        final Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            private int iterator = 1;
            @Override
            public void run() {
                // task to run goes here
                mBtHelper.startSendingData(data);

                if(++iterator > 3){
                    timer.cancel();
                    complete = true;
                }
            }
        };


        long delay = 0;
        long intevalPeriod = 12000;

        // schedules the task to be run in an interval
        timer.scheduleAtFixedRate(task, delay,
                intevalPeriod);
    }

    public String getResult(){
        byte[] data = mBtHelper.getName().getBytes();

        if((data.length - 1) == length){
            description = "TEST 1: SUCCESS - Name of " + data.length + "successfully set.";
            return description;
        }
        else{
            description = "TEST 1: ERROR - Name set to " + data.length + " characters.";
            return description;
        }
    }

    public boolean checkStatus(){
        return complete;
    }
}

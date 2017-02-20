package com.meshers.fyp.audiencebluetoothtester.tests;

import android.os.CountDownTimer;
import android.widget.Toast;

import com.meshers.fyp.audiencebluetoothtester.helpers.BTHelper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sarahcs on 2/19/2017.
 */

public class ChangeDataTest {

    private BTHelper mBtHelper;
    private int noOfScans;
    private long interval;

    private boolean complete = false;
    private String description;

    public ChangeDataTest(int noOfScans, long interval, BTHelper mBtHelper){
        this.noOfScans = noOfScans;
        this.interval = interval;
        this.mBtHelper = mBtHelper;
    }

    public void executeTest(){

        final Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            private int iterator = 1;
            @Override
            public void run() {
                // task to run goes here
                mBtHelper.startSendingData(interval + ":" + iterator);

                if(++iterator > noOfScans){
                    timer.cancel();
                    description = "TEST2: SUCCESS - Data sent with an interval of " + interval + ".";
                    complete = true;
                }
            }
        };


        long delay = 0;
        long intevalPeriod = interval;

        // schedules the task to be run in an interval
        timer.scheduleAtFixedRate(task, delay,
                intevalPeriod);
    }

    public String getResults(){

        if(description != null){
            return description;
        }
        else{
            return "TEST2: ERROR - Data not sent in " + interval + "interval.";
        }

    }

    public boolean checkStatus(){
        return complete;
    }


}

package com.meshers.fyp.audiencebluetoothtester;

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

    public ChangeDataTest(int noOsScans, long interval, BTHelper mBtHelper){
        this.noOfScans = noOsScans;
        this.interval = interval;
        this.mBtHelper = mBtHelper;
    }

    public void executeTest(){

        final Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            private int iterator = 0;
            @Override
            public void run() {
                // task to run goes here
                mBtHelper.startSendingData(interval + ":" + iterator);

                if(++iterator == noOfScans){
                    timer.cancel();
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

    public boolean isComplete(){
        return complete;
    }


}

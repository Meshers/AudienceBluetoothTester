package com.meshers.fyp.audiencebluetoothtester.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.meshers.fyp.audiencebluetoothtester.MainActivity;
import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;
import com.meshers.fyp.audiencebluetoothtester.tests.AckTest;
import com.meshers.fyp.audiencebluetoothtester.tests.ChangeDataTest;
import com.meshers.fyp.audiencebluetoothtester.tests.LengthTest;

import com.meshers.fyp.audiencebluetoothtester.adapters.CustomBluetoothAdapter;
import com.meshers.fyp.audiencebluetoothtester.helpers.BTHelper;
import com.meshers.fyp.audiencebluetoothtester.interfaces.DeviceDiscoveryHandler;
import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;
import com.meshers.fyp.audiencebluetoothtester.tests.AckTest;
import com.meshers.fyp.audiencebluetoothtester.tests.ChangeDataTest;
import com.meshers.fyp.audiencebluetoothtester.tests.LengthTest;

import static com.meshers.fyp.audiencebluetoothtester.MainActivity.TestNumber.ACK_TEST;
import static com.meshers.fyp.audiencebluetoothtester.MainActivity.TestNumber.CHANGE_DATA_TEST;
import static com.meshers.fyp.audiencebluetoothtester.MainActivity.TestNumber.LENGTH_TEST;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TestManagerIntentService extends IntentService {

    BTHelper mBtHelper;
    MainActivity.TestNumber currentTest;
    LinkLayerPdu pdu = null;
    private byte fromAddr;

    //Test Objects
    private ChangeDataTest changeDataTest;
    private LengthTest lengthTest;
    private AckTest ackTest;

    public TestManagerIntentService() {
        super("TestManagerIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.getExtras()!=null) {

            fromAddr = intent.getExtras().getByte("fromAddr");
            mBtHelper = MainActivity.mBtHelper;
            currentTest = (MainActivity.TestNumber) intent.getSerializableExtra("current_test");
            pdu = (LinkLayerPdu) intent.getSerializableExtra("pdu");

            String result = conductTest(currentTest, pdu);

            Log.i("STATUS", "Test Completed");
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP);
//            broadcastIntent.setClass(this,MainActivity.class);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("completed_test", currentTest);
            broadcastIntent.putExtra("result", result);
            sendBroadcast(broadcastIntent);

        }
    }

    public String conductTest(MainActivity.TestNumber testNumber, LinkLayerPdu pdu){


        switch(testNumber){
            case LENGTH_TEST:

                lengthTest = new LengthTest(250, mBtHelper);
                lengthTest.executeTest();
                while(!lengthTest.checkStatus());

                return lengthTest.getResult();

            case CHANGE_DATA_TEST:

                String results = "";

                ChangeDataTest[] changeDataTests = new ChangeDataTest[]{
                        new ChangeDataTest(5, 15000, mBtHelper),
                        new ChangeDataTest(5, 12000, mBtHelper)
//                        new ChangeDataTest(5, 10000, mBtHelper),
//                        new ChangeDataTest(5, 90000, mBtHelper)
                };

                for(ChangeDataTest changeDataSet: changeDataTests){
                    Log.e("LOOP", "TEST 2");
                    changeDataSet.executeTest();
                    while(!changeDataSet.checkStatus());

                    results+="\n"+changeDataSet.getResults();
                }

                return results;

            case ACK_TEST:

                Log.e("ENTER", "ACK TEST");
                ackTest = new AckTest(fromAddr, mBtHelper);
                ackTest.executeTest(pdu);


                return ackTest.getResult();
            default:
                throw new IllegalArgumentException("No such test number exists");
        }

    }


}

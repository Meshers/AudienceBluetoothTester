package com.meshers.fyp.audiencebluetoothtester;

import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.meshers.fyp.audiencebluetoothtester.adapters.CustomBluetoothAdapter;
import com.meshers.fyp.audiencebluetoothtester.helpers.BTHelper;
import com.meshers.fyp.audiencebluetoothtester.interfaces.DeviceDiscoveryHandler;
import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;
import com.meshers.fyp.audiencebluetoothtester.tests.AckTest;
import com.meshers.fyp.audiencebluetoothtester.tests.ChangeDataTest;
import com.meshers.fyp.audiencebluetoothtester.tests.LengthTest;

import java.util.ArrayList;
import java.util.BitSet;

import static com.meshers.fyp.audiencebluetoothtester.MainActivity.TestNumber.ACK_TEST;
import static com.meshers.fyp.audiencebluetoothtester.MainActivity.TestNumber.CHANGE_DATA_TEST;
import static com.meshers.fyp.audiencebluetoothtester.MainActivity.TestNumber.LENGTH_TEST;
import static java.lang.Thread.sleep;

/**
 * AIM: Used to test the following:
 * 1. check max length of bluetooth beacon
 * 2. change the values of the PDU for given intervals
 * 3. receive an ACK ans stop broadcasting
 */

public class MainActivity extends AppCompatActivity {


    private BTHelper mBtHelper;
    private boolean mBtReceiverRegistered = false;

    private Button startTestButton;
    private TextView testOneResults;
    private TextView testTwoResults;
    private TextView testThreeResults;
    private EditText usnEditText;

    //Device specific data
    private byte fromAddr = (byte) 3;


    public enum TestNumber{
        NO_TEST(0),
        LENGTH_TEST(1),
        CHANGE_DATA_TEST(2),
        ACK_TEST(3);

        private int testNumber;

        TestNumber(int testNumber){
            this.testNumber = testNumber;
        }
        public int getTestNumber(){
            return this.testNumber;
        }

        public void setTestNumber(int testNumber){
            this.testNumber = testNumber;
        }
    }

    TestNumber currentTest;

    //Test Objects
    private ChangeDataTest changeDataTest;
    private LengthTest lengthTest;
    private AckTest ackTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViews();


        startTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fromAddr = generateFromAddr();
                if (fromAddr == (byte) 0) return;

               if(!mBtReceiverRegistered){

                   mBtReceiverRegistered = true;
                   Toast.makeText(MainActivity.this, "Test Started",
                           Toast.LENGTH_LONG).show();

                   startTestButton.setClickable(false);
                   startTestButton.setEnabled(false);

                   //comment: testing mode
//                   BitSet set = new BitSet(5);
//                   set.set(4);
//                   mBtHelper.startSendingData(set.toString());

                   // conduct the length test
                   Log.e("TEST 1", "starting");
                   conductTest(LENGTH_TEST, null);
                    Log.e("TEST 1", "completed");
                   //conduct the data change test
                   Log.e("TEST 2", "starting");
                   conductTest(CHANGE_DATA_TEST, null);
                   Log.e("TEST 2", "completed");


               }

            }
        });

        CustomBluetoothAdapter adapter = new CustomBluetoothAdapter(this);

        mBtHelper = new BTHelper(adapter, new DeviceDiscoveryHandler() {

            long mLastScanStarted;

            @Override
            public void handleDiscovery(LinkLayerPdu receivedPacket) {

                Log.e("PACKET", receivedPacket.getDataAsString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                    if(currentTest == TestNumber.ACK_TEST){
                        conductTest(ACK_TEST, receivedPacket);
                    }

                }
            }

            @Override
            public void handleStarted() {
                mLastScanStarted = System.currentTimeMillis();
            }

            @Override
            public void handleFinished() {

                mBtHelper.startDiscovery();
            }
        });

        mBtHelper.setFromAddress(fromAddr);
    }

    private void initializeViews() {

        startTestButton = (Button) findViewById(R.id.start_test);
        testOneResults =(TextView) findViewById(R.id.test_one_results);
        testTwoResults =(TextView) findViewById(R.id.test_two_results);
        testThreeResults =(TextView) findViewById(R.id.test_three_results);
        usnEditText = (EditText) findViewById(R.id.usn);
        currentTest = TestNumber.LENGTH_TEST;
    }

    public void conductTest(TestNumber testNumber, LinkLayerPdu pdu){


        switch(testNumber){
            case LENGTH_TEST:

                lengthTest = new LengthTest(250, mBtHelper);
                lengthTest.executeTest();
                while(!lengthTest.checkStatus()){
                    Log.e("WITHIN WHILE LOOP", "TEST 1");
                }

                testOneResults.setText(lengthTest.getResult());

                currentTest = TestNumber.CHANGE_DATA_TEST;

                break;

            case CHANGE_DATA_TEST:

                ChangeDataTest[] changeDataTests = new ChangeDataTest[]{
                        new ChangeDataTest(2, 15000, mBtHelper),
                        new ChangeDataTest(2, 12000, mBtHelper)
//                        new ChangeDataTest(5, 10000, mBtHelper),
//                        new ChangeDataTest(5, 90000, mBtHelper)
                };

                for(ChangeDataTest changeDataSet: changeDataTests){
                    Log.e("LOOP", "TEST 2");
                    changeDataSet.executeTest();
                    while(!changeDataSet.checkStatus());
                    testTwoResults.setText(testTwoResults.getText() + "\n" + changeDataSet.getResults());
                }

                currentTest = TestNumber.ACK_TEST;

                break;
            case ACK_TEST:

                Log.e("ENTER", "ACK TEST");
                ackTest = new AckTest(fromAddr, mBtHelper);
                ackTest.executeTest(pdu);
                testThreeResults.setText(ackTest.getResult());

                currentTest = TestNumber.NO_TEST;
                startTestButton.setEnabled(true);

                break;
            default:
                throw new IllegalArgumentException("No such test number exists");
        }

    }

    public byte generateFromAddr(){
        try{
            int usn = Integer.parseInt(usnEditText.getText().toString());

            return (byte) usn;
        }
        catch (NumberFormatException e){
            Toast.makeText(MainActivity.this, "Illegal Format. Please enter a valid USN.",
                    Toast.LENGTH_LONG).show();

            return (byte) 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

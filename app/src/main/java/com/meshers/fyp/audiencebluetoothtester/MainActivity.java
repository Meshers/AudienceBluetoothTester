package com.meshers.fyp.audiencebluetoothtester;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.meshers.fyp.audiencebluetoothtester.adapters.CustomBluetoothAdapter;
import com.meshers.fyp.audiencebluetoothtester.helpers.BTHelper;
import com.meshers.fyp.audiencebluetoothtester.interfaces.DeviceDiscoveryHandler;
import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;
import com.meshers.fyp.audiencebluetoothtester.services.TestManagerIntentService;
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


    public static BTHelper mBtHelper;
    private boolean mBtReceiverRegistered = false;

    private Button startTestButton;
    private TextView testOneResults;
    private TextView testTwoResults;
    private TextView testThreeResults;
    private TextView testCount;
    private EditText usnEditText;

    //Device specific data
    private byte fromAddr = (byte) 1;


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

    private ResponseReceiver receiver;

    String usnStr;

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

        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

        initializeViews();

        usnEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.i("STATUS","Enter pressed");

                    startTestButton.performClick();
                }
                return false;
            }
        });

        usnEditText.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {

                for(int i = s.length(); i > 0; i--) {

                    if(s.subSequence(i-1, i).toString().equals("\n"))
                        s.replace(i-1, i, "");
                }

                usnStr = s.toString();
            }
        });


        startTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fromAddr = generateFromAddr();
                if (fromAddr == (byte) 1) return;
                else{
                    mBtHelper.setFromAddress(fromAddr);
                }

               if(!mBtReceiverRegistered){

                   mBtReceiverRegistered = true;
                   Toast.makeText(MainActivity.this, "Test Started",
                           Toast.LENGTH_LONG).show();

                   startTestButton.setClickable(false);
                   startTestButton.setEnabled(false);

                   testCount.setText(String.valueOf(currentTest.getTestNumber()));
                   Intent test1Intent = new Intent(MainActivity.this, TestManagerIntentService.class);
                   test1Intent.putExtra("fromAddr", fromAddr);
                   test1Intent.putExtra("current_test", TestNumber.LENGTH_TEST);
                   startService(test1Intent);

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
//                         create test for intent
                        Intent test1Intent = new Intent(MainActivity.this, TestManagerIntentService.class);
                        test1Intent.putExtra("fromAddr", fromAddr);
                        test1Intent.putExtra("pdu", receivedPacket);
                        test1Intent.putExtra("current_test", TestNumber.ACK_TEST);
                        startService(test1Intent);
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
    }

    private void initializeViews() {

        startTestButton = (Button) findViewById(R.id.start_test);
        testOneResults =(TextView) findViewById(R.id.test_one_results);
        testTwoResults =(TextView) findViewById(R.id.test_two_results);
        testThreeResults =(TextView) findViewById(R.id.test_three_results);
        usnEditText = (EditText) findViewById(R.id.usn);
        testCount = (TextView) findViewById(R.id.test_count);
        currentTest = TestNumber.LENGTH_TEST;
    }


    public byte generateFromAddr(){
        try{
            int usn = Integer.parseInt(usnStr);
            Log.e("usn",String.valueOf((byte)usn));
            return (byte) usn;
        }
        catch (NumberFormatException e){
            Toast.makeText(MainActivity.this, "Illegal Format. Please enter a valid USN.",
                    Toast.LENGTH_LONG).show();

            return (byte) 1;
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

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.meshers.fyp.audienceBluetoothTester";

        @Override
        public void onReceive(Context context, Intent intent) {

           try{
               TestNumber completedTest;
               if(intent!=null && intent.getExtras()!=null){

                   completedTest = (TestNumber) intent.getSerializableExtra("completed_test");

                   switch(completedTest){
                       case LENGTH_TEST:
                           Log.i("STATUS", "Within broadcast receiver of Test 1");
                           testOneResults.setText(intent.getExtras().getString("result"));
                           currentTest = TestNumber.CHANGE_DATA_TEST;

                           testCount.setText(String.valueOf(currentTest.getTestNumber()));
                           Intent test2Intent = new Intent(MainActivity.this, TestManagerIntentService.class);
                           test2Intent.putExtra("fromAddr", fromAddr);
                           test2Intent.putExtra("current_test", TestNumber.CHANGE_DATA_TEST);
                           startService(test2Intent);

                           break;
                       case CHANGE_DATA_TEST:
                           Log.i("STATUS", "Within broadcast receiver of Test 2");
                           testTwoResults.setText(intent.getExtras().getString("result"));
                           currentTest = TestNumber.ACK_TEST;

                           testCount.setText(String.valueOf(currentTest.getTestNumber()));
                           break;
                       case ACK_TEST:
                           testThreeResults.setText(intent.getExtras().getString("result"));
                           currentTest = TestNumber.NO_TEST;

                           testCount.setText(String.valueOf("DONE"));
                           break;
                       default:
                           throw new IllegalArgumentException("no such test exists");
                   }
               }
           }
           catch(IllegalArgumentException e){
               Log.e("IAE", "Attempt to access undefined test", e);
           }
        }
    }
}

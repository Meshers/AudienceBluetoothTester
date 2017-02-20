package com.meshers.fyp.audiencebluetoothtester;

import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.meshers.fyp.audiencebluetoothtester.adapters.CustomBluetoothAdapter;
import com.meshers.fyp.audiencebluetoothtester.helpers.BTHelper;
import com.meshers.fyp.audiencebluetoothtester.interfaces.DeviceDiscoveryHandler;
import com.meshers.fyp.audiencebluetoothtester.model.LinkLayerPdu;

/**
 * AIM: Used to test the following:
 * 1. check max length of bluetooth beacon
 * 2. change the values of the PDU for given intervals
 * 3. receive an ACK ans stop broadcasting
 */

public class MainActivity extends AppCompatActivity {

    byte fromAddr = (byte) 1;
    private BTHelper mBtHelper;
    private ChangeDataTest changeDataTest;
    private LengthTest lengthTest;
    private AckTest ackTest;
    private boolean mBtReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final TextView bluetoothBeacon = (TextView) findViewById(R.id.bluetooth_beacon);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               if(!mBtReceiverRegistered){
                   mBtHelper.setFromAddress(fromAddr);
                   String name = mBtHelper.startSendingData("HelloWorld");
                   bluetoothBeacon.setText(name);


//                   changeDataTest = new ChangeDataTest(4, 12000, mBtHelper);
//                   changeDataTest.executeTest();

//                   lengthTest = new LengthTest(250, mBtHelper);
//                   lengthTest.executeTest();

                   ackTest = new AckTest(fromAddr, mBtHelper);

                   mBtReceiverRegistered = true;

                   Toast.makeText(MainActivity.this, "Test Started: ",
                           Toast.LENGTH_LONG).show();
               }
                else{
                   mBtHelper.stopSendingData();
                   mBtReceiverRegistered = false;

               }
            }
        });

        CustomBluetoothAdapter adapter = new CustomBluetoothAdapter(this);

        mBtHelper = new BTHelper(adapter, new DeviceDiscoveryHandler() {

            long mLastScanStarted;

            @Override
            public void handleDiscovery(LinkLayerPdu receivedPacket) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                    Toast.makeText(MainActivity.this, "Discovered: "+receivedPacket.getDataAsString().getBytes().length,
                            Toast.LENGTH_LONG).show();
                    ackTest.executeTest(receivedPacket);

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

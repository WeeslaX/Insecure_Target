package sg.insecure.insecuretarget;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import sg.insecure.insecuretarget.aidl.IMyService;

public class RemoteServiceActivity extends AppCompatActivity {
    private static final String TAG = "RemoteServiceActivity";
    // AIDL Remote Service Variables
    private IMyService aidlRemoteService;
    private boolean mBound = false;
    private boolean isRandomizing = false;
    // initialize handler
    public Handler randomizingHandler;
    public Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialization
        randomizingHandler = new Handler();
        setContentView(R.layout.activity_aidl_service);

        // Run Aidl Service
        Log.d(TAG, "Starting AIDL Remote Service");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                getPackageName(), // The package name of the service app
                getPackageName() + ".services.RemoteService" // The fully qualified name of the service class
        ));
        boolean isServiceRunning = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if (isServiceRunning) {
            Toast.makeText(this, "AIDL service is running", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "AIDL service is not running", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume(){
        super.onResume();

        /* Layout Control */
        //TextView
        TextView currentRemoteServiceDataLabel = findViewById(R.id.getData_value_label);
        // Button
        Button randomiseRemoteServiceDataButton = findViewById(R.id.aidl_randomise_getdata_value);
        Button autoRandomiseRemoteServiceDataStartButton = findViewById(R.id.aidl_auto_randomise_getdata_value_start);
        Button autoRandomiseRemoteServiceDataStopButton = findViewById(R.id.aidl_auto_randomise_getdata_value_stop);

        /* On-click Listeners */
        // Manual randomization
        randomiseRemoteServiceDataButton.setOnClickListener(view ->{
            setDataInRemoteService(currentRemoteServiceDataLabel);
        });

        // Automatic randomization of data every 2 seconds
        autoRandomiseRemoteServiceDataStartButton.setOnClickListener(view->{
            if(!isRandomizing){
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (isRandomizing) {
                            setDataInRemoteService(currentRemoteServiceDataLabel);
                            randomizingHandler.postDelayed(this, 2000);
                        }
                    }
                };
                randomizingHandler.postDelayed(runnable, 2000);

                // Setting up state identifiers
                isRandomizing = true;
                randomiseRemoteServiceDataButton.setEnabled(false);
                Log.d(TAG,"Started data randomization.");
                Toast.makeText(this, "Started data randomization.", Toast.LENGTH_LONG).show();
            }
            else{
                Log.d(TAG,"Already randomizing remote service data.");
                Toast.makeText(this, "Already randomizing remote service data.", Toast.LENGTH_LONG).show();
            }
        });

        // Stopping automatic randomization of data
        autoRandomiseRemoteServiceDataStopButton.setOnClickListener(view->{
            if(isRandomizing){
                // Setting up state identifiers
                isRandomizing = false;
                // remove the runnable from the handler
                randomizingHandler.removeCallbacks(runnable);
                randomiseRemoteServiceDataButton.setEnabled(true);
                Log.d(TAG,"Stopped data randomization.");
                Toast.makeText(this, "Stopped data randomization.", Toast.LENGTH_LONG).show();
            }
            else{
                Log.d(TAG,"Not currently data randomizing, select start to begin.");
                Toast.makeText(this, "Not currently data randomizing, select start to begin.", Toast.LENGTH_LONG).show();
            }

        });
    }

    /**
     * Initialization of AIDL Remote Service
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            aidlRemoteService = IMyService.Stub.asInterface(service);
            Log.e(TAG, "AIDL Remote Service started.");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "AIDL Remote Service has unexpectedly disconnected.");
            aidlRemoteService = null;
            mBound = false;
        }
    };

    /**
     * Modify data within Remote service with a random number between 0 to 1000
     * @param currentRemoteServiceDataLabel
     */
    @SuppressLint("SetTextI18n")
    private void setDataInRemoteService(TextView currentRemoteServiceDataLabel){
        if (mBound) {
            Log.d(TAG, "Attemping to get change from Remote Service...");
            try {
                Random r = new Random();
                int randomNumber = r.nextInt(1000);
                String newData = Integer.toString(randomNumber);
                Log.d(TAG, "New Data: " + newData);
                aidlRemoteService.setData(newData);
                currentRemoteServiceDataLabel.setText("Remote Service data changed: " + newData);

                // Send implicit/explicit broadcast
                Intent intent = new Intent();
                intent.setAction("sg.insecure.insecuretarget.REMOTE_DATA_UPDATED");
                intent.putExtra("newData", "updated");
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

                // Explicit Broadcast (Comment out if implicit is required)
//                Log.d(TAG,"Explicit Broadcast to " + BuildConfig.SECURE_APP_PKG
//                        + ", Broadcast Receiver Name: " + BuildConfig.SECURE_APP_RECEIVER);
//                intent.setComponent(
//                        new ComponentName(BuildConfig.SECURE_APP_PKG,BuildConfig.SECURE_APP_RECEIVER)
//                );

                sendBroadcast(intent);
                Log.d(TAG, "Broadcast sent - Remote service data changed.");

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
}

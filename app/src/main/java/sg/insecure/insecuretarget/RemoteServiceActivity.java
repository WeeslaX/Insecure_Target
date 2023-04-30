package sg.insecure.insecuretarget;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //TextView
        TextView currentRemoteServiceDataLabel = findViewById(R.id.getData_value_label);
        // Button
        Button randomiseRemoteServiceDataButton = findViewById(R.id.aidl_randomise_getdata_value);

        randomiseRemoteServiceDataButton.setOnClickListener(view ->{
            setDataInRemoteService(currentRemoteServiceDataLabel);
        });
    }

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

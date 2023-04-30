package sg.insecure.insecuretarget.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import sg.insecure.insecuretarget.aidl.IMyService;
import android.os.Process;
import android.util.Log;

public class RemoteService extends Service {

    private String mData = "Initial Data";
    private static final String TAG = "RemoteService";

    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IMyService.Stub mBinder = new IMyService.Stub() {

        // Return Pid of service app
        public int getPid(){
            return Process.myPid();
        }
        // Return app package name
        public String getPackageName(){
            return getApplicationContext().getPackageName();
        }

        @Override
        public String getData() {
            return mData;
        }

        @Override
        public void setData(String data) {
            Log.d(TAG, "New data received: " + data +" Attempting to change...");
            mData = data;
        }


    };


}

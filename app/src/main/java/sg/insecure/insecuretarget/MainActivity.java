package sg.insecure.insecuretarget;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import sg.insecure.insecuretarget.util.CreatePackageContext;


public class MainActivity extends AppCompatActivity {

    private static final int dangerousPermissionCode = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Required Permissions
        String[] neededPermission = new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        // Runtime Permission Request
        ActivityCompat.requestPermissions(this, neededPermission, dangerousPermissionCode);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* Layout Control */
        // Buttons
        Button unencryptedButton = findViewById(R.id.unencrypted_db_btn);
        Button encryptedButton = findViewById(R.id.encrypted_db_btn);
        Button aidlServiceButton = findViewById(R.id.aidl_service_btn);
        Button localSocketButton = findViewById(R.id.local_socket_btn);
        Button createPackageContextScanningBtn = findViewById(R.id.create_package_context_scanning);

        // Transition to unencrypted database activity
        unencryptedButton.setOnClickListener(view ->{
            startActivity(new Intent(this, UnencryptedDatabaseActivity.class));
        });

        // Transition to encrypted database activity
        encryptedButton.setOnClickListener(view ->{
            startActivity(new Intent(this, EncryptedDatabaseActivity.class));
        });

        // Transition to AIDL Service activity
        aidlServiceButton.setOnClickListener(view ->{
            startActivity(new Intent(this, RemoteServiceActivity.class));
        });

        localSocketButton.setOnClickListener(view->{
            startActivity(new Intent(this, LocalSocketActivity.class));
        });

        createPackageContextScanningBtn.setOnClickListener(view->{
            CreatePackageContext.scanAndLoadPackage(this, BuildConfig.TARGET_PACKAGE_PREFIX_FOR_DCL);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
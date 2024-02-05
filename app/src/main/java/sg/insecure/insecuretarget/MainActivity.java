package sg.insecure.insecuretarget;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private static final int dangerousPermissionCode = 100;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    private final String TAG = this.getClass().getSimpleName();

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

        // Initialize the executor service
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(numberOfCores);

        // Any port > 1024 not blocked by firewall
        int portNumber = 50000;

        // FYI: Let system allocate a free port, may need a means to obtain this value on client app
//        int portNumber = 0; // Let the system assign a free port
//        ServerSocket serverSocket = new ServerSocket(portNumber);
//        int assignedPort = serverSocket.getLocalPort(); // Get the assigned port number

        // Pending client connections
        executorService.execute(() -> {
            try {
                serverSocket = new ServerSocket(portNumber);
                Log.i(TAG,"Local Server Socket initialized, listening to port: " + portNumber);
                while (!Thread.currentThread().isInterrupted()) {
                    Socket client = serverSocket.accept();
                    // Handle client connection in a separate thread
                    handleClientConnection(client);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error starting server", e);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* Layout Control */
        // Buttons
        Button unencryptedButton = findViewById(R.id.unencrypted_db_btn);
        Button encryptedButton = findViewById(R.id.encrypted_db_btn);
        Button aidlServiceButton = findViewById(R.id.aidl_service_btn);

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
    }

    /**
     * Handles a client connection by reading messages sent from the client and logging them.
     * This method executes the handling of the client connection in a separate thread managed by the ExecutorService.
     * It reads lines from the client's input stream and logs each message received. Upon completion, it closes the client socket.
     *
     * @param client the {@link Socket} object representing the client connection.
     */
    private void handleClientConnection(final Socket client) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Client connected: " + client.getRemoteSocketAddress());
                    InputStream inputStream = client.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String message;
                    while ((message = reader.readLine()) != null) {
                        Log.i(TAG, "Received message: " + message);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error handling client connection", e);
                } finally {
                    try {
                        client.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing client connection", e);
                    }
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Terminating Locally hosted socket
        if (executorService != null) {
            executorService.shutdownNow();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing server socket", e);
            }
        }

    }
}
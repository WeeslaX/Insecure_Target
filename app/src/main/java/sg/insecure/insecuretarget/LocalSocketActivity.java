package sg.insecure.insecuretarget;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sg.insecure.insecuretarget.util.LoggingAdapter;

public class LocalSocketActivity extends AppCompatActivity {
    private ExecutorService executorService;
    private ServerSocket serverSocket;
    LoggingAdapter loggingAdapter;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_socket);

        // Setting up RecyclerView to print local socket logs
        RecyclerView recyclerView = findViewById(R.id.logging_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loggingAdapter = new LoggingAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(loggingAdapter);

        // Add divider line between RecyclerView items
        MaterialDividerItemDecoration dividerItemDecoration = new MaterialDividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Initialize the executor service for local socket
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
                loggingAdapter.addLogEntry("Local Server Socket initialized, listening to port: " + portNumber);
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

    /**
     * Handles a client connection by reading messages sent from the client and logging them.
     * This method executes the handling of the client connection in a separate thread managed by the ExecutorService.
     * It reads lines from the client's input stream and logs each message received. Upon completion, it closes the client socket.
     *
     * @param client the {@link Socket} object representing the client connection.
     */
    private void handleClientConnection(final Socket client) {
        executorService.execute(() -> {
            try {
                runOnUiThread(() -> {
                    loggingAdapter.addLogEntry("Client connected: " + client.getRemoteSocketAddress());
                });
                InputStream inputStream = client.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String message;
                while ((message = reader.readLine()) != null) {
                    String finalMessage = message;
                    runOnUiThread(() -> {
                        loggingAdapter.addLogEntry("Received message: " + finalMessage);
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> {
                    loggingAdapter.addLogEntry("Error handling client connection (IOException)");
                });
                Log.e(TAG, "Error handling client connection", e);
            } finally {
                try {
                    client.close();
                    runOnUiThread(() -> {
                        loggingAdapter.addLogEntry("Closed Client connection to " + client.getRemoteSocketAddress());
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        loggingAdapter.addLogEntry("Error closing client connection");
                    });
                    Log.e(TAG, "Error closing client connection", e);
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

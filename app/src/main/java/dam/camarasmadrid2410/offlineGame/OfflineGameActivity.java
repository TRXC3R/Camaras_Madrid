package dam.camarasmadrid2410.offlineGame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Network;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import dam.camarasmadrid2410.R;

public class OfflineGameActivity extends AppCompatActivity {
    private BroadcastReceiver reconnectReceiver;
    private ConnectivityManager connectivityManager;
    private int score = 0;
    private ImageButton gameButton;
    private TextView scoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_game);

        gameButton = findViewById(R.id.gameButton);
        scoreText = findViewById(R.id.scoreText);

        gameButton.setOnClickListener(v -> {
            score++;
            scoreText.setText("Puntuación: " + score);
            changeButtonColor();
        });

        reconnectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isNetworkAvailable()) {
                    finish(); // Cierra la actividad del juego
                }
            }
        };
    }

    private void changeButtonColor() {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        gameButton.setBackgroundColor(color);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registra el receptor al activarse la actividad
        registerReceiver(reconnectReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Anula el registro al pausar la actividad
        unregisterReceiver(reconnectReceiver);
    }

    // Método para verificar si hay conexión
    private boolean isNetworkAvailable() {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        }
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}


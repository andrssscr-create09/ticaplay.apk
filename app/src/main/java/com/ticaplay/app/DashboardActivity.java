package com.ticaplay.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        String username  = getIntent().getStringExtra("username");
        String password  = getIntent().getStringExtra("password");
        String serverUrl = getIntent().getStringExtra("server");

        TextView tvWelcome = findViewById(R.id.tv_welcome);
        tvWelcome.setText("Bienvenido, " + username + " 👋");

        // Botón — Ver TV en vivo
        Button btnLive = findViewById(R.id.btn_live);
        btnLive.setOnClickListener(v -> openStream(serverUrl, username, password, "live"));

        // Botón — Películas
        Button btnMovies = findViewById(R.id.btn_movies);
        btnMovies.setOnClickListener(v -> openStream(serverUrl, username, password, "movies"));

        // Botón — Series
        Button btnSeries = findViewById(R.id.btn_series);
        btnSeries.setOnClickListener(v -> openStream(serverUrl, username, password, "series"));

        // Botón — Soporte WhatsApp
        Button btnSupport = findViewById(R.id.btn_support);
        btnSupport.setOnClickListener(v -> {
            Intent wa = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/50600000000?text=Hola+TicaPlay,+necesito+soporte"));
            startActivity(wa);
        });

        // Botón — Cerrar sesión
        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("ticaplay", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void openStream(String server, String user, String pass, String type) {
        String url = server + "/get.php?username=" + Uri.encode(user)
            + "&password=" + Uri.encode(pass)
            + "&type=m3u_plus&output=ts";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "application/x-mpegurl");
        try {
            startActivity(intent);
        } catch (Exception e) {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browser);
        }
    }
}

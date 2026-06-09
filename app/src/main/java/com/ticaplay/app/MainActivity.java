package com.ticaplay.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    // ─── URLs del servidor (fallback automático) ───────────────────────
    private static final String SERVER_PRIMARY   = "http://tvpluscr.com:8080";
    private static final String SERVER_SECONDARY = "https://reborned.cc:8443";
    // ──────────────────────────────────────────────────────────────────

    private EditText etUsername, etPassword;
    private Button   btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername  = findViewById(R.id.et_username);
        etPassword  = findViewById(R.id.et_password);
        btnLogin    = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvError     = findViewById(R.id.tv_error);

        // Si ya tiene sesión guardada, entrar directo
        SharedPreferences prefs = getSharedPreferences("ticaplay", MODE_PRIVATE);
        String savedUser = prefs.getString("username", "");
        String savedPass = prefs.getString("password", "");
        if (!savedUser.isEmpty() && !savedPass.isEmpty()) {
            etUsername.setText(savedUser);
            etPassword.setText(savedPass);
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            tvError.setText("Por favor ingresá tu usuario y contraseña.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        tvError.setVisibility(View.GONE);
        setLoading(true);

        new LoginTask(username, password).execute(SERVER_PRIMARY, SERVER_SECONDARY);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Verificando..." : "Ingresar");
    }

    private void onLoginSuccess(String username, String password, String serverUrl) {
        // Guardar credenciales
        getSharedPreferences("ticaplay", MODE_PRIVATE)
            .edit()
            .putString("username", username)
            .putString("password", password)
            .putString("server",   serverUrl)
            .apply();

        setLoading(false);

        // Abrir el portal Xtream Codes en el player externo
        String playUrl = serverUrl
            + "/get.php?username=" + Uri.encode(username)
            + "&password="         + Uri.encode(password)
            + "&type=m3u_plus&output=ts";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(playUrl), "application/x-mpegurl");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Intentar abrir con app IPTV instalada, si no, navegador
        try {
            startActivity(intent);
        } catch (Exception e) {
            // Fallback: abrir panel en navegador
            Intent browser = new Intent(Intent.ACTION_VIEW,
                Uri.parse(serverUrl + "/player_api.php?username="
                    + Uri.encode(username) + "&password=" + Uri.encode(password)));
            startActivity(browser);
        }

        // Ir al dashboard interno
        Intent dashboard = new Intent(this, DashboardActivity.class);
        dashboard.putExtra("username", username);
        dashboard.putExtra("password", password);
        dashboard.putExtra("server",   serverUrl);
        startActivity(dashboard);
        finish();
    }

    private void onLoginFailed() {
        setLoading(false);
        tvError.setText("Usuario o contraseña incorrectos. Revisá tus datos o escribinos al WhatsApp.");
        tvError.setVisibility(View.VISIBLE);
    }

    // ─── AsyncTask de autenticación con fallback ─────────────────────
    private class LoginTask extends AsyncTask<String, Void, String> {
        private final String username;
        private final String password;

        LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(String... servers) {
            for (String server : servers) {
                if (server == null || server.isEmpty()) continue;
                String result = tryAuth(server);
                if (result != null) return result; // devuelve server que funcionó
            }
            return null;
        }

        private String tryAuth(String server) {
            try {
                String apiUrl = server + "/player_api.php?username="
                    + Uri.encode(username) + "&password=" + Uri.encode(password);
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                if (code == 200) {
                    // Leer respuesta para verificar auth válida
                    byte[] buffer = new byte[512];
                    int read = conn.getInputStream().read(buffer);
                    String response = read > 0 ? new String(buffer, 0, read) : "";
                    conn.disconnect();
                    // Si la respuesta contiene "user_info" es auth válida
                    if (response.contains("user_info") || response.contains("username")) {
                        return server;
                    }
                }
                conn.disconnect();
            } catch (IOException ignored) {}
            return null;
        }

        @Override
        protected void onPostExecute(String serverUrl) {
            if (serverUrl != null) {
                onLoginSuccess(username, password, serverUrl);
            } else {
                onLoginFailed();
            }
        }
    }
}

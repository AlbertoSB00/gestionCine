package com.mobilepulse.gestioncine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilepulse.gestioncine.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText campoUser;
    private EditText campoPassword;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoUser = findViewById(R.id.campoUser);
        campoPassword = findViewById(R.id.campoPassword);
        Button botonSiguiente = findViewById(R.id.botonSiguiente);
        TextView textoRegistrateAhora = findViewById(R.id.textoRegistrateAhora);
        TextView textoOlvidastePassword = findViewById(R.id.textoOlvidastePassword);

        // Al pulsar "¿Olvidaste la contraseña?".
        textoOlvidastePassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Al pulsar "Siguiente".
        botonSiguiente.setOnClickListener(v -> {
            String user = campoUser.getText().toString().trim();
            String password = campoPassword.getText().toString().trim();
            String passwordHashed = cifrarPassword(password);

            // Validamos usuario y contraseña.
            if (user.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor, rellene todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar si el usuario existe en la bd.
            ordenServer(user, passwordHashed);
        });

        // Al pulsar "Regístrate ahora".
        textoRegistrateAhora.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // Método para cifrar la contraseña usando SHA-256
    public String cifrarPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            // Convertir el hash en formato hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para enviar orden al servidor.
    private void ordenServer(String user, String passwordHashed) {
        executorService.execute(() -> {
            String response;

            try {
                Socket socket = new Socket("192.168.0.108", 12345);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos orden al servidor.
                out.println("LOGIN");

                // Enviamos credenciales al servidor.
                out.println(user);
                out.println(passwordHashed);

                // Leemos respuesta.
                response = in.readLine();

                // Cerramos el socket.
                out.close();
                in.close();
                socket.close();

            } catch (IOException e) {
                response = "ERROR";
            }

            final String result = response;
            handler.post(() -> handleServerResponse(result));
        });
    }

    // Método para manejar la respuesta del servidor.
    private void handleServerResponse(String result) {
        if ("LOGIN_SUCCESS".equals(result)) {
            // Aquí puedes abrir la actividad principal de tu aplicación
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("CORREO", campoUser.getText().toString());
            startActivity(intent);

        } else if ("LOGIN_FAILED".equals(result)) {
            Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(LoginActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
        }
    }
}
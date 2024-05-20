package com.mobilepulse.gestioncine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {

    private static final String IP = "192.168.0.108";
    private static final int PORT = 12345;

    private EditText campoName;
    private EditText campoSurname;
    private EditText campoUser;
    private EditText campoPassword;
    private EditText campoRepitePassword;
    private EditText campoFecha;
    private CheckBox campoConsentimiento;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Asignando los campos.
        campoName = findViewById(R.id.campoName);
        campoSurname = findViewById(R.id.campoSurname);
        campoUser = findViewById(R.id.campoUser);
        campoPassword = findViewById(R.id.campoPassword);
        campoRepitePassword = findViewById(R.id.campoRepitePassword);
        campoFecha = findViewById(R.id.campoFecha);
        campoConsentimiento = findViewById(R.id.campoConsentimiento);
        Button botonSiguiente = findViewById(R.id.botonSiguiente);

        // Creando el executor.
        executorService = Executors.newSingleThreadExecutor();

        // Al pulsar "Siguiente".
        botonSiguiente.setOnClickListener(v -> {
            String name = campoName.getText().toString();
            String surname = campoSurname.getText().toString();
            String email = campoUser.getText().toString();
            String password = campoPassword.getText().toString();
            String repitePassword = campoRepitePassword.getText().toString();
            String passwordHashed = cifrarPassword(password);
            String birthdate = campoFecha.getText().toString();
            boolean consentimiento = campoConsentimiento.isChecked();

            // Validamos campos.
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || repitePassword.isEmpty() || birthdate.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Por favor, rellene todos los campos.", Toast.LENGTH_SHORT).show();
                return;

            } else if (!password.equals(repitePassword)) {
                Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                return;
            } else if (!consentimiento) {
                Toast.makeText(RegisterActivity.this, "Por favor, acepte los términos y la política de privacidad.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enviamos orden al servidor.
            ordenServer(name, surname, email, passwordHashed, birthdate);
        });
    }

    // Método para enviar orden al servidor.
    private void ordenServer(String name, String surname, String email, String passwordHashed, String birthdate) {
        executorService.execute(() -> {
            String response = authenticationTask("REGISTER", name, surname, email, passwordHashed, birthdate);

            runOnUiThread(() -> {
                if (response.equals("REGISTER_SUCCESS")) {
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("CORREO", campoUser.getText().toString());
                    startActivity(intent);
                } else if (response.equals("REGISTER_FAILED")) {
                    Toast.makeText(RegisterActivity.this, "Algo ha ido mal...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Método para manejar la respuesta del servidor.
    private String authenticationTask(String... params) {
        String response;
        try {
            Socket socket = new Socket(IP, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Enviamos orden al servidor.
            out.println(params[0]);

            // Enviamos credenciales al servidor.
            out.println(params[1]);
            out.println(params[2]);
            out.println(params[3]);
            out.println(params[4]);
            out.println(params[5]);

            // Leemos respuesta.
            response = in.readLine();

            // Cerramos el socket.
            out.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response;
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

    // Metodo para cerrar el executor.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
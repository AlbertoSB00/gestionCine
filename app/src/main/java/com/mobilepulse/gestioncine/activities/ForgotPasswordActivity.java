package com.mobilepulse.gestioncine.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText campoUser;
    private LinearLayout layoutPasswords;
    private EditText campoPassword;
    private EditText campoRepitePassword;
    private Button botonSiguiente;
    private Button botonContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        campoUser = findViewById(R.id.campoUser);
        layoutPasswords = findViewById(R.id.layoutPasswords);
        layoutPasswords.setVisibility(View.INVISIBLE);
        campoPassword = findViewById(R.id.campoPassword);
        campoRepitePassword = findViewById(R.id.campoRepitePassword);
        botonSiguiente = findViewById(R.id.botonSiguiente);
        botonContinuar = findViewById(R.id.botonContinuar);

        // Al pulsar "Siguiente".
        botonSiguiente.setOnClickListener(v -> {
            String correo = campoUser.getText().toString().trim();

            if( correo.isEmpty() ) {
                Toast.makeText(this, "Por favor, introduzca su correo", Toast.LENGTH_SHORT).show();
            }

            // Verificar si el correo existe en la bd.
            ordenServer("FORGOT", correo, null);
        });

        // Al pulsar "Continuar".
        botonContinuar.setOnClickListener(v -> {
            String correo = campoUser.getText().toString().trim();
            String password = campoPassword.getText().toString().trim();
            String repitePassword = campoRepitePassword.getText().toString().trim();

            if(  password.isEmpty() || repitePassword.isEmpty() ) {
                Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
            } else if( !password.equals(repitePassword) ) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            // Encriptar la contraseña.
            String passwordHashed = cifrarPassword(password);

            ordenServer("UPDATE", correo, passwordHashed);
        });
    }

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

    private void ordenServer(String orden, String correo, String passwordHashed) {
        new AuthenticationTask().execute(orden, correo, passwordHashed);
    }

    private class AuthenticationTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String response = "";

            try{
                Socket socket = new Socket("192.168.0.108", 12345);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                if( strings[0].equals("FORGOT") ) {
                    // Enviamos orden al servidor.
                    out.println(strings[0]);

                    // Enviamos correo al servidor.
                    out.println(strings[1]);

                    // Leemos respuesta.
                    response = in.readLine();
                    
                } else if( strings[0].equals("UPDATE") ) {
                    // Enviamos orden al servidor.
                    out.println(strings[0]);

                    // Enviamos correo y contraseña al servidor.
                    out.println(strings[1]);
                    out.println(strings[2]);

                    // Leemos respuesta.
                    response = in.readLine();
                }

                // Cerramos el socket.
                out.close();
                in.close();
                socket.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            switch (result) {
                case "FORGOT_SUCCESS":
                    // Aquí puedes abrir la actividad principal de tu aplicación
                    layoutPasswords.setVisibility(View.VISIBLE);

                    botonSiguiente.setVisibility(View.INVISIBLE);
                    botonContinuar.setVisibility(View.VISIBLE);
                    break;

                case "FORGOT_FAILED":
                    Toast.makeText(ForgotPasswordActivity.this, "El correo no existe en la base de datos", Toast.LENGTH_SHORT).show();
                    break;

                case "UPDATE_SUCCESS":
                    Toast.makeText(ForgotPasswordActivity.this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    break;

                case "UPDATE_FAILED":
                    Toast.makeText(ForgotPasswordActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(ForgotPasswordActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
package com.mobilepulse.gestioncine;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LoginActivity extends AppCompatActivity {

    private ConnectionSQL connection;
    private Connection connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        connection = new ConnectionSQL();

        // Al pulsar "Regístrate ahora".
        TextView textoRegistrateAhora = findViewById(R.id.textoRegistrateAhora);
        textoRegistrateAhora.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Al pulsar "Siguiente".
        TextView botonIniciarSesion = findViewById(R.id.botonSiguiente);
        botonIniciarSesion.setOnClickListener(v -> {

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                try {
                    connect = connection.newConnection();
                    if (connect != null) {

                        EditText campoUser = findViewById(R.id.campoUser);
                        EditText campoPassword = findViewById(R.id.campoPassword);

                        String user = campoUser.getText().toString();
                        String password = campoPassword.getText().toString();
                        String hashedPassword = cifrarPassword(password);

                        // Realizar la consulta SQL para buscar al usuario por su correo electrónico y contraseña
                        String query = "SELECT * FROM usuario WHERE email = ? AND password = ?";
                        PreparedStatement preparedStatement = null;
                        ResultSet resultSet = null;
                        try {
                            preparedStatement = connect.prepareStatement(query);
                            preparedStatement.setString(1, user);
                            preparedStatement.setString(2, hashedPassword);
                            resultSet = preparedStatement.executeQuery();

                            if (resultSet.next()) {
                                runOnUiThread(() -> {

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);

                                });
                            } else {
                                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Correo electrónico o contraseña incorrectos", Toast.LENGTH_SHORT).show());
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();

                        } finally {
                            try {
                                if (resultSet != null) {
                                    resultSet.close();
                                }
                                if (preparedStatement != null) {
                                    preparedStatement.close();
                                }

                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                    } else {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error al conectar con la bd", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
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
}
package com.mobilepulse.gestioncine;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    ConnectionSQL connection;
    Connection connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Crea la conexión con la base de datos.
        connection = new ConnectionSQL();

        // Al pulsar "Siguiente".
        TextView botonIniciarSesion = findViewById(R.id.botonSiguiente);
        botonIniciarSesion.setOnClickListener(v -> {

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                try {
                    connect = connection.newConnection();
                    if (connect != null) {

                        EditText campoNombre = findViewById(R.id.campoName);
                        EditText campoApellidos = findViewById(R.id.campoSurname);
                        EditText campoCorreo = findViewById(R.id.campoUser);
                        EditText campoPassword = findViewById(R.id.campoPassword);
                        EditText campoBirthdate = findViewById(R.id.campoFecha);
                        CheckBox checkboxPolitica = findViewById(R.id.campoConsentimiento);

                        String name = campoNombre.getText().toString();
                        String surname = campoApellidos.getText().toString();
                        String email = campoCorreo.getText().toString();
                        String password = campoPassword.getText().toString();
                        String birthdate = campoBirthdate.getText().toString();
                        boolean policityCheck = checkboxPolitica.isChecked();

                        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() ||  birthdate.isEmpty() || !policityCheck) {
                            runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Por favor, complete todos los campos y marque la política de privacidad", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        String hashedPassword = cifrarPassword(password);

                        // Realizar la consulta SQL para insertar un nuevo usuario en la tabla
                        String query = "INSERT INTO usuario (name, surname, email, password, birthdate) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement preparedStatement = connect.prepareStatement(query)) {
                            preparedStatement.setString(1, name);
                            preparedStatement.setString(2, surname);
                            preparedStatement.setString(3, email);
                            preparedStatement.setString(4, hashedPassword);
                            preparedStatement.setString(5, birthdate);

                            int filasAfectadas = preparedStatement.executeUpdate();
                            if (filasAfectadas > 0) {
                                runOnUiThread(() -> {
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                });
                            } else {
                                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show());
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    } else {
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Error al conectar con la bd", Toast.LENGTH_SHORT).show());
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
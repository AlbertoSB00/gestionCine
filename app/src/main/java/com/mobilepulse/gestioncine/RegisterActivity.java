package com.mobilepulse.gestioncine;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText campoName, campoSurname, campoUser, campoPassword, campoFecha;
    private CheckBox campoConsentimiento;

    // SQL Connection
    private ConnectionSQL connectionSQL;
    private Connection connection;
    private ResultSet resultSet;
    private String name, surname, user, password, fecha;

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
        connectionSQL = new ConnectionSQL();
        connect();

        // Inicializa la vista.
        inicializaVista();
    }

    private void inicializaVista() {
        campoName = findViewById(R.id.campoName);
        campoSurname = findViewById(R.id.campoSurname);
        campoUser = findViewById(R.id.campoUser);
        campoPassword = findViewById(R.id.campoPassword);
        campoFecha = findViewById(R.id.campoFecha);
        Button botonSiguiente = findViewById(R.id.botonSiguiente);
        campoConsentimiento = findViewById(R.id.campoConsentimiento);

        botonSiguiente.setOnClickListener(v -> {
            if( campoName.getText().toString().isEmpty() || campoSurname.getText().toString().isEmpty() || campoUser.getText().toString().isEmpty() || campoPassword.getText().toString().isEmpty() || campoFecha.getText().toString().isEmpty() ) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            } else if (!campoConsentimiento.isChecked()) {
                Toast.makeText(this, "Debes leer y aceptar los términos y la politica de privacidad.", Toast.LENGTH_SHORT).show();
            } else{
                name = campoName.getText().toString();
                surname = campoSurname.getText().toString();
                user = campoUser.getText().toString();
                password = campoPassword.getText().toString();
                fecha = campoFecha.getText().toString();

                // Provisional para ver que funciona.
                Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });
    }

    public void connect() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                connection = connectionSQL.newConnection();
                runOnUiThread(() -> {
                    if (connection != null) {
                        Toast.makeText(RegisterActivity.this, "Conectado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error al conectar", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
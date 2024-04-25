package com.mobilepulse.gestioncine;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/*
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
*/

public class LoginActivity extends AppCompatActivity {

    private EditText campoUser, campoPassword;

    /*
    private ConnectionSQL connectionSQL;
    private Connection connection;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*
        // Crea la conexión con la base de datos.
        connectionSQL = new ConnectionSQL();
        connect();
        */

        inicializaVista();

        // Al pulsar registrar, nos lleva a la vista de registro.
        TextView textoRegistrateAhora = findViewById(R.id.textoRegistrateAhora);
        textoRegistrateAhora.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    // Inicia la vista para comprobar credenciales.
    private void inicializaVista() {
        campoUser = findViewById(R.id.campoUser);
        campoPassword = findViewById(R.id.campoPassword);
        Button botonSiguiente = findViewById(R.id.botonSiguiente);

        botonSiguiente.setOnClickListener(v -> {
            if( campoUser.getText().toString().isEmpty() || campoPassword.getText().toString().isEmpty() ) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();

            } else if( campoUser.getText().toString().equals("usuario") && campoPassword.getText().toString().equals("usuario") ) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }else{
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    public void connect() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                connection = connectionSQL.newConnection();
                runOnUiThread(() -> {
                    if (connection != null) {
                        Toast.makeText(LoginActivity.this, "Conectado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error al conectar", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    */
}
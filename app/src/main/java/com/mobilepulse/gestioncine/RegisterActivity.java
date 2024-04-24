package com.mobilepulse.gestioncine;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private EditText campoName, campoUser, campoPassword, campoFecha;
    private Button botonSiguiente;
    private String name, user, password, fecha;

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
        
        inicializaVista();
    }

    private void inicializaVista() {
        campoName = findViewById(R.id.campoName);
        campoUser = findViewById(R.id.campoUser);
        campoPassword = findViewById(R.id.campoPassword);
        campoFecha = findViewById(R.id.campoFecha);
        botonSiguiente = findViewById(R.id.botonSiguiente);

        botonSiguiente.setOnClickListener(v -> {
            if( campoName.getText().toString().isEmpty() || campoUser.getText().toString().isEmpty() || campoPassword.getText().toString().isEmpty() || campoFecha.getText().toString().isEmpty() ) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            }else{
                name = campoName.getText().toString();
                user = campoUser.getText().toString();
                password = campoPassword.getText().toString();
                fecha = campoFecha.getText().toString();

                // Se inserta en la bd...

                // Provisional para ver que funciona.
                Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
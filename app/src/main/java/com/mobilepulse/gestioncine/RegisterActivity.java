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

public class RegisterActivity extends AppCompatActivity {

    private EditText campoName, campoSurname, campoUser, campoPassword, campoFecha;
    private String name, surname, user, password, fecha;
    private CheckBox campoConsentimiento;

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

                // Se inserta en la bd...

                // Provisional para ver que funciona.
                Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });
    }
}
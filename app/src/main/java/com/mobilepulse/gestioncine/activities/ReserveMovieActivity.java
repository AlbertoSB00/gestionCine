package com.mobilepulse.gestioncine.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilepulse.gestioncine.R;

public class ReserveMovieActivity extends AppCompatActivity {

    private Spinner spinnerSala;
    private Spinner spinnerHorario;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_movie);


        // Obtener el titulo de la película.
        String titulo = getIntent().getStringExtra("titulo");
        String correo = getIntent().getStringExtra("correo");
        Toast.makeText(this, correo, Toast.LENGTH_SHORT).show();
        TextView textView = findViewById(R.id.titulo);
        textView.setText(titulo);

        // Al pulsar el botón "Proceder con el pago".
        Button buttonConfirmar = findViewById(R.id.buttonConfirmar);
        buttonConfirmar.setOnClickListener(v -> {

            // Antes de continuar con el pago, hacemos las gestiones para crear la reserva en la bd.

        });
    }

    public void reservarButaca(View view) {
        Button butaca = (Button) view;
        Drawable drawable = butaca.getBackground();

        // Verifica el estado actual de la butaca
        if (drawable.getConstantState().equals(getResources().getDrawable(R.drawable.butaca_libre).getConstantState())) {
            // Si está libre, cambia a reservada
            butaca.setBackgroundResource(R.drawable.butaca_ocupada);
        } else {
            // Si está reservada, cambia a libre
            butaca.setBackgroundResource(R.drawable.butaca_libre);
        }
    }
}
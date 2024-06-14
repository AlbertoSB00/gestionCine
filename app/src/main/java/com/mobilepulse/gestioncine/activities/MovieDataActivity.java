package com.mobilepulse.gestioncine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.mobilepulse.gestioncine.R;
import com.mobilepulse.gestioncine.classes.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad para mostrar los datos de una película específica.
 */
public class MovieDataActivity extends AppCompatActivity {

    private static final String IP = Configuration.IP;
    private static final int PORT = Configuration.PORT;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Llamado cuando la actividad es creada por primera vez.
     *
     * @param savedInstanceState Si la actividad está siendo re-inicializada después de haber sido previamente terminada, este Bundle contiene los datos que más recientemente suministró en onSaveInstanceState(Bundle). De lo contrario, está nulo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_data);

        // Obtener la URL de la imagen de los extras.
        String imageUrl = getIntent().getStringExtra("image_url");

        ordenServer(imageUrl);

        // Inicializar la vista de la imagen.
        ImageView imageView = findViewById(R.id.pelicula);

        // Cargar la imagen.
        Glide.with(this)
                .load(imageUrl)
                .into(imageView);
    }

    /**
     * Método para enviar una solicitud al servidor.
     *
     * @param imageUrl La URL de la imagen de la película.
     */
    private void ordenServer(String imageUrl) {
        executorService.execute(() -> {
            String response;

            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos orden al servidor.
                out.println("MOVIE_DATA");

                // Enviamos credenciales al servidor.
                out.println(imageUrl);

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

    /**
     * Método para manejar la respuesta del servidor.
     *
     * @param result La respuesta del servidor.
     */
    private void handleServerResponse(String result) {
        if (result.equals("ERROR")) {
            Toast.makeText(this, "Error al obtener los datos de la película", Toast.LENGTH_SHORT).show();

        } else if (result.equals("MOVIE_NOT_FOUND")) {
            Toast.makeText(this, "La película no se encuentra", Toast.LENGTH_SHORT).show();
        } else {
            String[] movieData = result.split(";");

            // Actualizar los elementos de la interfaz de usuario con los datos de la película
            TextView tituloTextView = findViewById(R.id.titulo);
            TextView descripcionTextView = findViewById(R.id.descripcion);
            TextView generoTextView = findViewById(R.id.genero);
            TextView directorTextView = findViewById(R.id.director);
            TextView duracionTextView = findViewById(R.id.duracion);
            TextView clasificacionTextView = findViewById(R.id.clasificacion);

            tituloTextView.setText(movieData[0]);
            descripcionTextView.setText(movieData[1]);
            generoTextView.setText(movieData[2]);
            directorTextView.setText(movieData[3]);
            duracionTextView.setText(movieData[4]);
            clasificacionTextView.setText(movieData[5]);

            // Al pulsar el boton reservar.
            Button botonReservar = findViewById(R.id.botonReservar);
            botonReservar.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReserveMovieActivity.class);
                intent.putExtra("titulo", movieData[0]);
                intent.putExtra("correo", getIntent().getStringExtra("correo"));
                startActivity(intent);
            });
        }
    }

}

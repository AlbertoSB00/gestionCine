package com.mobilepulse.gestioncine.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilepulse.gestioncine.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReserveMovieActivity extends AppCompatActivity {

    private static final String IP = "192.168.0.108";
    private static final int PORT = 12345;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Spinner spinnerSala;
    private Spinner spinnerHorario;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_movie);

        spinnerSala = findViewById(R.id.spinnerSala);
        spinnerHorario = findViewById(R.id.spinnerHorario);

        // Obtener el título de la película.
        String titulo = getIntent().getStringExtra("titulo");
        String correo = getIntent().getStringExtra("correo");
        TextView textView = findViewById(R.id.titulo);
        textView.setText(titulo);

        // Poblar los Spinners
        loadSalaData();
        loadHorarioData();

        // Al pulsar el botón "Proceder con el pago".
        Button buttonConfirmar = findViewById(R.id.buttonConfirmar);
        buttonConfirmar.setOnClickListener(v -> {
            // Obtener el estado de la reserva
            String estadoReserva = "Confirmada";

            // Obtener el ID de usuario y de película de manera asíncrona
            CompletableFuture<Integer>[] futures = new CompletableFuture[2];
            futures[0] = obtenerIdUsuario(correo);
            futures[1] = obtenerIdPelicula(titulo);

            // Cuando se completen ambos futures, realizar la reserva
            CompletableFuture.allOf(futures).thenAcceptAsync(result -> {
                try {
                    reservar(futures[0].get(), futures[1].get(), spinnerSala.getSelectedItemId(), spinnerHorario.getSelectedItemId(), estadoReserva, contarButacasReservadas());
                } catch (Exception e) {
                    mostrarMensaje("Error al realizar la reserva: " + e.getMessage());
                }
            });
        });
    }

    private void loadSalaData() {
        CompletableFuture.supplyAsync(() -> {
            List<String> salas = new ArrayList<>();
            try (Socket socket = new Socket(IP, PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println("GET_SALAS");
                String response;
                while ((response = in.readLine()) != null) {
                    salas.add(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return salas;
        }, executorService).thenAcceptAsync(salas -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, salas);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSala.setAdapter(adapter);
        }, handler::post);
    }

    private void loadHorarioData() {
        CompletableFuture.supplyAsync(() -> {
            List<String> horarios = new ArrayList<>();
            try (Socket socket = new Socket(IP, PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println("GET_HORARIOS");
                String response;
                while ((response = in.readLine()) != null) {
                    horarios.add(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return horarios;
        }, executorService).thenAcceptAsync(horarios -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, horarios);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHorario.setAdapter(adapter);
        }, handler::post);
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

    private int contarButacasReservadas() {
        int count = 0;
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                Drawable background = button.getBackground();
                Drawable reservedDrawable = getResources().getDrawable(R.drawable.butaca_ocupada);
                if (background.getConstantState().equals(reservedDrawable.getConstantState())) {
                    count++;
                }
            }
        }
        return count;
    }

    private CompletableFuture<Integer> obtenerIdUsuario(String correo) {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket socket = new Socket(IP, PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Enviamos orden al servidor.
                out.println("GET_USER_ID");

                // Enviamos el correo.
                out.println(correo);

                // Leemos respuesta.
                String response = in.readLine();
                return Integer.parseInt(response);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error al obtener el ID de usuario.");
            }
        }, executorService);
    }

    private CompletableFuture<Integer> obtenerIdPelicula(String titulo) {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket socket = new Socket(IP, PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Enviamos orden al servidor.
                out.println("GET_MOVIE_ID");

                // Enviamos el titulo.
                out.println(titulo);

                // Leemos respuesta.
                String response = in.readLine();
                return Integer.parseInt(response);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error al obtener el ID de película.");
            }
        }, executorService);
    }

    private void reservar(int idUsuario, int idPelicula, Object idSala, Object idProyeccion, String estadoReserva, int butacasReservadas) {
        executorService.execute(() -> {
            String response = authenticationTask(idUsuario, idPelicula, idSala, idProyeccion, estadoReserva, butacasReservadas);

            runOnUiThread(() -> {
                switch (response) {
                    case "INSERT_MOVIE_SUCCESS":
                        Toast.makeText(this, "Reserva realizada con éxito", Toast.LENGTH_SHORT).show();
                        break;

                    case "INSERT_MOVIE_FAILED":
                        Toast.makeText(this, "Error al realizar la reserva", Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        });
    }

    private String authenticationTask(int idUsuario, int idPelicula, Object idSala, Object idProyeccion, String estadoReserva, int butacasReservadas) {
        String response = "";
        try {
            Socket socket = new Socket(IP, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Enviamos orden al servidor.
            out.println("INSERT_RESERVE");

            // Enviamos datos al servidor.
            out.println(idUsuario);
            out.println(idPelicula);
            out.println(idSala);
            out.println(idProyeccion);
            out.println(estadoReserva);
            out.println(butacasReservadas);

            // Leemos la respuesta.
            response = in.readLine();

            // Cerramos el socket.
            out.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private void mostrarMensaje(String mensaje) {
        handler.post(() -> Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show());
    }
}
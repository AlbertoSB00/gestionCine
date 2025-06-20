package com.mobilepulse.gestioncine.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilepulse.gestioncine.R;
import com.mobilepulse.gestioncine.classes.Configuration;

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

/**
 * Actividad para reservar una película en un cine.
 */
public class ReserveMovieActivity extends AppCompatActivity {

    private static final String IP = Configuration.IP;
    private static final int PORT = Configuration.PORT;

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

        String titulo = getIntent().getStringExtra("titulo");
        String correo = getIntent().getStringExtra("correo");
        TextView textView = findViewById(R.id.titulo);
        textView.setText(titulo);

        // Poblar el spinner de salas y esperar a que el usuario seleccione una sala antes de cargar los horarios
        loadSalaData(titulo);
        spinnerSala.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String numeroSala = spinnerSala.getSelectedItem().toString();
                loadHorarioData(numeroSala, titulo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        Button buttonConfirmar = findViewById(R.id.buttonConfirmar);
        buttonConfirmar.setOnClickListener(v -> {
            String estadoReserva = "Confirmada";

            CompletableFuture<Integer>[] futures = new CompletableFuture[2];
            futures[0] = obtenerIdUsuario(correo);
            futures[1] = obtenerIdPelicula(titulo);

            CompletableFuture.allOf(futures).thenAcceptAsync(result -> {
                try {
                    // Obtén el valor seleccionado en lugar de la posición
                    String numeroSala = spinnerSala.getSelectedItem().toString();
                    String horario = spinnerHorario.getSelectedItem().toString();
                    reservar(futures[0].get(), futures[1].get(), numeroSala, horario, estadoReserva, contarButacasReservadas());
                } catch (Exception e) {
                    mostrarMensaje("Error al realizar la reserva: " + e.getMessage());
                }
            });
        });

    }

    /**
     * Carga los datos de las salas disponibles para la película seleccionada.
     *
     * @param tituloPelicula El título de la película.
     */
    private void loadSalaData(String tituloPelicula) {
        CompletableFuture.supplyAsync(() -> {
            List<String> salas = new ArrayList<>();
            try (Socket socket = new Socket(IP, PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println("GET_SALAS_FOR_MOVIE");
                out.println(tituloPelicula); // Envía el título de la película al servidor
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

    /**
     * Carga los datos de los horarios disponibles para la sala y la película seleccionadas.
     *
     * @param numeroSala     El número de la sala seleccionada.
     * @param tituloPelicula El título de la película.
     */
    private void loadHorarioData(String numeroSala, String tituloPelicula) {
        CompletableFuture.supplyAsync(() -> {
            List<String> horarios = new ArrayList<>();
            try (Socket socket = new Socket(IP, PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println("GET_HORARIOS_FOR_MOVIE");
                out.println(numeroSala); // Envía el número de sala al servidor
                out.println(tituloPelicula); // Envía el título de la película al servidor

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

    /**
     * Maneja la acción de reservar una butaca.
     *
     * @param view La vista del botón de la butaca.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
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

    /**
     * Cuenta el número de butacas reservadas.
     *
     * @return El número de butacas reservadas.
     */
    private int contarButacasReservadas() {
        int count = 0;
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                Drawable background = button.getBackground();
                @SuppressLint("UseCompatLoadingForDrawables") Drawable reservedDrawable = getResources().getDrawable(R.drawable.butaca_ocupada);
                if (background.getConstantState().equals(reservedDrawable.getConstantState())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Obtiene el ID del usuario basado en su correo electrónico.
     *
     * @param correo El correo electrónico del usuario.
     * @return Un CompletableFuture que devuelve el ID del usuario.
     */
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

    /**
     * Obtiene el ID de la película basado en su título.
     *
     * @param titulo El título de la película.
     * @return Un CompletableFuture que devuelve el ID de la película.
     */
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

    /**
     * Realiza la reserva de la película.
     *
     * @param idUsuario         El ID del usuario.
     * @param idPelicula        El ID de la película.
     * @param sala              El número de la sala.
     * @param hora              El horario de la película.
     * @param estadoReserva     El estado de la reserva.
     * @param butacasReservadas El número de butacas reservadas.
     */
    private void reservar(int idUsuario, int idPelicula, String sala, String hora, String estadoReserva, int butacasReservadas) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("id_usuario", idUsuario);
        intent.putExtra("id_pelicula", idPelicula);
        intent.putExtra("sala", sala);
        intent.putExtra("hora", hora);
        intent.putExtra("estado_reserva", estadoReserva);
        intent.putExtra("butacas_reservadas", butacasReservadas);
        intent.putExtra("total_pagar", calcularTotal(butacasReservadas));
        startActivity(intent);
    }

    /**
     * Calcula el total a pagar por la reserva.
     *
     * @param butacasReservadas El número de butacas reservadas.
     * @return El total a pagar.
     */
    private double calcularTotal(int butacasReservadas) {
        double precioPorButaca = 7.0;
        return butacasReservadas * precioPorButaca;
    }

    /**
     * Muestra un mensaje en la interfaz de usuario.
     *
     * @param mensaje El mensaje a mostrar.
     */
    private void mostrarMensaje(String mensaje) {
        handler.post(() -> Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show());
    }
}
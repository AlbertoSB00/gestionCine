package com.mobilepulse.gestioncine.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
 * Fragmento para mostrar las reservas realizadas por el usuario.
 */
public class ReserveFragment extends Fragment {

    private static final String IP = Configuration.IP;
    private static final int PORT = Configuration.PORT;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Constructor público de la clase ReserveFragment.
     */
    public ReserveFragment() {

    }

    /**
     * Método llamado cuando se crea el fragmento.
     *
     * @param savedInstanceState El Bundle que contiene el estado anteriormente guardado del fragmento.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Método llamado para crear la vista del fragmento.
     *
     * @param inflater           El LayoutInflater objeto que se puede utilizar para inflar cualquier vista en
     *                           el fragmento.
     * @param container          Si no es nulo, este es el ViewGroup al que se debe adjuntar la vista de fragmento.
     * @param savedInstanceState Si no es nulo, este fragmento es una reanudación de un estado guardado anteriormente.
     * @return La View para el fragmento, simplemente inflada según el archivo de diseño definido en XML.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reserve, container, false);
    }

    /**
     * Método llamado después de que la vista del fragmento ha sido creada.
     *
     * @param view               La vista raíz del fragmento.
     * @param savedInstanceState Si no es nulo, este fragmento es una reanudación de un estado guardado anteriormente.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readReservesFromDB();
    }

    /**
     * Método para leer las reservas de la base de datos del servidor.
     */
    private void readReservesFromDB() {
        executorService.execute(() -> {
            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviar orden al servidor
                out.println("GET_RESERVE");

                // Enviar el correo
                out.println(getArguments().getString("CORREO"));

                // Leer todas las líneas de respuesta
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    responseBuilder.append(line).append("\n");
                }
                String response = responseBuilder.toString().trim();

                // Cerrar recursos
                out.close();
                in.close();
                socket.close();

                // Manejar la respuesta
                handler.post(() -> handleServerResponse(response));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Método para manejar la respuesta del servidor y mostrar las reservas en una tabla.
     *
     * @param response La respuesta del servidor que contiene las reservas.
     */
    @SuppressLint("SetTextI18n")
    private void handleServerResponse(String response) {
        TableLayout tableLayout = requireView().findViewById(R.id.tableLayout);

        // Limpiar la tabla antes de agregar nuevos datos
        tableLayout.removeAllViews();

        // Verificar si la respuesta no está vacía
        if (response != null && !response.isEmpty()) {
            // Dividir la cadena de respuesta en líneas
            String[] lines = response.split("\n");
            for (String line : lines) {
                // Dividir cada línea en campos utilizando el carácter "|" como delimitador
                String[] fields = line.split("\\|");

                // Crear una nueva fila para la tabla
                TableRow row = new TableRow(requireContext());

                // Crear celdas para cada campo y agregarlas a la fila
                for (String field : fields) {
                    TextView textView = new TextView(requireContext());
                    textView.setText(field);
                    textView.setPadding(8, 8, 8, 8);
                    row.addView(textView);
                }

                // Agregar la fila a la tabla
                tableLayout.addView(row);
            }
        } else {
            // Si la respuesta está vacía o nula, mostrar un mensaje indicando que no hay datos
            TableRow row = new TableRow(requireContext());
            TextView textView = new TextView(requireContext());
            textView.setText("No hay reservas disponibles");
            textView.setPadding(8, 8, 8, 8);
            row.addView(textView);
            tableLayout.addView(row);
        }
    }
}
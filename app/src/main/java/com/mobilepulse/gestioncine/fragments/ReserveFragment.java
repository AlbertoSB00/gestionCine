package com.mobilepulse.gestioncine.fragments;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReserveFragment extends Fragment {

    private static final String IP = "192.168.0.108";
    private static final int PORT = 12345;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    public ReserveFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String correo = getArguments().getString("CORREO");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reserve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readReservesFromDB();
    }

    private void readReservesFromDB() {
        executorService.execute(() ->{
            try{
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos orden al servidor.
                out.println("GET_RESERVE");

                // Enviamos el correo.
                out.println(getArguments().getString("CORREO"));

                // Leemos la respuesta.
                String response = in.readLine();

                // Cerramos recursos
                out.close();
                in.close();
                socket.close();

                // Manejamos la respuesta
                final String result = response;
                handler.post(() -> handleServerResponse(result));

            }catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    private void handleServerResponse(String response) {
        TableLayout tableLayout = requireView().findViewById(R.id.tableLayout);
        tableLayout.removeAllViews(); // Limpiar la tabla antes de agregar nuevos datos

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
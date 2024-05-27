package com.mobilepulse.gestioncine.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobilepulse.gestioncine.R;
import com.mobilepulse.gestioncine.adapters.MovieAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieFragment extends Fragment {

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String IP = "192.168.0.108";
    private static final int PORT = 12345;

    private RecyclerView mayor18;
    private RecyclerView menor18;
    private RecyclerView kids;

    public MovieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mayor18 = view.findViewById(R.id.mayor18);
        menor18 = view.findViewById(R.id.menor18);
        kids = view.findViewById(R.id.kids);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // Configurar el RecyclerView y el LayoutManager
        mayor18.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        menor18.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        kids.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Listas para almacenar las URLs de las imágenes
        List<String> movieUrlsMayor18 = new ArrayList<>();
        List<String> movieUrlsMenor18 = new ArrayList<>();
        List<String> movieUrlsKids = new ArrayList<>();

        // Lógica para obtener las URLs +18 del servidor
        executorService.execute(() -> {
            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos orden al servidor.
                out.println("GET_MORE_18_IMAGES_PATH");

                // Leemos respuesta y almacenamos las URLs en la lista correspondiente
                String imagePath;
                while ((imagePath = in.readLine()) != null) {
                    movieUrlsMayor18.add(imagePath);
                }

                // Cerramos el socket.
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Actualizar el RecyclerView en el hilo principal
            handler.post(() -> {
                // Crear y configurar el adaptador
                MovieAdapter movieAdapter = new MovieAdapter(movieUrlsMayor18);
                mayor18.setAdapter(movieAdapter);
            });
        });

        // Lógica para obtener las URLs -18 del servidor
        executorService.execute(() -> {
            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos orden al servidor.
                out.println("GET_MINUS_18_IMAGES_PATH");

                // Leemos respuesta y almacenamos las URLs en la lista correspondiente
                String imagePath;
                while ((imagePath = in.readLine()) != null) {
                    movieUrlsMenor18.add(imagePath);
                }

                // Cerramos el socket.
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Actualizar el RecyclerView en el hilo principal
            handler.post(() -> {
                // Crear y configurar el adaptador
                MovieAdapter movieAdapter = new MovieAdapter(movieUrlsMenor18);
                menor18.setAdapter(movieAdapter);
            });
        });

        // Lógica para obtener las URLs para niños del servidor.
        executorService.execute(() -> {
            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos orden al servidor.
                out.println("GET_KIDS_IMAGES_PATH");

                // Leemos respuesta y almacenamos las URLs en la lista correspondiente
                String imagePath;
                while ((imagePath = in.readLine()) != null) {
                    movieUrlsKids.add(imagePath);
                }

                // Cerramos el socket.
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Actualizar el RecyclerView en el hilo principal
            handler.post(() -> {
                // Crear y configurar el adaptador
                MovieAdapter movieAdapter = new MovieAdapter(movieUrlsKids);
                kids.setAdapter(movieAdapter);
            });
        });
    }
}

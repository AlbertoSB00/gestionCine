package com.mobilepulse.gestioncine.fragments;

import android.content.Intent;
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
import com.mobilepulse.gestioncine.activities.MovieDataActivity;
import com.mobilepulse.gestioncine.adapters.MovieAdapter;
import com.mobilepulse.gestioncine.interfaces.OnItemClickListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieFragment extends Fragment implements OnItemClickListener {

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String IP = "192.168.0.108";
    private static final int PORT = 12345;

    private RecyclerView mayor18;
    private RecyclerView menor18;
    private RecyclerView kids;

    private List<String> movieUrlsMayor18;
    private List<String> movieUrlsMenor18;
    private List<String> movieUrlsKids;

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

        // Inicializa las listas
        movieUrlsMayor18 = new ArrayList<>();
        movieUrlsMenor18 = new ArrayList<>();
        movieUrlsKids = new ArrayList<>();

        // Lógica para obtener las URLs +18 del servidor
        fetchMovieUrls("GET_MORE_18_IMAGES_PATH", mayor18);

        // Lógica para obtener las URLs -18 del servidor
        fetchMovieUrls("GET_MINUS_18_IMAGES_PATH", menor18);

        // Lógica para obtener las URLs para niños del servidor
        fetchMovieUrls("GET_KIDS_IMAGES_PATH", kids);
    }

    // Elimina el segundo parámetro movieUrls y agrégalo a la lista correspondiente según la categoría
    private void fetchMovieUrls(String command, RecyclerView recyclerView) {
        executorService.execute(() -> {
            List<String> movieUrls = new ArrayList<>(); // Lista temporal para almacenar las URLs de esta categoría

            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos orden al servidor.
                out.println(command);

                // Leemos respuesta y almacenamos las URLs en la lista correspondiente
                String imagePath;
                while ((imagePath = in.readLine()) != null) {
                    movieUrls.add(imagePath); // Agrega la URL a la lista temporal
                }

                // Agrega la lista temporal a la lista correcta según la categoría
                if (recyclerView == mayor18) {
                    movieUrlsMayor18.addAll(movieUrls);
                } else if (recyclerView == menor18) {
                    movieUrlsMenor18.addAll(movieUrls);
                } else if (recyclerView == kids) {
                    movieUrlsKids.addAll(movieUrls);
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
                MovieAdapter movieAdapter = new MovieAdapter(movieUrls, this);
                recyclerView.setAdapter(movieAdapter);
            });
        });
    }

    @Override
    public void onItemClick(int position) {
        // Verificar de cuál lista es la posición
        String movieUrl = "";
        if (mayor18.getAdapter() != null && mayor18.getAdapter().getItemCount() > position) {
            movieUrl = movieUrlsMayor18.get(position);
        } else if (menor18.getAdapter() != null && menor18.getAdapter().getItemCount() > position) {
            movieUrl = movieUrlsMenor18.get(position);
        } else if (kids.getAdapter() != null && kids.getAdapter().getItemCount() > position) {
            movieUrl = movieUrlsKids.get(position);
        }

        // Iniciar MovieDataActivity con los datos
        Intent intent = new Intent(getActivity(), MovieDataActivity.class);
        intent.putExtra("image_url", movieUrl);
        if (getArguments() != null && getArguments().getString("CORREO") != null) {
            intent.putExtra("correo", getArguments().getString("CORREO"));
        }
        startActivity(intent);
    }
}
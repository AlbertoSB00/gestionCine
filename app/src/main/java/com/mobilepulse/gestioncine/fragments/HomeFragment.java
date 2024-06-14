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
import androidx.viewpager.widget.ViewPager;

import com.mobilepulse.gestioncine.R;
import com.mobilepulse.gestioncine.activities.MovieDataActivity;
import com.mobilepulse.gestioncine.adapters.ImagePagerAdapter;
import com.mobilepulse.gestioncine.classes.Configuration;
import com.mobilepulse.gestioncine.interfaces.OnItemClickListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragmento para mostrar la pantalla de inicio con una vista de carrusel de imágenes de películas.
 */
public class HomeFragment extends Fragment implements OnItemClickListener {

    private static final String IP = Configuration.IP;
    private static final int PORT = Configuration.PORT;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ViewPager viewPager;

    private List<String> imageURLs;

    /**
     * Constructor público de la clase HomeFragment.
     */
    public HomeFragment() {
    }

    /**
     * Método para crear la vista del fragmento.
     *
     * @param inflater           El inflador de la vista.
     * @param container          El contenedor de la vista.
     * @param savedInstanceState Si no es nulo, este fragmento es una reanudación de un estado guardado anteriormente.
     * @return La vista del fragmento.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Llamado cuando la vista creada por {@link #onCreateView} ha sido preparada para mostrar al usuario.
     *
     * @param view               La vista raíz del fragmento.
     * @param savedInstanceState Si no es nulo, este fragmento es una reanudación de un estado guardado anteriormente.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.viewPager);
        fetchImagePathsFromServer();
    }

    /**
     * Método para obtener las URL de las imágenes de las películas desde el servidor.
     */
    private void fetchImagePathsFromServer() {
        executorService.execute(() -> {
            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos orden al servidor.
                out.println("GET_ALL_IMAGE_PATHS");

                // Leemos respuesta.
                String imagePath;
                List<String> imagePathsList = new ArrayList<>();
                while ((imagePath = in.readLine()) != null) {
                    imagePathsList.add(imagePath);
                }

                // Cerramos el socket.
                out.close();
                in.close();
                socket.close();

                // Manejamos la respuesta del servidor en el hilo principal
                handler.post(() -> handleServerResponse(imagePathsList.toArray(new String[0])));

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Método para manejar la respuesta del servidor y actualizar la vista del carrusel de imágenes de películas.
     *
     * @param imagePaths Arreglo de strings que contiene las URL de las imágenes de las películas.
     */
    private void handleServerResponse(String[] imagePaths) {
        imageURLs = Arrays.asList(imagePaths);

        // Crear un adaptador personalizado para cargar las imágenes en el ViewPager
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(imageURLs, this);

        // Establecer el adaptador en el ViewPager
        viewPager.setAdapter(pagerAdapter);
    }

    /**
     * Método para manejar la pulsación en una imagen de película en el carrusel.
     *
     * @param position La posición de la imagen de la película pulsada.
     */
    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getActivity(), MovieDataActivity.class);
        intent.putExtra("image_url", imageURLs.get(position));
        assert getArguments() != null;
        intent.putExtra("correo", getArguments().getString("CORREO"));
        startActivity(intent);
    }
}
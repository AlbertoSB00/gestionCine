package com.mobilepulse.gestioncine.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.mobilepulse.gestioncine.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private static final String IP = "192.168.0.108";
    private static final int PORT = 12345;
    private static final int DELAY_MS = 3000;

    private Handler sliderHandler;
    private Runnable sliderRunnable;

    private ViewPager viewPager;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler();

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.viewPager);
        ordenServer();
    }

    private void ordenServer() {
        executorService.execute(() -> {
            String[] response;

            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos orden al servidor.
                out.println("GET_IMAGE_PATHS");

                // Leemos respuesta.
                String imagePath;
                List<String> imagePathsList = new ArrayList<>();
                while ((imagePath = in.readLine()) != null) {
                    imagePathsList.add(imagePath);
                }
                response = imagePathsList.toArray(new String[0]);

                // Cerramos el socket.
                out.close();
                in.close();
                socket.close();

            } catch (IOException e) {
                // En caso de error, devolvemos un array vacío
                response = new String[0];
            }

            final String[] result = response;
            handler.post(() -> handleServerResponse(result));
        });
    }

    // En el método handleServerResponse
    private void handleServerResponse(String[] imagePaths) {
        List<String> imageURLs = Arrays.asList(imagePaths);

        // Crear un adaptador personalizado para cargar las imágenes en el ViewPager
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(imageURLs);
        viewPager.setAdapter(pagerAdapter);

        // Iniciar el temporizador para el cambio automático de imágenes
        startSlider();
    }

    private void startSlider() {
        sliderHandler = new Handler();
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPager.getCurrentItem();
                int totalItems = Objects.requireNonNull(viewPager.getAdapter()).getCount();
                int nextItem = (currentItem + 1) % totalItems; // Calcula el siguiente índice de imagen
                viewPager.setCurrentItem(nextItem, true); // Cambia al siguiente índice de imagen
                sliderHandler.postDelayed(this, DELAY_MS); // Programa la ejecución del próximo cambio después de un retraso
            }
        };

        // Iniciamos el slider
        sliderHandler.postDelayed(sliderRunnable, DELAY_MS);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Detenemos el slider cuando la actividad o fragmento se detiene
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onStart() {
        super.onStart();
        startSlider();
    }
}

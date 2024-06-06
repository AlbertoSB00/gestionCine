package com.mobilepulse.gestioncine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.mobilepulse.gestioncine.R;
import com.mobilepulse.gestioncine.adapters.MovieSliderAdapter;

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

public class AddCommentActivity extends AppCompatActivity {

    private static final String IP = "192.168.0.108";
    private static final int PORT = 12345;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String correo;
    private int selectedMovieId; // Cambiar a int para el ID de la película
    private EditText etComment;
    private RatingBar rbRating;
    private List<String> movieImageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        Intent intent = getIntent();
        correo = intent.getStringExtra("CORREO");

        // Obtener referencias a los componentes
        ViewPager vpMovieSlider = findViewById(R.id.vp_movie_slider);
        etComment = findViewById(R.id.et_comment);
        rbRating = findViewById(R.id.rb_rating);
        Button btnSubmit = findViewById(R.id.btn_submit);

        // Inicializar la lista de películas
        movieImageUrls = new ArrayList<>();

        // Configurar el slider de películas
        fetchMovieList(vpMovieSlider);

        // Listener para obtener la película seleccionada
        vpMovieSlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                selectedMovieId = position; // La posición coincide con el ID de la película
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // Manejar el evento de clic del botón de envío
        btnSubmit.setOnClickListener(v -> submitComment());
    }

    // Método para enviar el comentario
    private void submitComment() {
        String comment = etComment.getText().toString();
        int rating = (int) rbRating.getRating();

        executorService.execute(() -> {
            String result = submitCommentToServer(comment, rating);
            handler.post(() -> {
                if ("INSERT_COMMENT_SUCCESS".equals(result)) {
                    Toast.makeText(this, "Comentario añadido", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("CORREO", correo);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Hubo un error al añadir su comentario", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Obtener id del usuario
    private CompletableFuture<Integer> obtenerIdUsuario(String correo) {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket socket = new Socket(IP, PORT)) {
                socket.setSoTimeout(5000); // 5 segundos de timeout
                try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    out.println("GET_USER_ID");
                    out.println(correo);
                    String response = in.readLine();

                    if (response == null || response.isEmpty()) {
                        throw new RuntimeException("Received empty response for user ID.");
                    }
                    return Integer.parseInt(response);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error al obtener el ID de usuario.", e);
            }
        }, executorService);
    }

    private String submitCommentToServer(String comment, int rating) {
        try {
            CompletableFuture<Integer> userIdFuture = obtenerIdUsuario(correo);

            Integer userId = userIdFuture.join();

            try (Socket socket = new Socket(IP, PORT)) {
                socket.setSoTimeout(5000); // 5 segundos de timeout
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    out.println("INSERT_COMMENT");
                    out.println(userId);
                    out.println(selectedMovieId + 1);
                    out.println(rating);
                    out.println(comment);
                    return in.readLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "INSERT_COMMENT_FAILED";
        }
    }

    // Método para obtener la lista de películas
    private void fetchMovieList(ViewPager viewPager) {
        executorService.execute(() -> {
            List<String> fetchedMovieList = fetchMoviesFromServer();
            handler.post(() -> {
                movieImageUrls.clear();
                movieImageUrls.addAll(fetchedMovieList);
                MovieSliderAdapter adapter = new MovieSliderAdapter(AddCommentActivity.this, movieImageUrls);
                viewPager.setAdapter(adapter);
            });
        });
    }

    private List<String> fetchMoviesFromServer() {
        List<String> movieList = new ArrayList<>();
        try (Socket socket = new Socket(IP, PORT)) {
            socket.setSoTimeout(5000); // 5 segundos de timeout
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println("GET_ALL_IMAGE_PATHS");
                String response;
                while ((response = in.readLine()) != null) {
                    movieList.add(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movieList;
    }
}
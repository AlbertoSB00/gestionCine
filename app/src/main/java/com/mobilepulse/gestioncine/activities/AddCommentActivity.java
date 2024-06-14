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
 * Actividad para añadir comentarios y valoraciones a las películas.
 */
public class AddCommentActivity extends AppCompatActivity {

    private static final String IP = Configuration.IP;
    private static final int PORT = Configuration.PORT;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String correo;
    private int selectedMovieId;
    private EditText etComment;
    private RatingBar rbRating;
    private List<String> movieImageUrls;

    /**
     * Llamado cuando la actividad es creada por primera vez.
     *
     * @param savedInstanceState Si la actividad está siendo re-inicializada después de haber sido previamente terminada, este Bundle contiene los datos que más recientemente suministró en onSaveInstanceState(Bundle). De lo contrario, está nulo.
     */
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
                selectedMovieId = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        btnSubmit.setOnClickListener(v -> submitComment());
    }

    /**
     * Método para enviar el comentario.
     */
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

    /**
     * Obtener el ID del usuario.
     *
     * @param correo El correo electrónico del usuario.
     * @return Un CompletableFuture que contiene el ID del usuario.
     */
    private CompletableFuture<Integer> obtenerIdUsuario(String correo) {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket socket = new Socket(IP, PORT)) {
                socket.setSoTimeout(5000);
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

    /**
     * Enviar el comentario al servidor.
     *
     * @param comment El comentario del usuario.
     * @param rating La valoración del usuario.
     * @return Una cadena que indica el resultado de la operación.
     */
    private String submitCommentToServer(String comment, int rating) {
        try {
            CompletableFuture<Integer> userIdFuture = obtenerIdUsuario(correo);

            Integer userId = userIdFuture.join();

            try (Socket socket = new Socket(IP, PORT)) {
                socket.setSoTimeout(5000);
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

    /**
     * Método para obtener la lista de películas.
     *
     * @param viewPager El ViewPager donde se mostrarán las películas.
     */
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

    /**
     * Obtener la lista de películas desde el servidor.
     *
     * @return Una lista de URLs de las imágenes de las películas.
     */
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
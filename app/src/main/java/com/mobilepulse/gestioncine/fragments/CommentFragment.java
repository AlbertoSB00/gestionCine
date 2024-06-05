package com.mobilepulse.gestioncine.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobilepulse.gestioncine.R;
import com.mobilepulse.gestioncine.adapters.CommentsAdapter;
import com.mobilepulse.gestioncine.classes.Comment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentFragment extends Fragment {

    private static final String IP = "192.168.0.108";
    private static final int PORT = 12345;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private RecyclerView recyclerViewComments;
    private CommentsAdapter commentsAdapter;
    private List<Comment> commentList = new ArrayList<>();

    public CommentFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aquí puedes inicializar cualquier dato que necesites
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        recyclerViewComments = view.findViewById(R.id.recyclerViewComments);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsAdapter = new CommentsAdapter(getContext(), commentList);
        recyclerViewComments.setAdapter(commentsAdapter);

        loadComments(); // Método para cargar los comentarios desde la BD

        return view;
    }

    private void loadComments() {
        executorService.execute(() -> {
            String response;

            try {
                Socket socket = new Socket(IP, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviamos la orden al servidor para obtener los comentarios.
                out.println("GET_COMMENTS");

                // Leemos la respuesta del servidor.
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    responseBuilder.append(line).append("\n");
                }
                response = responseBuilder.toString().trim();

                // Cerramos el socket.
                out.close();
                in.close();
                socket.close();

            } catch (IOException e) {
                response = "ERROR";
            }

            final String result = response;
            handler.post(() -> handleCommentsResponse(result));
        });
    }

    // Método para manejar la respuesta del servidor y actualizar la lista de comentarios.
    private void handleCommentsResponse(String result) {
        if (!"ERROR".equals(result)) {
            commentList.clear();
            String[] comments = result.split("\n");
            for (String commentData : comments) {
                String[] fields = commentData.split(";");
                if (fields.length == 5) {
                    String name = fields[0];
                    int rating = Integer.parseInt(fields[1]);
                    String commentText = fields[2];
                    String dateTime = fields[3];
                    String imageUrl = fields[4];
                    commentList.add(new Comment(name, rating, commentText, dateTime, imageUrl));
                }
            }
            commentsAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), "Error al cargar comentarios", Toast.LENGTH_SHORT).show();
        }
    }

}
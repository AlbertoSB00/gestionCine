package com.mobilepulse.gestioncine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobilepulse.gestioncine.R;
import com.mobilepulse.gestioncine.classes.Comment;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adaptador para mostrar la lista de comentarios en un RecyclerView.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private final List<Comment> commentList;
    private final Context context;

    /**
     * Constructor de la clase CommentsAdapter.
     *
     * @param context     Contexto de la aplicación.
     * @param commentList Lista de comentarios a mostrar.
     */
    public CommentsAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    /**
     * Método llamado cuando se necesita crear un nuevo ViewHolder.
     *
     * @param parent   El ViewGroup en el que se inflará la vista.
     * @param viewType El tipo de vista que se está inflando.
     * @return Un nuevo CommentViewHolder que contiene la vista para cada elemento de la lista.
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Método llamado por RecyclerView para mostrar los datos en una posición específica.
     *
     * @param holder   El ViewHolder que debe actualizarse.
     * @param position La posición del elemento en los datos.
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.userName.setText(comment.getUserName());
        holder.ratingBar.setRating(comment.getRating());
        holder.commentText.setText(comment.getComment());
        holder.dateTime.setText(comment.getDateTime());
        Picasso.get().load(comment.getImageUrl()).into(holder.movieImage);
    }

    /**
     * Método llamado por RecyclerView para obtener el número de elementos en el conjunto de datos.
     *
     * @return El número total de elementos en la lista de comentarios.
     */
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    /**
     * Clase interna que representa el ViewHolder para cada elemento de la lista de comentarios.
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName, commentText, dateTime;
        RatingBar ratingBar;
        ImageView movieImage;

        /**
         * Constructor de la clase CommentViewHolder.
         *
         * @param itemView La vista que se va a inflar como un elemento de la lista.
         */
        CommentViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            commentText = itemView.findViewById(R.id.commentText);
            dateTime = itemView.findViewById(R.id.dateTime);
            movieImage = itemView.findViewById(R.id.movieImage);
        }
    }
}

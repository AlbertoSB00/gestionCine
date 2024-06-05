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

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> commentList;
    private Context context;

    public CommentsAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.userName.setText(comment.getUserName());
        holder.ratingBar.setRating(comment.getRating());
        holder.commentText.setText(comment.getComment());
        holder.dateTime.setText(comment.getDateTime());
        Picasso.get().load(comment.getImageUrl()).into(holder.movieImage);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName, commentText, dateTime;
        RatingBar ratingBar;
        ImageView movieImage;

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

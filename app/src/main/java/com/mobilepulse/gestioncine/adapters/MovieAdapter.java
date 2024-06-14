package com.mobilepulse.gestioncine.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mobilepulse.gestioncine.R;
import com.mobilepulse.gestioncine.interfaces.OnItemClickListener;

import java.util.List;

/**
 * Adaptador para mostrar películas en un RecyclerView.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private final List<String> movieUrls;
    private final OnItemClickListener onItemClickListener;

    /**
     * Constructor de la clase MovieAdapter.
     *
     * @param movieUrls             Lista de URLs de las imágenes de las películas.
     * @param onItemClickListener Listener para gestionar los clics en las películas.
     */
    public MovieAdapter(List<String> movieUrls, OnItemClickListener onItemClickListener) {
        this.movieUrls = movieUrls;
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Crea nuevas instancias de ViewHolder.
     *
     * @param parent   El ViewGroup al que se añadirá la vista.
     * @param viewType El tipo de la vista.
     * @return Una nueva instancia de ViewHolder.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view, onItemClickListener);
    }

    /**
     * Actualiza el contenido de la vista en la posición especificada.
     *
     * @param holder   El ViewHolder que debe ser actualizado para representar los contenidos del elemento en la posición dada en los datos.
     * @param position La posición del elemento en el conjunto de datos del adaptador.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String movieUrl = movieUrls.get(position);
        Glide.with(holder.itemView.getContext())
                .load(movieUrl)
                .into(holder.imageView);
    }

    /**
     * Devuelve el número total de elementos en el conjunto de datos del adaptador.
     *
     * @return El número total de elementos.
     */
    @Override
    public int getItemCount() {
        return movieUrls.size();
    }

    /**
     * ViewHolder para cada elemento de la lista.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        OnItemClickListener onItemClickListener;

        /**
         * Constructor de la clase ViewHolder.
         *
         * @param itemView             La vista que se debe vincular.
         * @param onItemClickListener El Listener para los clics en los elementos de la lista.
         */
        public ViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        /**
         * Se llama cuando se hace clic en la vista.
         *
         * @param v La vista que se ha hecho clic.
         */
        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(getAdapterPosition());
        }
    }
}
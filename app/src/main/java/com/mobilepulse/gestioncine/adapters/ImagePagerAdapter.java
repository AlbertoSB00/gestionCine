package com.mobilepulse.gestioncine.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.mobilepulse.gestioncine.interfaces.OnItemClickListener;

import java.util.List;

/**
 * Adaptador para mostrar imágenes en un ViewPager.
 */
public class ImagePagerAdapter extends PagerAdapter {

    private final List<String> imagePaths;
    private final OnItemClickListener listener;

    /**
     * Constructor de la clase ImagePagerAdapter.
     *
     * @param imagePaths Lista de rutas de las imágenes.
     * @param listener   Listener para gestionar clics en las imágenes.
     */
    public ImagePagerAdapter(List<String> imagePaths, OnItemClickListener listener) {
        this.imagePaths = imagePaths;
        this.listener = listener;
    }

    /**
     * Obtiene el número total de imágenes en el ViewPager.
     *
     * @return El número total de imágenes.
     */
    @Override
    public int getCount() {
        return imagePaths.size();
    }

    /**
     * Determina si una vista dada es asociada con un objeto específico.
     *
     * @param view   Vista a comparar.
     * @param object Objeto a comparar.
     * @return True si la vista es igual al objeto, false de lo contrario.
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    /**
     * Crea la vista para la página específica en la posición especificada.
     *
     * @param container Contenedor donde se debe agregar la vista.
     * @param position  Posición de la página en el ViewPager.
     * @return La vista para la página en la posición dada.
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(container.getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(container.getContext())
                .load(imagePaths.get(position))
                .into(imageView);
        container.addView(imageView);

        imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });

        return imageView;
    }

    /**
     * Elimina una vista específica.
     *
     * @param container Contenedor donde se encuentra la vista a eliminar.
     * @param position  Posición de la vista a eliminar.
     * @param object    Vista a eliminar.
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
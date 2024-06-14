package com.mobilepulse.gestioncine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.mobilepulse.gestioncine.R;

import java.util.List;

/**
 * Adaptador para mostrar imágenes de películas en un ViewPager.
 */
public class MovieSliderAdapter extends PagerAdapter {

    private final Context context;
    private final List<String> imageUrls;

    /**
     * Constructor de la clase MovieSliderAdapter.
     *
     * @param context   El contexto de la aplicación.
     * @param imageUrls Lista de URLs de las imágenes de las películas.
     */
    public MovieSliderAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    /**
     * Obtiene el número total de elementos en el conjunto de datos del adaptador.
     *
     * @return El número total de elementos.
     */
    @Override
    public int getCount() {
        return imageUrls.size();
    }

    /**
     * Determina si una página vista proporcionada es asociada con una clave específica.
     *
     * @param view   Vista de la página.
     * @param object Clave única de la página.
     * @return True si la vista de la página es la misma que el objeto proporcionado; de lo contrario, false.
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * Crea una nueva instancia de la vista en la posición especificada.
     *
     * @param container Contenedor en el que se debe agregar la vista.
     * @param position  La posición de la vista en el conjunto de datos.
     * @return La nueva instancia de la vista.
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.slider_item, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);

        Glide.with(context)
                .load(imageUrls.get(position))
                .into(imageView);

        container.addView(view);
        return view;
    }

    /**
     * Elimina la vista en la posición especificada.
     *
     * @param container Contenedor del que se debe eliminar la vista.
     * @param position  La posición de la vista a eliminar.
     * @param object    La vista que se debe eliminar.
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
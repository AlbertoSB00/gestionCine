package com.mobilepulse.gestioncine.interfaces;

/**
 * Interfaz para manejar los clics en los elementos de la lista.
 */
public interface OnItemClickListener {

    /**
     * Método llamado cuando se hace clic en un elemento de la lista.
     *
     * @param position La posición del elemento en la lista en la que se ha hecho clic.
     */
    void onItemClick(int position);
}

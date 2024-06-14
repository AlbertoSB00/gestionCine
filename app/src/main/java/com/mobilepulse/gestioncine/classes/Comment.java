package com.mobilepulse.gestioncine.classes;

/**
 * Representa un comentario sobre una película.
 */
public class Comment {
    private final String userName;
    private final int rating;
    private final String comment;
    private final String dateTime;
    private final String imageUrl;

    /**
     * Constructor de la clase Comment.
     *
     * @param userName Nombre del usuario que hizo el comentario.
     * @param rating   Calificación asignada al comentario.
     * @param comment  Texto del comentario.
     * @param dateTime Fecha y hora en que se hizo el comentario.
     * @param imageUrl URL de la imagen asociada al comentario.
     */
    public Comment(String userName, int rating, String comment, String dateTime, String imageUrl) {
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.dateTime = dateTime;
        this.imageUrl = imageUrl;
    }

    /**
     * Obtiene el nombre del usuario que hizo el comentario.
     *
     * @return Nombre del usuario.
     */
    public String getUserName() { return userName; }

    /**
     * Obtiene la calificación asignada al comentario.
     *
     * @return Calificación del comentario.
     */
    public int getRating() { return rating; }

    /**
     * Obtiene el texto del comentario.
     *
     * @return Texto del comentario.
     */
    public String getComment() { return comment; }

    /**
     * Obtiene la fecha y hora en que se hizo el comentario.
     *
     * @return Fecha y hora del comentario.
     */
    public String getDateTime() { return dateTime; }

    /**
     * Obtiene la URL de la imagen asociada al comentario.
     *
     * @return URL de la imagen.
     */
    public String getImageUrl() { return imageUrl; }
}
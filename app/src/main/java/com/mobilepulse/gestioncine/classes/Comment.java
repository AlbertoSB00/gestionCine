package com.mobilepulse.gestioncine.classes;

public class Comment {
    private final String userName;
    private final int rating;
    private final String comment;
    private final String dateTime;
    private final String imageUrl;

    // Constructor
    public Comment(String userName, int rating, String comment, String dateTime, String imageUrl) {
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.dateTime = dateTime;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public String getUserName() { return userName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getDateTime() { return dateTime; }
    public String getImageUrl() { return imageUrl; }
}
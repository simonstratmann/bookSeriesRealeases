package de.sist.bookseries.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Book {

    private String series;
    private int number;
    private String title;
    private Publication publication;
    private String url;
    private double rating;
    private int ratings;

    public Book() {
    }


    public Book(String series, int number, String title, String url, double rating, int ratings) {
        this.number = number;
        this.title = title;
        this.url = url;
        this.rating = rating;
        this.ratings = ratings;
    }


    public String getSeries() {
        return series;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public Publication getPublication() {
        return publication;
    }

    public String getUrl() {
        return url;
    }

    public double getRating() {
        return rating;
    }

    public int getRatings() {
        return ratings;
    }

    public void update(Book book) {
        series = book.series;
        number = book.number;
        title = book.title;
        if (book.publication != null) {
            publication = book.publication;
        }
        rating = book.rating;
        ratings = book.ratings;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    @Override
    public String toString() {

        return String.format("Series: %s. Title: %s. Book number: %s. Publication: %s. Rating: %.2f (%d ratings)", series, title, number, publication, rating, ratings);

    }
}

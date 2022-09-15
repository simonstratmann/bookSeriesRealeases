package de.sist.bookseries.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Book {

    @JsonIgnore
    private BookSeries series;
    private int number;
    private String title;
    private Publication publication;
    private String url;
    private double rating;
    private int ratings;

    public Book() {
    }


    public Book(int number, String title, String url, double rating, int ratings) {
        this.number = number;
        this.title = title;
        this.url = url;
        this.rating = rating;
        this.ratings = ratings;
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
        final String seriesTitle = series != null ? series.getTitle() : "<null>";
        final String ofKnown = series == null ? "" : (" of "+Iterables.getLast(series.getBooks()).getNumber());
        return String.format("Series: %s. Title: %s. Book number: %s%s. Publication: %s. Rating: %.2f (%d ratings). %s", seriesTitle, title, number, ofKnown, publication, rating, ratings, url);

    }

    @JsonIgnore
    public void setSeries(BookSeries bookSeries) {
        series = bookSeries;
    }
}

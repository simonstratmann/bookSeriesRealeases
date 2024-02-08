package de.sist.bookseries.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Iterables;

@SuppressWarnings("unused")
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

    public BookSeries getSeries() {
        return series;
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
        return toStringNoUrl() + " " + url;
    }


    public String toStringNoUrl() {
        final String seriesTitle = series != null ? series.getTitle() : "<null>";
        final String ofKnown = series == null ? "" : (" of "+Iterables.getLast(series.getBooks()).getNumber());
        final String author = series == null ? "<Unknown>" : series.getAuthor();
        return String.format("%-25s %-30s Book %s%s  %-30s   Publication: %-20s  Rating: %.2f (%d ratings)", author, seriesTitle, number, ofKnown,title, publication, rating, ratings);
    }

    @JsonIgnore
    public void setSeries(BookSeries bookSeries) {
        series = bookSeries;
    }
}

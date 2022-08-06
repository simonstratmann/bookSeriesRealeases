package de.sist.bookseries.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    private String series;
    private int number;
    private String title;
    private Publication publication;
    private String url;
    private double rating;
    private int ratings;


    public Book(String series, int number, String title, String url, double rating, int ratings) {
        this.number = number;
        this.title = title;
        this.url = url;
        this.rating = rating;
        this.ratings = ratings;
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
}

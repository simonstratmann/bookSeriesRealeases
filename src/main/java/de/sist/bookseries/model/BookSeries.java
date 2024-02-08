package de.sist.bookseries.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BookSeries {

    private String title;
    private String author;
    private String goodReadsUrl;
    private Instant lastUpdate;
    private List<Book> books = new ArrayList<>();

    public BookSeries() {
    }

    public BookSeries(String title, String author, String goodReadsUrl, List<Book> books) {
        this.title = title;
        this.author = author;
        this.goodReadsUrl = goodReadsUrl;
        this.books = books;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGoodReadsUrl() {
        return goodReadsUrl;
    }

    public List<Book> getBooks() {
        return books;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}

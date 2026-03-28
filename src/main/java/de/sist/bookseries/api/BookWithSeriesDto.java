package de.sist.bookseries.api;

import java.time.LocalDate;

public class BookWithSeriesDto {

    private String seriesTitle;
    private String seriesAuthor;
    private String seriesUrl;
    private int bookNumber;
    private int totalBooks;
    private String title;
    private String bookUrl;
    private double rating;
    private int ratings;
    private LocalDate publicationDate;
    private LocalDate expectedPublicationDate;

    public String getSeriesTitle() { return seriesTitle; }
    public void setSeriesTitle(String seriesTitle) { this.seriesTitle = seriesTitle; }

    public String getSeriesAuthor() { return seriesAuthor; }
    public void setSeriesAuthor(String seriesAuthor) { this.seriesAuthor = seriesAuthor; }

    public String getSeriesUrl() { return seriesUrl; }
    public void setSeriesUrl(String seriesUrl) { this.seriesUrl = seriesUrl; }

    public int getBookNumber() { return bookNumber; }
    public void setBookNumber(int bookNumber) { this.bookNumber = bookNumber; }

    public int getTotalBooks() { return totalBooks; }
    public void setTotalBooks(int totalBooks) { this.totalBooks = totalBooks; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBookUrl() { return bookUrl; }
    public void setBookUrl(String bookUrl) { this.bookUrl = bookUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getRatings() { return ratings; }
    public void setRatings(int ratings) { this.ratings = ratings; }

    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

    public LocalDate getExpectedPublicationDate() { return expectedPublicationDate; }
    public void setExpectedPublicationDate(LocalDate expectedPublicationDate) { this.expectedPublicationDate = expectedPublicationDate; }
}

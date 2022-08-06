package de.sist.bookseries.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSeries {

    private String title;
    private String author;
    private String goodReadsUrl;
    private List<Book> books = new ArrayList<>();

}

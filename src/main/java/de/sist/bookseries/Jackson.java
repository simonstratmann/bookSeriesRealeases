package de.sist.bookseries;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.sist.bookseries.model.BookSeries;

import java.io.File;
import java.util.List;

public class Jackson {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final File BOOK_SERIES_JSON_FILE = new File("bookSeries.json");

    private Jackson() {
    }

    public static void write(List<BookSeries> bookSeriesList) throws Exception {
        OBJECT_MAPPER.writeValue(BOOK_SERIES_JSON_FILE, bookSeriesList);
    }

    public static List<BookSeries> load() throws Exception {
        return OBJECT_MAPPER.readValue(BOOK_SERIES_JSON_FILE, new TypeReference<>() {
        });
    }


}

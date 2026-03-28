package de.sist.bookseries.api;

import de.sist.bookseries.AddSeriesCommand;
import de.sist.bookseries.Jackson;
import de.sist.bookseries.UpdateSeriesCommand;
import de.sist.bookseries.model.Book;
import de.sist.bookseries.model.BookSeries;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookSeriesController {

    private final AtomicBoolean updateRunning = new AtomicBoolean(false);

    @GetMapping("/series")
    public List<BookSeries> getSeries() throws Exception {
        return Jackson.load();
    }

    @DeleteMapping("/series/{index}")
    public ResponseEntity<Void> deleteSeries(@PathVariable int index) throws Exception {
        List<BookSeries> series = Jackson.load();
        if (index < 0 || index >= series.size()) {
            return ResponseEntity.notFound().build();
        }
        series.remove(index);
        Jackson.write(series);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/series")
    public ResponseEntity<String> addSeries(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || !url.contains("/series/")) {
            return ResponseEntity.badRequest().body("URL must be a Goodreads series URL");
        }
        new Thread(() -> {
            try {
                List<BookSeries> existing = Jackson.load();
                if (existing.stream().anyMatch(x -> x.getGoodReadsUrl().equals(url))) {
                    return;
                }
                List<BookSeries> newSeries = AddSeriesCommand.createInitialBookSeriesListFromUrl(url);
                for (BookSeries series : newSeries) {
                    UpdateSeriesCommand.updateSeries(series);
                }
                existing.addAll(newSeries);
                Jackson.write(existing);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return ResponseEntity.ok("Adding series in background...");
    }

    @GetMapping("/update/status")
    public Map<String, Boolean> getUpdateStatus() {
        return Collections.singletonMap("running", updateRunning.get());
    }

    @PostMapping("/update")
    public ResponseEntity<String> update() {
        if (updateRunning.get()) {
            return ResponseEntity.ok("Update already running");
        }
        updateRunning.set(true);
        new Thread(() -> {
            try {
                new UpdateSeriesCommand().call();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                updateRunning.set(false);
            }
        }).start();
        return ResponseEntity.ok("Update started");
    }

    @GetMapping("/books/recent")
    public Map<String, Object> getRecentBooks(
            @RequestParam(defaultValue = "90") int lastDays,
            @RequestParam(defaultValue = "90") int nextDays) throws Exception {
        List<BookSeries> bookSeriesList = Jackson.load();
        for (BookSeries bookSeries : bookSeriesList) {
            for (Book book : bookSeries.getBooks()) {
                book.setSeries(bookSeries);
            }
        }

        List<BookWithSeriesDto> recentlyPublished = bookSeriesList.stream()
                .flatMap(x -> x.getBooks().stream())
                .filter(book -> {
                    if (book.getPublication() != null && !book.getPublication().notYetPublished()) {
                        Optional<LocalDate> date = book.getPublication().getPublicationDate().toLocalDate();
                        return date.isPresent() && date.get().isAfter(LocalDate.now().minusDays(lastDays));
                    }
                    return false;
                })
                .sorted(Comparator.comparing(b -> b.getPublication().getPublicationDate().toLocalDate().orElse(LocalDate.MAX)))
                .map(this::toDto)
                .collect(Collectors.toList());

        List<BookWithSeriesDto> upcoming = bookSeriesList.stream()
                .flatMap(x -> x.getBooks().stream())
                .filter(book -> {
                    if (book.getPublication() != null && book.getPublication().notYetPublished()) {
                        Optional<LocalDate> date = book.getPublication().getExpectedPublicationDate().toLocalDate();
                        return date.isPresent() && date.get().isBefore(LocalDate.now().plusDays(nextDays));
                    }
                    return false;
                })
                .sorted(Comparator.comparing(b -> b.getPublication().getExpectedPublicationDate().toLocalDate().orElse(LocalDate.MAX)))
                .map(this::toDto)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recentlyPublished", recentlyPublished);
        result.put("upcoming", upcoming);
        return result;
    }

    private BookWithSeriesDto toDto(Book book) {
        BookWithSeriesDto dto = new BookWithSeriesDto();
        dto.setTitle(book.getTitle());
        dto.setBookUrl(book.getUrl());
        dto.setBookNumber(book.getNumber());
        dto.setRating(book.getRating());
        dto.setRatings(book.getRatings());
        if (book.getSeries() != null) {
            dto.setSeriesTitle(book.getSeries().getTitle());
            dto.setSeriesAuthor(book.getSeries().getAuthor());
            dto.setSeriesUrl(book.getSeries().getGoodReadsUrl());
            dto.setTotalBooks(book.getSeries().getBooks().size());
        }
        if (book.getPublication() != null) {
            if (!book.getPublication().notYetPublished() && book.getPublication().getPublicationDate() != null) {
                dto.setPublicationDate(book.getPublication().getPublicationDate().toLocalDate().orElse(null));
            }
            if (book.getPublication().notYetPublished() && book.getPublication().getExpectedPublicationDate() != null) {
                dto.setExpectedPublicationDate(book.getPublication().getExpectedPublicationDate().toLocalDate().orElse(null));
            }
        }
        return dto;
    }
}

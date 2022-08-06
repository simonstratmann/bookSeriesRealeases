package de.sist.bookseries;

import com.google.common.base.Joiner;
import de.sist.bookseries.model.Book;
import de.sist.bookseries.model.BookSeries;
import picocli.CommandLine;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;


@CommandLine.Command(name = "showCurrent", description = "Show books released lately or to be released soon(ish)")
public class ShowCurrentBooksCommand implements Callable<Integer> {


    private static final int EXPECTED_NEXT_DAYS = 90;
    private static final int PUBLISHED_LAST_DAYS = 90;

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new ShowCurrentBooksCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        final List<BookSeries> bookSeriesList = Jackson.load();
        final List<Book> expectedNextTime = bookSeriesList.stream().flatMap(x -> x.getBooks().stream()).filter(book -> {
                    if (book.getPublication() != null && book.getPublication().notYetPublished()) {
                        final Optional<LocalDate> expectedDate = book.getPublication().getExpectedPublicationDate().toLocalDate();
                        return expectedDate.isPresent() && expectedDate.get().isBefore(LocalDate.now().plusDays(EXPECTED_NEXT_DAYS));
                    }
                    return false;
                })
                .collect(Collectors.toList());
        if (!expectedNextTime.isEmpty()) {
            System.out.println("Expected books in the next " + EXPECTED_NEXT_DAYS + " days:");
            System.out.println(Joiner.on("\n").join(expectedNextTime));
        }

        final List<Book> releasedLately = bookSeriesList.stream().flatMap(x -> x.getBooks().stream()).filter(book -> {
                    if (book.getPublication() != null && !book.getPublication().notYetPublished()) {
                        final Optional<LocalDate> publicationDate = book.getPublication().getPublicationDate().toLocalDate();
                        return publicationDate.isPresent() && publicationDate.get().isAfter(LocalDate.now().minusDays(PUBLISHED_LAST_DAYS));
                    }
                    return false;
                })
                .collect(Collectors.toList());
        System.out.println();
        if (!releasedLately.isEmpty()) {
            System.out.println("Published books in the last " + PUBLISHED_LAST_DAYS + " days:");
            System.out.println(Joiner.on("\n").join(releasedLately));
        }
        return 0;
    }
}

package de.sist.bookseries;

import com.google.common.base.Joiner;
import de.sist.bookseries.model.Book;
import de.sist.bookseries.model.BookSeries;
import picocli.CommandLine;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;


@CommandLine.Command(name = "show", description = "Show books released lately or to be released soon(ish)", aliases = {"s"})
public class ShowCurrentBooksCommand implements Callable<Integer> {

    @CommandLine.Option(names = "-lastDays", defaultValue = "90", description = "Show books released the last x days (by default ${DEFAULT-VALUE})")
    private int publishedLastDays;

    @CommandLine.Option(names = "-nextDays", defaultValue = "90", description = "Show books expected the next x days (by default ${DEFAULT-VALUE})")
    private int expectedNextDays;


    public static void main(String[] args) {
        int exitCode = new CommandLine(new ShowCurrentBooksCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        final List<BookSeries> bookSeriesList = Jackson.load();
        for (BookSeries bookSeries : bookSeriesList) {
            for (Book book : bookSeries.getBooks()) {
                book.setSeries(bookSeries);
            }
        }

        final List<String> releasedLately = bookSeriesList.stream().flatMap(x -> x.getBooks().stream()).filter(book -> {
                    if (book.getPublication() != null && !book.getPublication().notYetPublished()) {
                        final Optional<LocalDate> publicationDate = book.getPublication().getPublicationDate().toLocalDate();
                        return publicationDate.isPresent() && publicationDate.get().isAfter(LocalDate.now().minusDays(publishedLastDays));
                    }
                    return false;
                })
                .sorted(Comparator.comparing(x -> x.getPublication().getPublicationDate().toLocalDate().orElse(LocalDate.MAX)))
                .map(x -> x.toStringNoUrl() + " " + x.getSeries().getGoodReadsUrl())
                .collect(Collectors.toList());
        System.out.println();
        if (!releasedLately.isEmpty()) {
            System.out.println("Published books in the last " + publishedLastDays + " days:");
            System.out.println(Joiner.on("\n").join(releasedLately));
        }

        final List<String> expectedNextTime = bookSeriesList.stream().flatMap(x -> x.getBooks().stream()).filter(book -> {
                    if (book.getPublication() != null && book.getPublication().notYetPublished()) {
                        final Optional<LocalDate> expectedDate = book.getPublication().getExpectedPublicationDate().toLocalDate();
                        return expectedDate.isPresent() && expectedDate.get().isBefore(LocalDate.now().plusDays(expectedNextDays));
                    }
                    return false;
                })
                .sorted(Comparator.comparing(x -> x.getPublication().getExpectedPublicationDate().toLocalDate().orElse(LocalDate.MAX)))
                .map(x -> x.toStringNoUrl() + " " + x.getSeries().getGoodReadsUrl())
                .collect(Collectors.toList());
        if (!expectedNextTime.isEmpty()) {
            System.out.println();
            System.out.println("Expected books in the next " + expectedNextDays + " days:");
            System.out.println(Joiner.on("\n").join(expectedNextTime));
        }

        return 0;
    }
}

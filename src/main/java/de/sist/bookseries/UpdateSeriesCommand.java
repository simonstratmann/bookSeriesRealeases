package de.sist.bookseries;

import com.google.common.base.Strings;
import de.sist.bookseries.model.Book;
import de.sist.bookseries.model.BookSeries;
import de.sist.bookseries.model.Publication;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import picocli.CommandLine;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@CommandLine.Command(name = "update", description = "Updates series data", aliases = {"u"})
@SuppressWarnings({"ConstantConditions", "CallToPrintStackTrace"})
public class UpdateSeriesCommand implements Callable<Integer> {

    private static final Pattern BOOK_NUMBER_PATTERN = Pattern.compile("Book (\\d+)");
    private static final Pattern DETAILS_PUBLICATION_PATTERN = Pattern.compile("(First)? published (?<published>[\\w ,]+)|(Expected publication:? ?(?<expected>[\\w ,]+))", Pattern.CASE_INSENSITIVE);


    @Override
    public Integer call() {
        try {

            final List<BookSeries> bookSeriesList = Jackson.load();

            update(bookSeriesList);
            Jackson.write(bookSeriesList);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public static void updateSeries(BookSeries series) {
        update(Collections.singletonList(series));
    }


    public static void main(String[] args) {
        int exitCode = new CommandLine(new UpdateSeriesCommand()).execute(args);
        System.exit(exitCode);
    }

    private static void update(List<BookSeries> bookSeriesList) {
        ChromeOptions options = new ChromeOptions();
        options.setBinary("c:\\temp\\GoogleChromePortableDev\\App\\Chrome-bin\\chrome.exe");
        WebDriver driver = new ChromeDriver(options);
        try {
            for (BookSeries series : bookSeriesList) {
                if (series.getLastUpdate() != null && series.getLastUpdate().isAfter(Instant.now().minus(4 * 7 * 24, ChronoUnit.HOURS))) {
                    System.out.println("Series was last updated less than 4 weeks ago - skipping it");
                    continue;
                }
                System.out.printf("Updating series %s by %s (%d of %d)%n", series.getTitle(), series.getAuthor(), bookSeriesList.indexOf(series)+1, bookSeriesList.size());
                updateBooksForSeries(series, driver);
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        try {
            Jackson.write(bookSeriesList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Completed");
    }

    private static void updateBooksForSeries(BookSeries series, WebDriver driver) {
        driver.get(series.getGoodReadsUrl());
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.findElement(By.className("responsiveBook"));

        final Document seriesDoc = Jsoup.parse(driver.getPageSource());
        List<String> foundBookUrls = new ArrayList<>();
        for (Element book : seriesDoc.select(".responsiveBook")) {
            try {
                final Element bookNumberElement = book.previousElementSibling();
                if (bookNumberElement == null) {
                    // Sometimes there are multiple editions shown in the book series, we usually are only interested in the first (most popular one)
                    continue;
                }
                final String bookNumberText = bookNumberElement.text();
                final Matcher numberMatcher = BOOK_NUMBER_PATTERN.matcher(bookNumberText);
                int ratings = 0;
                if (!numberMatcher.matches()) {
                    continue;
                }
                int bookNumber = Integer.parseInt(numberMatcher.group(1));
                final Element titleElement = book.selectFirst(new Evaluator.AttributeWithValue("itemprop", "name"));
                final String title = titleElement.text();
                final Elements metaTextElements = book.select(".gr-metaText");
                boolean published = false;
                for (Element metaTextElement : metaTextElements) {
                    final String text = metaTextElement.text();
                    if (text.endsWith("Ratings")) {
                        ratings = Integer.parseInt(text.replace(" Ratings", "").replace(",",""));
                    }
                    if (text.contains("published")) {
                        published = true;
                    }
                }

                final String bookUrl = "https://www.goodreads.com" + book.selectFirst(new Evaluator.AttributeWithValue("itemprop", "url")).attr("href");

                final double rating = Double.parseDouble(book.selectFirst(".communityRating > span > span").text());


                if (published && ratings < 50) {
                    // Skip obscure releases
                    continue;
                }
                final Book newBook = new Book(bookNumber, title, bookUrl, rating, ratings);
                final Optional<Book> matchingExistingBook = series.getBooks().stream().filter(x -> x.getUrl().equals(bookUrl)).findFirst();
                if (matchingExistingBook.isPresent()) {
                    System.out.println("Updating existing book " + newBook.getTitle());
                    matchingExistingBook.get().update(newBook);
                } else {
                    System.out.println("Adding new book " + newBook.getTitle());
                    series.getBooks().add(newBook);
                }
                foundBookUrls.add(newBook.getUrl());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        List<Book> unknownBooks = series.getBooks().stream().filter(x -> !foundBookUrls.contains(x.getUrl())).toList();
        if (!unknownBooks.isEmpty()) {
            System.out.println("These books cannot be found anymore on the series page: " + unknownBooks.stream().map(Book::getTitle).collect(Collectors.joining(", ")));
            series.getBooks().removeAll(unknownBooks);
        }
        for (Book book : series.getBooks()) {
            try {
                if (book.getTitle().contains("Untitled")) {
                    System.out.println("Skipping update of \"Untitled\" book - we don't expect any details yet");
                }
                if (book.getPublication() == null || book.getPublication().notYetPublished() || book.getPublication().getPublicationDate() == null) {
                    System.out.printf("Updating publication date for %s (%d)%n", book.getTitle(), book.getNumber());
                    driver.get(book.getUrl());
                    driver.findElement(By.className("FeaturedDetails"));
                    final Document bookDoc = Jsoup.parse(driver.getPageSource());
                    final Element publicationElement = bookDoc.selectFirst(".FeaturedDetails > p:nth-child(2)");
                    if (publicationElement == null) {
                        System.out.println("No publication data found");
                        continue;
                    }
                    final String publishedDateString = publicationElement.text();
                    final Publication publication = parsePublication(publishedDateString);
                    System.out.println("Determined publication " + publication);
                    book.setPublication(publication);
                }
            } catch (Exception e) {
                if (e.getMessage().contains("Unable to locate element: {\"method\":\"css selector\",\"selector\":\".FeaturedDetails\"}")) {
                    System.out.println("No publication details for " + book);
                    continue;
                }
                System.out.println("Error loading details for book " + book);
                e.printStackTrace();
            }
        }
        series.setLastUpdate(Instant.now());
        System.out.println();
    }

    static Publication parsePublication(String input) {
        final Matcher publicationMatcher = DETAILS_PUBLICATION_PATTERN.matcher(input);
        if (!publicationMatcher.find()) {
            System.out.println("No match: " + input);
            return null;
        }
        final boolean published;
        if (!Strings.isNullOrEmpty(publicationMatcher.group("published"))) {
            input = publicationMatcher.group("published");
            published = true;
        } else {
            input = publicationMatcher.group("expected");
            published = false;
        }
        input = input.trim().replaceAll("(\\d)(th|nd|rd|st)", "$1").replace(",","");
        final TemporalAccessor parsed = DateTimeFormatter.ofPattern("[LLLL ][d ]yyyy").parse(input);

        if (published) {
            return Publication.published(parsed);
        } else {
            return Publication.expected(parsed);
        }
    }

}

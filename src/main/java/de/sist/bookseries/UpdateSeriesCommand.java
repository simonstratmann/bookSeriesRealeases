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
import picocli.CommandLine;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@CommandLine.Command(name = "update", description = "Updates series data", aliases = {"u"})
@SuppressWarnings("ConstantConditions")
public class UpdateSeriesCommand implements Callable<Integer> {

    private static final Pattern BOOK_NUMBER_PATTERN = Pattern.compile("Book (\\d+)");
    private static final Pattern DETAILS_PUBLICATION_PATTERN = Pattern.compile("(Published (?<published>(?<month>\\w+)( (?<day>\\d+)\\w+)? (?<year>\\d+)))|(Expected publication: ?(?<expected>(?<expectedMonth>\\w+)?( (?<expectedDay>\\d+)\\w+)? (?<expectedYear>\\d+)))");


    @Override
    public Integer call() throws Exception {
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

    public static void updateSeries(BookSeries series) throws Exception {
        update(Collections.singletonList(series));
    }


    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new UpdateSeriesCommand()).execute(args);
        System.exit(exitCode);
    }

    private static void update(List<BookSeries> bookSeriesList) {
            WebDriver driver = new ChromeDriver();
        try {
            for (BookSeries series : bookSeriesList) {
                System.out.println("Updating series " + series.getTitle() + " by " + series.getAuthor());
                updateBooksForSeries(series, driver);
                Thread.sleep(2500);
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
    }

    private static void updateBooksForSeries(BookSeries series, WebDriver driver) throws IOException {
        driver.get(series.getGoodReadsUrl());
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.findElement(By.className("responsiveBook"));

        final Document seriesDoc = Jsoup.parse(driver.getPageSource());
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
                        ratings = Integer.parseInt(text.replace(" Ratings", ""));
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
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        for (Book book : series.getBooks()) {
            try {
                if (book.getPublication() == null || book.getPublication().notYetPublished() || book.getPublication().getPublicationDate() == null) {
                    System.out.printf("Updating publication date for %s (%d)%n", book.getTitle(), book.getNumber());
                    final Document bookDoc = Jsoup.connect(book.getUrl()).get();
                    final Element publicationElement = bookDoc.selectFirst(".FeaturedDetails > div:nth-child(2)");
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
                e.printStackTrace();
            }
        }
        System.out.println();
    }

    private static Publication parsePublication(String input) {
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
        input = input.trim().replaceAll("(\\d)(th|nd|rd|st)", "$1");
        final TemporalAccessor parsed = DateTimeFormatter.ofPattern("[LLLL ][d ]yyyy").parse(input);

        if (published) {
            return Publication.published(parsed);
        } else {
            return Publication.expected(parsed);
        }
    }

}

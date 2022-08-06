package de.sist.bookseries;

import de.sist.bookseries.model.BookSeries;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@CommandLine.Command(name = "addSeries", description = "Adds a new series by goodreads URL")
@SuppressWarnings("ConstantConditions")
public class AddSeriesCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Goodreads URL of the series")
    private String newSeriesUrl;


    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new AddSeriesCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        final List<BookSeries> bookSeriesList = Jackson.load();

        createInitialBookSeriesListFromListOfUrls(bookSeriesList, newSeriesUrl);

        Jackson.write(bookSeriesList);
        return 0;
    }


    private static void createInitialBookSeriesListFromListOfUrls(List<BookSeries> bookSeriesList, String... seriesUrl) {
        for (String url : seriesUrl) {
            try {
                final Document doc = Jsoup.connect(url).get();
                final Element seriesHeaderElement = doc.selectFirst(".responsiveSeriesHeader .gr-h1");
                final String seriesHeader = seriesHeaderElement.text();
                final Matcher matcher = Pattern.compile("(.+) by (.+)").matcher(seriesHeader);
                final String seriesAuthor;
                final String seriesTitle;
                if (matcher.matches()) {
                    seriesTitle = matcher.group(1);
                    seriesAuthor = matcher.group(2);
                } else {
                    seriesTitle = seriesHeader;
                    seriesAuthor = doc.selectFirst(new Evaluator.AttributeWithValue("itemprop", "author")).selectFirst(new Evaluator.AttributeWithValue("itemprop", "name")).text();
                }
                System.out.println(seriesAuthor + " - " + seriesTitle);
                bookSeriesList.add(new BookSeries(seriesTitle, seriesAuthor, url, Collections.emptyList()));
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println(url);
                e.printStackTrace();
            }
        }

    }
}

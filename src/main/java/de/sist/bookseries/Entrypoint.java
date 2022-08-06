package de.sist.bookseries;

import picocli.CommandLine;


@CommandLine.Command(subcommands = {AddSeriesCommand.class, ShowCurrentBooksCommand.class, UpdateSeriesCommand.class})
public class Entrypoint {

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static void main(String[] args) throws Exception {
        new CommandLine(new Entrypoint()).execute(args);
    }


}

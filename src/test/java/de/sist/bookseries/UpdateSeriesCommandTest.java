package de.sist.bookseries;

import de.sist.bookseries.model.Publication;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateSeriesCommandTest {

    @Test
    public void should() {
        Publication first = UpdateSeriesCommand.parsePublication("First published July 25, 2023");
        assertEquals(LocalDate.of(2023, 7, 25), first.getPublicationDate().toLocalDate().get());

        Publication expected = UpdateSeriesCommand.parsePublication("Expected publication January 1, 2024");
        assertEquals(LocalDate.of(2024, 1, 1), expected.getExpectedPublicationDate().toLocalDate().get());
    }

}
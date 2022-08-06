package de.sist.bookseries.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

@Data
public class PublicationDate {

    private int year;
    private int month;
    private int day;

    public static PublicationDate from(TemporalAccessor accessor) {
        final PublicationDate publicationDate = new PublicationDate();
        if (accessor.isSupported(ChronoField.YEAR)) {
            publicationDate.year = accessor.get(ChronoField.YEAR);
        }
        if (accessor.isSupported(ChronoField.MONTH_OF_YEAR)) {
            publicationDate.month = accessor.get(ChronoField.MONTH_OF_YEAR);
        }
        if (accessor.isSupported(ChronoField.DAY_OF_MONTH)) {
            publicationDate.day = accessor.get(ChronoField.DAY_OF_MONTH);
        }
        return publicationDate;
    }

    public Optional<LocalDate> toLocalDate() {
        if (year == 0 || month == 0 || day == 0) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.of(year, month, day));
    }

}

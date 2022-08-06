package de.sist.bookseries.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.temporal.TemporalAccessor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Publication {

    private PublicationDate publicationDate;
    private PublicationDate expectedPublicationDate;

    public static Publication published(TemporalAccessor accessor) {
        return new Publication(PublicationDate.from(accessor), null);
    }

    public static Publication expected(TemporalAccessor accessor) {
        return new Publication(null, PublicationDate.from(accessor));
    }


    public boolean notYetPublished() {
        return publicationDate == null;
    }

    public boolean hasConcreteExpectedData() {
        return expectedPublicationDate == null;
    }

}

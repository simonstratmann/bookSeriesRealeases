package de.sist.bookseries.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.temporal.TemporalAccessor;


public class Publication {

    private PublicationDate publicationDate;
    private PublicationDate expectedPublicationDate;

    public Publication() {
    }

    public Publication(PublicationDate publicationDate, PublicationDate expectedPublicationDate) {
        this.publicationDate = publicationDate;
        this.expectedPublicationDate = expectedPublicationDate;
    }

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

    public PublicationDate getPublicationDate() {
        return publicationDate;
    }

    public PublicationDate getExpectedPublicationDate() {
        return expectedPublicationDate;
    }

    @Override
    public String toString() {
        if (notYetPublished() && expectedPublicationDate != null) {
            return "Expected " + expectedPublicationDate;
        } else if (notYetPublished()) {
            return "No publication date known";
        } else if (publicationDate != null) {
            return publicationDate.toString();
        } else {
            return "<Unknown>";
        }
    }
}

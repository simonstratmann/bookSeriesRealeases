export interface PublicationDate {
  year: number;
  month: number;
  day: number;
}

export interface Publication {
  publicationDate: PublicationDate | null;
  expectedPublicationDate: PublicationDate | null;
}

export interface Book {
  number: number;
  title: string;
  publication: Publication | null;
  url: string;
  rating: number;
  ratings: number;
}

export interface BookSeries {
  title: string;
  author: string;
  goodReadsUrl: string;
  lastUpdate: string | null;
  books: Book[];
}

export interface BookWithSeries {
  seriesTitle: string;
  seriesAuthor: string;
  seriesUrl: string;
  bookNumber: number;
  totalBooks: number;
  title: string;
  bookUrl: string;
  rating: number;
  ratings: number;
  publicationDate: string | null;
  expectedPublicationDate: string | null;
}

export interface RecentBooksResponse {
  recentlyPublished: BookWithSeries[];
  upcoming: BookWithSeries[];
}

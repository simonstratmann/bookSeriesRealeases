import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookSeries, RecentBooksResponse } from '../models/book-series.model';

@Injectable({ providedIn: 'root' })
export class BookSeriesService {
  private readonly api = '/api';
  private http = inject(HttpClient);

  getSeries(): Observable<BookSeries[]> {
    return this.http.get<BookSeries[]>(`${this.api}/series`);
  }

  deleteSeries(index: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/series/${index}`);
  }

  addSeries(url: string): Observable<string> {
    return this.http.post(`${this.api}/series`, { url }, { responseType: 'text' });
  }

  triggerUpdate(): Observable<string> {
    return this.http.post(`${this.api}/update`, {}, { responseType: 'text' });
  }

  getUpdateStatus(): Observable<{ running: boolean }> {
    return this.http.get<{ running: boolean }>(`${this.api}/update/status`);
  }

  getRecentBooks(lastDays = 90, nextDays = 90): Observable<RecentBooksResponse> {
    return this.http.get<RecentBooksResponse>(
      `${this.api}/books/recent?lastDays=${lastDays}&nextDays=${nextDays}`
    );
  }
}

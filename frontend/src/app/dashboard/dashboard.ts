import { Component, OnInit, signal, inject } from '@angular/core';
import { BookSeriesService } from '../services/book-series.service';
import { BookWithSeries, RecentBooksResponse } from '../models/book-series.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  private service = inject(BookSeriesService);

  data = signal<RecentBooksResponse | null>(null);
  loading = signal(true);
  error = signal('');
  lastDays = signal(90);
  nextDays = signal(90);

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.service.getRecentBooks(this.lastDays(), this.nextDays()).subscribe({
      next: data => { this.data.set(data); this.loading.set(false); },
      error: err => {
        this.error.set('Could not reach the backend. Is the Spring Boot server running on port 8080?');
        this.loading.set(false);
      }
    });
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return 'Unknown';
    const d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  starsArray(rating: number): number[] {
    return Array.from({ length: 5 }, (_, i) => i);
  }

  starFill(index: number, rating: number): number {
    const diff = rating - index;
    if (diff >= 1) return 100;
    if (diff <= 0) return 0;
    return Math.round(diff * 100);
  }

  trackByTitle(_: number, item: BookWithSeries): string {
    return item.bookUrl;
  }
}

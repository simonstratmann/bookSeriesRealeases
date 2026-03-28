import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BookSeriesService } from '../services/book-series.service';
import { Book, BookSeries, PublicationDate } from '../models/book-series.model';

type SortCol = 'title' | 'author' | 'lastRelease' | 'nextRelease' | 'updated' | '';

@Component({
  selector: 'app-series-list',
  imports: [FormsModule],
  templateUrl: './series-list.html',
  styleUrl: './series-list.scss'
})
export class SeriesList implements OnInit {
  private service = inject(BookSeriesService);

  series = signal<BookSeries[]>([]);
  loading = signal(true);
  expanded = signal<Record<string, boolean>>({});
  sortCol = signal<SortCol>('');
  sortDir = signal<'asc' | 'desc'>('asc');
  newUrl = '';
  adding = signal(false);
  message = signal('');
  messageType = signal<'success' | 'error'>('success');

  sortedSeries = computed(() => {
    const col = this.sortCol();
    const dir = this.sortDir();
    if (!col) return this.series();
    return [...this.series()].sort((a, b) => {
      const aVal = this.sortValue(a, col);
      const bVal = this.sortValue(b, col);
      if (aVal < bVal) return dir === 'asc' ? -1 : 1;
      if (aVal > bVal) return dir === 'asc' ? 1 : -1;
      return 0;
    });
  });

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.service.getSeries().subscribe({
      next: s => { this.series.set(s); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  sort(col: SortCol) {
    if (this.sortCol() === col) {
      this.sortDir.update(d => d === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortCol.set(col);
      this.sortDir.set('asc');
    }
  }

  sortIcon(col: SortCol): string {
    if (this.sortCol() !== col) return '⇅';
    return this.sortDir() === 'asc' ? '↑' : '↓';
  }

  toggleExpand(url: string) {
    this.expanded.update(e => ({ ...e, [url]: !e[url] }));
  }

  isExpanded(url: string): boolean {
    return !!this.expanded()[url];
  }

  delete(index: number, title: string, event: Event) {
    event.stopPropagation();
    if (!confirm(`Remove "${title}" from your list?`)) return;
    // index in sortedSeries may differ from index in series(); find by url
    const s = this.sortedSeries()[index];
    const realIndex = this.series().findIndex(x => x.goodReadsUrl === s.goodReadsUrl);
    this.service.deleteSeries(realIndex).subscribe({
      next: () => { this.load(); this.showMessage(`Removed "${title}"`, 'success'); },
      error: () => this.showMessage('Failed to remove series', 'error')
    });
  }

  add() {
    const url = this.newUrl.trim();
    if (!url) return;
    this.adding.set(true);
    this.service.addSeries(url).subscribe({
      next: msg => {
        this.showMessage(msg, 'success');
        this.newUrl = '';
        this.adding.set(false);
      },
      error: err => {
        this.showMessage(err.error || 'Failed to add series', 'error');
        this.adding.set(false);
      }
    });
  }

  releasedCount(s: BookSeries): number {
    return s.books.filter(b => b.publication?.publicationDate).length;
  }

  lastRelease(s: BookSeries): string {
    const ts = this.lastReleaseTs(s);
    return ts === 0 ? '—' : this.formatShortDate(new Date(ts));
  }

  nextRelease(s: BookSeries): string {
    const ts = this.nextReleaseTs(s);
    return ts === Infinity ? '—' : this.formatShortDate(new Date(ts));
  }

  bookDate(book: Book): string {
    if (!book.publication) return '—';
    if (book.publication.publicationDate) {
      const d = this.pubDateToDate(book.publication.publicationDate);
      return d ? this.formatShortDate(d) : '—';
    }
    if (book.publication.expectedPublicationDate) {
      const d = this.pubDateToDate(book.publication.expectedPublicationDate);
      return d ? 'Expected ' + this.formatShortDate(d) : 'Expected (unknown)';
    }
    return '—';
  }

  isExpected(book: Book): boolean {
    return !!book.publication?.expectedPublicationDate && !book.publication?.publicationDate;
  }

  formatDate(iso: string | null): string {
    if (!iso) return '—';
    return this.formatShortDate(new Date(iso));
  }

  private sortValue(s: BookSeries, col: SortCol): string | number {
    switch (col) {
      case 'title':       return s.title.toLowerCase();
      case 'author':      return s.author.toLowerCase();
      case 'lastRelease': return this.lastReleaseTs(s);
      case 'nextRelease': return this.nextReleaseTs(s);
      case 'updated':     return s.lastUpdate ? new Date(s.lastUpdate).getTime() : 0;
      default:            return '';
    }
  }

  private lastReleaseTs(s: BookSeries): number {
    const dates = s.books
      .filter(b => b.publication?.publicationDate)
      .map(b => this.pubDateToDate(b.publication!.publicationDate!))
      .filter((d): d is Date => d !== null)
      .map(d => d.getTime());
    return dates.length ? Math.max(...dates) : 0;
  }

  private nextReleaseTs(s: BookSeries): number {
    const today = new Date(); today.setHours(0, 0, 0, 0);
    const dates = s.books
      .filter(b => b.publication?.expectedPublicationDate)
      .map(b => this.pubDateToDate(b.publication!.expectedPublicationDate!))
      .filter((d): d is Date => d !== null && d >= today)
      .map(d => d.getTime());
    return dates.length ? Math.min(...dates) : Infinity;
  }

  private formatShortDate(d: Date): string {
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  private pubDateToDate(pd: PublicationDate): Date | null {
    if (!pd.year || !pd.month || !pd.day) return null;
    return new Date(pd.year, pd.month - 1, pd.day);
  }

  private showMessage(msg: string, type: 'success' | 'error') {
    this.message.set(msg);
    this.messageType.set(type);
    setTimeout(() => this.message.set(''), 5000);
  }

  trackBySeries(_: number, s: BookSeries): string {
    return s.goodReadsUrl;
  }
}

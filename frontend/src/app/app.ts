import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { BookSeriesService } from './services/book-series.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit, OnDestroy {
  private service = inject(BookSeriesService);

  updating = signal(false);
  updateMessage = signal('');

  private pollInterval: ReturnType<typeof setInterval> | null = null;

  ngOnInit() {
    this.checkUpdateStatus();
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  triggerUpdate() {
    this.service.triggerUpdate().subscribe({
      next: msg => {
        this.updateMessage.set(msg);
        this.updating.set(true);
        this.startPolling();
      },
      error: () => this.updateMessage.set('Failed to start update')
    });
  }

  private checkUpdateStatus() {
    this.service.getUpdateStatus().subscribe(status => {
      this.updating.set(status.running);
      if (status.running) {
        this.startPolling();
      }
    });
  }

  private startPolling() {
    this.stopPolling();
    this.pollInterval = setInterval(() => {
      this.service.getUpdateStatus().subscribe(status => {
        this.updating.set(status.running);
        if (!status.running) {
          this.stopPolling();
          this.updateMessage.set('');
        }
      });
    }, 3000);
  }

  private stopPolling() {
    if (this.pollInterval !== null) {
      clearInterval(this.pollInterval);
      this.pollInterval = null;
    }
  }
}

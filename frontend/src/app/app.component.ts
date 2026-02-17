import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { BackendService } from './services/backend.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  status = 'Checking backend status...';

  constructor(private readonly backendService: BackendService) {}

  ngOnInit(): void {
    this.loadStatus();
  }

  loadStatus(): void {
    this.backendService.getHealth().subscribe({
      next: (response) => {
        this.status = `Backend status: ${response.status}`;
      },
      error: () => {
        this.status = 'Backend status: DOWN';
      }
    });
  }
}

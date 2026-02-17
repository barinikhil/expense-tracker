import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackendService } from './services/backend.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  message = 'Loading backend message...';
  status = 'Checking backend status...';

  constructor(private readonly backendService: BackendService) {}

  ngOnInit(): void {
    this.backendService.getHealth().subscribe({
      next: (response) => {
        this.status = `Backend status: ${response.status}`;
      },
      error: () => {
        this.status = 'Backend status: DOWN';
      }
    });

    this.backendService.getMessage().subscribe({
      next: (response) => {
        this.message = response.message;
      },
      error: () => {
        this.message = 'Unable to reach Spring Boot backend.';
      }
    });
  }
}

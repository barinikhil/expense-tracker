import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SessionExpiredModalService {
  private readonly visibleSubject = new BehaviorSubject<boolean>(false);
  private readonly messageSubject = new BehaviorSubject<string>('Your session has expired. Please log in again.');

  readonly visible$ = this.visibleSubject.asObservable();
  readonly message$ = this.messageSubject.asObservable();

  get isVisible(): boolean {
    return this.visibleSubject.value;
  }

  show(message = 'Your session has expired. Please log in again.'): void {
    this.messageSubject.next(message);
    this.visibleSubject.next(true);
  }

  hide(): void {
    this.visibleSubject.next(false);
  }
}

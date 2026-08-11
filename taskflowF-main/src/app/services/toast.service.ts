import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastKind = 'info' | 'success' | 'error';

export interface ToastMessage {
  id: number;
  kind: ToastKind;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  #nextId = 1;
  #messages = new BehaviorSubject<ToastMessage[]>([]);
  messages$ = this.#messages.asObservable();

  show(message: string, kind: ToastKind = 'info', ms = 3000) {
    const item: ToastMessage = { id: this.#nextId++, kind, message };
    this.#messages.next([...this.#messages.value, item]);
    setTimeout(() => this.dismiss(item.id), ms);
  }

  dismiss(id: number) {
    this.#messages.next(this.#messages.value.filter(m => m.id !== id));
  }
}


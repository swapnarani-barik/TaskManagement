import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-stack" aria-live="polite" aria-relevant="additions removals">
      <div *ngFor="let t of (toast.messages$ | async)" class="toast" [class]="t.kind">
        <span class="toast__msg">{{ t.message }}</span>
        <button type="button" class="toast__btn" (click)="toast.dismiss(t.id)">×</button>
      </div>
    </div>
  `,
  styles: [
    `
      .toast-stack{position:fixed;top:16px;right:16px;display:flex;flex-direction:column;gap:10px;z-index:9999}
      .toast{min-width:260px;max-width:420px;padding:12px 12px;border-radius:12px;border:1px solid rgba(255,255,255,.12);background:rgba(20,20,20,.95);color:#fff;box-shadow:0 10px 30px rgba(0,0,0,.35);display:flex;align-items:center;gap:12px}
      .toast.info{border-color:rgba(99,102,241,.35)}
      .toast.success{border-color:rgba(34,197,94,.35)}
      .toast.error{border-color:rgba(239,68,68,.35)}
      .toast__msg{flex:1;font-size:13px;line-height:1.3}
      .toast__btn{border:0;background:transparent;color:#fff;font-size:18px;line-height:1;cursor:pointer;opacity:.8}
      .toast__btn:hover{opacity:1}
    `
  ]
})
export class ToastComponent {
  toast = inject(ToastService);
}


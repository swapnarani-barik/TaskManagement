import { Injectable } from '@angular/core';
import type { ThemeMode } from '../models/settings.model';

const STORAGE_KEY = 'taskflow_theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  getSavedTheme(): ThemeMode {
    const raw = (localStorage.getItem(STORAGE_KEY) || 'LIGHT').toUpperCase();
    if (raw === 'DARK' || raw === 'SYSTEM' || raw === 'LIGHT') {
      return raw;
    }
    return 'LIGHT';
  }

  setTheme(theme: ThemeMode): void {
    localStorage.setItem(STORAGE_KEY, theme);
    this.applyTheme(theme);
  }

  applySavedTheme(): void {
    this.applyTheme(this.getSavedTheme());
  }

  applyTheme(theme: ThemeMode): void {
    const html = document.documentElement;
    if (theme === 'DARK') {
      html.setAttribute('data-theme', 'dark');
      return;
    }
    if (theme === 'SYSTEM') {
      const isDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
      html.setAttribute('data-theme', isDark ? 'dark' : 'light');
      return;
    }
    html.setAttribute('data-theme', 'light');
  }
}

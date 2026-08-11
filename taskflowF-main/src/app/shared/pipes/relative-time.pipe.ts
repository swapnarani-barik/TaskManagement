import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'relativeTime',
  standalone: true
})
export class RelativeTimePipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    if (!value) return '';

    const date = value instanceof Date ? value : new Date(value);
    const diffMs = Date.now() - date.getTime();
    const future = diffMs < 0;
    const absMs = Math.abs(diffMs);
    const minute = 60 * 1000;
    const hour = 60 * minute;
    const day = 24 * hour;

    const asText = (n: number, unit: string) => {
      const label = `${n}${unit}`;
      return future ? `in ${label}` : `${label} ago`;
    };

    if (absMs < minute) return future ? 'in moments' : 'just now';
    if (absMs < hour) return asText(Math.floor(absMs / minute), 'm');
    if (absMs < day) return asText(Math.floor(absMs / hour), 'h');
    return asText(Math.floor(absMs / day), 'd');
  }
}

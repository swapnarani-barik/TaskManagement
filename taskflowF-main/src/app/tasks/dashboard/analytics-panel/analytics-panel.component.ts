import {
  AfterViewInit, Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskSummaryResponse } from '../../../models/task.model';
import Chart from 'chart.js/auto';
// declare const Chart: any;

@Component({
  standalone: true,
  selector: 'app-analytics-panel',
  imports: [CommonModule],
  templateUrl: './analytics-panel.component.html',
  styleUrls: ['./analytics-panel.component.css']
})
export class AnalyticsPanelComponent implements OnChanges, AfterViewInit, OnDestroy {
  @Input() summary: TaskSummaryResponse | null = null;
  @Input() loading = false;
  @Input() error = '';
  @Output() hide = new EventEmitter<void>();

  @ViewChild('statusChart') statusChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('priorityChart') priorityChartRef!: ElementRef<HTMLCanvasElement>;

  private statusChart: any;
  private priorityChart: any;
  private viewReady = false;

  get hasData(): boolean {
    return !!this.summary && this.summary.totalTasks > 0;
  }

  ngAfterViewInit(): void {
    this.viewReady = true;
    setTimeout(() => this.renderCharts());
  }

  ngOnChanges(_changes: SimpleChanges): void {
    setTimeout(() => this.renderCharts());
  }

  ngOnDestroy(): void {
    this.destroyCharts();
  }

  private destroyCharts(): void {
    if (this.statusChart) {
      this.statusChart.destroy();
      this.statusChart = null;
    }
    if (this.priorityChart) {
      this.priorityChart.destroy();
      this.priorityChart = null;
    }
  }

  private renderCharts(): void {
    if (!this.viewReady || !this.hasData || typeof Chart === 'undefined') {
      this.destroyCharts();
      return;
    }

    if (!this.statusChartRef || !this.priorityChartRef || !this.summary) {
      // Canvases are inside an *ngIf block; retry after view update.
      setTimeout(() => this.renderCharts());
      return;
    }

    this.destroyCharts();

    const centerTextPlugin = {
      id: 'centerText',
      afterDraw: (chart: any) => {
        if (!this.summary) return;
        const { ctx } = chart;
        const meta = chart.getDatasetMeta(0);
        const arc = meta?.data?.[0];
        if (!arc) return;
        const x = arc.x;
        const y = arc.y;
        ctx.save();
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillStyle = '#1f2937';
        ctx.font = '700 28px Segoe UI';
        ctx.fillText(`${this.summary.totalTasks}`, x, y - 8);
        ctx.font = '600 13px Segoe UI';
        ctx.fillStyle = '#64748b';
        ctx.fillText('tasks', x, y + 14);
        ctx.restore();
      }
    };
    
  const statusCtx = this.statusChartRef.nativeElement.getContext('2d');
    const priorityCtx = this.priorityChartRef.nativeElement.getContext('2d');
    if (!statusCtx || !priorityCtx) {
      setTimeout(() => this.renderCharts());
      return;
    }


    // this.statusChart = new Chart(this.statusChartRef.nativeElement.getContext('2d'), {
    this.statusChart = new Chart(statusCtx, {
      type: 'doughnut',
      data: {
        labels: ['To-Do', 'In Progress', 'Done'],
        datasets: [{
          data: [this.summary.byStatus.todo, this.summary.byStatus.inProgress, this.summary.byStatus.done],
          backgroundColor: ['#2563eb', '#f59e0b', '#22c55e'],
          borderWidth: 0
        }],
      },
      options: {
        maintainAspectRatio: false,
        cutout: '62%',
        plugins: {
          legend: { position: 'right', labels: { boxWidth: 12, color: '#334155', font: { weight: 600 } } }
        }
      },
      plugins: [centerTextPlugin]
    });

    // this.priorityChart = new Chart(this.priorityChartRef.nativeElement.getContext('2d'), {
     this.priorityChart = new Chart(priorityCtx, {
      type: 'bar',
      data: {
        labels: ['HIGH', 'MEDIUM', 'LOW'],
        datasets: [{
          data: [this.summary.byPriority.high, this.summary.byPriority.medium, this.summary.byPriority.low],
          backgroundColor: ['#ef4444', '#d97706', '#16a34a'],
          borderRadius: 8,
          borderSkipped: false
        }],
      },
      options: {
        maintainAspectRatio: false,
        scales: {
          y: { beginAtZero: true, ticks: { precision: 0 } }
        },
        plugins: { legend: { display: false } }
      }
    });
  }
}
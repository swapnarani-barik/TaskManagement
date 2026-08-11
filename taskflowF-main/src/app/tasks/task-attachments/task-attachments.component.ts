import { Component, Input, OnChanges, SimpleChanges, inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AttachmentService, AttachmentListDTO } from '../../services/attachment.service';
import { AuthService } from '../../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-task-attachments',
  imports: [CommonModule],
  templateUrl: './task-attachments.component.html',
  styleUrls: ['./task-attachments.component.css']
})
export class TaskAttachmentsComponent implements OnChanges {
  @Input() taskId!: number;
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  #attachmentService = inject(AttachmentService);
  #auth = inject(AuthService);

  attachments: AttachmentListDTO[] = [];
  loading = false;
  uploading = false;
  uploadingName = '';
  error = '';

  readonly MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
  readonly ACCEPT_EXTENSIONS = '.jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.xls,.xlsx,.txt,.zip';
  readonly VALID_EXTS = new Set(['jpg', 'jpeg', 'png', 'gif', 'pdf', 'doc', 'docx', 'xls', 'xlsx', 'txt', 'zip']);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['taskId'] && this.taskId) {
      this.loadAttachments();
    }
  }

  loadAttachments(): void {
    this.loading = true;
    this.error = '';
    this.#attachmentService.getAttachments(this.taskId).subscribe({
      next: (list) => { this.attachments = list; this.loading = false; },
      error: () => { this.error = 'Failed to load attachments'; this.loading = false; }
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) this.handleFile(file);
  }

  onDragOver(event: DragEvent): void {
    if (this.reachedMax || this.isViewer()) return;
    event.preventDefault();
    event.stopPropagation();
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    if (this.reachedMax || this.isViewer()) return;
    const file = event.dataTransfer?.files[0];
    if (file) this.handleFile(file);
  }

  handleFile(file: File): void {
    this.error = '';

    if (this.reachedMax) {
      this.error = 'Maximum 5 files reached.';
      return;
    }

    if (file.size > this.MAX_FILE_SIZE) {
      this.error = 'File exceeds 5 MB limit.';
      return;
    }

    const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
    if (!this.VALID_EXTS.has(ext)) {
      this.error = 'File type not allowed.';
      return;
    }

    this.uploading = true;
    this.uploadingName = file.name;
    this.#attachmentService.uploadAttachment(this.taskId, file).subscribe({
      next: (att) => {
        this.attachments.push(att);
        this.attachments = this.attachments.slice(0, 5);
        this.uploadingName = '';
        if(this.fileInput) this.fileInput.nativeElement.value = '';
      },
      error: (err) => {
        this.error = typeof err?.error === 'string' ? err.error : (err?.error?.message || 'Failed to upload attachment');
        this.uploadingName = '';
        if(this.fileInput) this.fileInput.nativeElement.value = '';
      },
      complete: () => { this.uploading = false; }
    });
  }

  deleteAttachment(id: number): void {
    this.error = '';
    this.#attachmentService.deleteAttachment(id).subscribe({
      next: () => {
        this.attachments = this.attachments.filter(a => a.id !== id);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete attachment';
      }
    });
  }

  download(id: number): void {
    this.error = '';
    this.#attachmentService.downloadAttachment(id).subscribe({
        next: (res) => {
          const blob = res.body;
          if (!blob) {
            this.error = 'Download failed.';
            return;
          }

          const contentDisposition = res.headers.get('content-disposition') ?? '';
          const filename = this.parseFilename(contentDisposition) || 'attachment';

          const objectUrl = URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = objectUrl;
          link.download = filename;
          document.body.appendChild(link);
          link.click();
          link.remove();
          URL.revokeObjectURL(objectUrl);
        },
        error: (err) => {
          this.error = err?.error?.message || 'Failed to download attachment';
        }
      });
  }

  formatBytes(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }

  getFileTypeIcon(name: string): string {
    const ext = name.split('.').pop()?.toLowerCase() || '';
    if (ext === 'pdf') return 'PDF';
    if (['jpg', 'jpeg', 'png', 'gif'].includes(ext)) return 'IMG';
    if (['doc', 'docx'].includes(ext)) return 'DOC';
    if (ext === 'zip') return 'ZIP';
    if (['xls', 'xlsx'].includes(ext)) return 'XLS';
    return 'TXT';
  }

  canDelete(uploaderId: number): boolean {
    const cuId = this.#auth.getCurrentUserId();
    const role = this.#auth.getCurrentUser()?.role ?? 'MEMBER';
    return cuId === uploaderId || role === 'ADMIN' || role === 'MANAGER';
  }

  isViewer(): boolean {
    return (this.#auth.getCurrentUser()?.role ?? 'MEMBER') === 'VIEWER';
  }

  get reachedMax(): boolean {
    return this.attachments.length >= 5;
  }

  formatUploadDate(raw: string): string {
    const d = new Date(raw);
    if (Number.isNaN(d.getTime())) return '-';
    return d.toLocaleDateString(undefined, { month: 'short', day: '2-digit' });
  }

  private parseFilename(contentDisposition: string): string | null {
    const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8Match?.[1]) {
      return decodeURIComponent(utf8Match[1]);
    }

    const simpleMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/i);
    if (simpleMatch?.[1]) {
      return simpleMatch[1];
    }
    return null;
  }
}

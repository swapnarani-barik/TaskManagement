import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AttachmentListDTO {
  id: number;
  taskId: number;
  uploaderId: number;
  uploaderName: string;
  originalName: string;
  mimeType: string;
  fileSizeBytes: number;
  uploadedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AttachmentService {
  #http = inject(HttpClient);
  #base = environment.apiBaseUrl;

  uploadAttachment(taskId: number, file: File): Observable<AttachmentListDTO> {
    const formData = new FormData();
    formData.append('file', file);

    return this.#http.post<AttachmentListDTO>(
      `${this.#base}/api/tasks/${taskId}/attachments`,
      formData
    );
  }

  getAttachments(taskId: number): Observable<AttachmentListDTO[]> {
    return this.#http.get<AttachmentListDTO[]>(
      `${this.#base}/api/tasks/${taskId}/attachments`
    );
  }

  deleteAttachment(attachmentId: number): Observable<void> {
    return this.#http.delete<void>(
      `${this.#base}/api/attachments/${attachmentId}`
    );
  }

  downloadAttachment(attachmentId: number): Observable<HttpResponse<Blob>> {
    return this.#http.get(`${this.#base}/api/attachments/${attachmentId}/download`, {
      observe: 'response',
      responseType: 'blob'
    });
  }
}

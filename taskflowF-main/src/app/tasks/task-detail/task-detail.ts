// import { Component, OnInit, inject } from '@angular/core';
// import { ActivatedRoute } from '@angular/router';
// import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
// import { CommonModule, DatePipe } from '@angular/common';
// import { TaskService } from '../../services/task.service';
// import { CommentResponse } from '../../models/comment.model';
// import { CommentSection } from '../../task-detail/comment-section/comment-section';

// @Component({
//   standalone: true,
//   selector: 'app-task-detail',
//   templateUrl: './task-detail.html',
//   styleUrls: ['./task-detail.css'],
//   imports: [CommonModule, ReactiveFormsModule, DatePipe,CommentSection]
// })
// export class TaskDetail implements OnInit {
//   taskId!: number;
//   comments: CommentResponse[] = [];
//   posting = false;

//   private fb = inject(FormBuilder);
//   private route = inject(ActivatedRoute);
//   private tasks = inject(TaskService);

//   // ✅ Add this getter
//   get currentUserId(): number | null {
//     try {
//       const raw = localStorage.getItem('user');
//       if (!raw) return null;
//       const u = JSON.parse(raw);
//       // your login saves { id, email, fullName }
//       const idNum = typeof u?.id === 'number' ? u.id : Number(u?.id);
//       return Number.isFinite(idNum) ? idNum : null;
//     } catch {
//       return null;
//     }
//   }

//   commentForm = this.fb.group({ text: ['', Validators.required] });

//   ngOnInit(): void {
//     this.taskId = Number(this.route.snapshot.paramMap.get('id'));
//     this.loadComments();
//   }

//   loadComments() {
//     this.tasks.getComments(this.taskId).subscribe(res => (this.comments = res));
//   }

//   postComment() {
//     const text = (this.commentForm.value.text ?? '').trim();
//     if (!text || this.posting) return;
//     this.posting = true;
//     this.tasks.postComment(this.taskId, text).subscribe({
//       next: res => {
//         this.comments.push(res);
//         this.commentForm.reset();
//       },
//       error: err => console.error('Failed to post comment', err),
//       complete: () => (this.posting = false)
//     });
//   }

//   deleteComment(id: number) {
//     this.tasks.deleteComment(id).subscribe(() => {
//       this.comments = this.comments.filter(c => c.id !== id);
//     });
//   }
// }
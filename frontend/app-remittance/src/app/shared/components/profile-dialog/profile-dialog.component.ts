import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { UserSession, AuthService } from '../../../core/services/auth/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Router } from '@angular/router';
import { ROUTER_PATHS } from '../../../core/constants/app.constants';

@Component({
  selector: 'app-profile-dialog',
  template: `
    <button mat-icon-button class="close-btn" (click)="dialogRef.close()">
      <mat-icon>close</mat-icon>
    </button>
    <div class="profile-header">
      <div class="profile-avatar">{{ (data.user?.username || 'U').substring(0, 2).toUpperCase() }}</div>
      <h2>{{ data.user?.username | titlecase }}</h2>
      <p class="role">{{ data.user?.role === 'admin' ? 'Administrador del Sistema' : 'Cliente' }}</p>
    </div>
    
    <mat-dialog-content>
      <div class="info-row">
        <mat-icon>email</mat-icon>
        <span>{{ data.user?.username }}@remit.app</span>
      </div>
      <div class="info-row" *ngIf="data.user?.clientId">
        <mat-icon>badge</mat-icon>
        <span>ID de Cliente: {{ data.user?.clientId }}</span>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="center" style="margin-top: 20px;">
      <button mat-raised-button color="warn" (click)="onLogout()">
        <mat-icon>exit_to_app</mat-icon> Cerrar Sesión
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .close-btn {
      position: absolute;
      top: 10px;
      right: 10px;
      color: var(--text-color-secondary);
    }
    .profile-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 0;
      border-bottom: 1px solid var(--border-color);
      margin-bottom: 20px;
    }
    .profile-avatar {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background-color: var(--primary-color);
      color: #000;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 32px;
      font-weight: 700;
      margin-bottom: 15px;
    }
    h2 {
      margin: 0;
      color: var(--text-color);
      font-weight: 600;
    }
    .role {
      color: var(--text-color-secondary);
      margin: 5px 0 0 0;
      font-size: 14px;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
    .info-row {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 0;
      color: var(--text-color);
    }
    .info-row mat-icon {
      color: var(--text-color-secondary);
    }
    .mat-mdc-dialog-surface {
      background-color: var(--surface-color) !important;
    }
  `]
})
export class ProfileDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ProfileDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user: UserSession },
    private notificationService: NotificationService,
    private authService: AuthService,
    private router: Router
  ) {}

  onLogout(): void {
    this.notificationService.confirm('Cerrar Sesión', '¿Estás seguro que deseas cerrar tu sesión?').subscribe(res => {
      if (res) {
        this.dialogRef.close();
        this.authService.logout();
        this.router.navigate([ROUTER_PATHS.LOGIN]);
        this.notificationService.showInfo('Sesión cerrada correctamente');
      }
    });
  }
}

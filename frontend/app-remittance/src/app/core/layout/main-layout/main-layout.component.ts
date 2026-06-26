import { Component, OnInit } from '@angular/core';
import { AuthService, UserSession } from '../../services/auth/auth.service';
import { Router } from '@angular/router';
import { ROUTER_PATHS } from '../../constants/app.constants';
import { ProfileDialogComponent } from '../../../shared/components/profile-dialog/profile-dialog.component';

import { ThemeService } from '../../services/theme/theme.service';
import { ResponsiveService } from '../../services/responsive/responsive.service';
import { Observable } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css']
})
export class MainLayoutComponent implements OnInit {
  user: UserSession | null = null;
  isAdmin: boolean = false;
  isMobile$: Observable<boolean>;
  isDarkTheme$: Observable<boolean>;

  constructor(
    private authService: AuthService, 
    private router: Router,
    private responsiveService: ResponsiveService,
    private themeService: ThemeService,
    private dialog: MatDialog
  ) {
    this.isMobile$ = this.responsiveService.isMobile$;
    this.isDarkTheme$ = this.themeService.darkMode$;
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      this.user = user;
      this.isAdmin = this.authService.isAdmin();
    });
  }

  openProfile(): void {
    if (this.user) {
      this.dialog.open(ProfileDialogComponent, {
        width: '400px',
        data: { user: this.user },
        panelClass: 'profile-dialog-container',
        autoFocus: false
      });
    }
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  closeSidenav(sidenav: any): void {
    this.isMobile$.subscribe(isMobile => {
      if (isMobile) {
        sidenav.close();
      }
    }).unsubscribe();
  }

  createMovement(): void {
    import('../../../modules/movimientos/infrastructure/ui/transaction-form/transaction-form.component').then(m => {
      const isMobile = window.innerWidth < 900;
      const dialogRef = this.dialog.open(m.TransactionFormComponent, {
        width: isMobile ? '100vw' : '500px',
        maxWidth: isMobile ? '100vw' : '80vw',
        height: isMobile ? '100vh' : 'auto',
        autoFocus: false
      });
      dialogRef.afterClosed().subscribe(res => {
        if (res) window.location.reload();
      });
    });
  }
}

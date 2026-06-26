import { Component, OnInit } from '@angular/core';
import { ClientHttpService, Client } from './infrastructure/adapters/client-http.service';
import { AuthService } from '@core/services/auth/auth.service';
import { ResponsiveService } from '@core/services/responsive/responsive.service';
import { TABLE_COLUMNS, UI_MESSAGES } from '@core/constants/app.constants';
import { MatDialog } from '@angular/material/dialog';
import { ClientFormComponent } from './infrastructure/ui/client-form/client-form.component';
import { AccountHttpService } from '../cuentas/infrastructure/adapters/account-http.service';
import { MovementHttpService } from '../movimientos/infrastructure/adapters/movement-http.service';
import { NotificationService } from '@core/services/notification.service';

@Component({
  selector: 'app-clientes',
  templateUrl: './clientes.component.html',
  styleUrls: ['./clientes.component.css']
})
export class ClientesComponent implements OnInit {

  isAdmin = false;
  clients: Client[] = [];
  displayedColumns: string[] = TABLE_COLUMNS.CLIENTS;
  
  // For standard user
  myProfile: Client | null = null;
  totalBalance: number = 0;
  recentMovements: any[] = [];
  isMobile = false;
  isLoading = true;

  constructor(
    private clientHttpService: ClientHttpService,
    private authService: AuthService,
    private dialog: MatDialog,
    private responsiveService: ResponsiveService,
    private accountHttpService: AccountHttpService,
    private movementHttpService: MovementHttpService,
    private notificationService: NotificationService
  ) {
    this.responsiveService.isMobile$.subscribe(val => this.isMobile = val);
  }

  isScrollingDown = false;
  private lastScrollTop = 0;
  private scrollListener: any;

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    if (this.isAdmin) {
      this.loadAllClients();
    } else {
      this.loadMyProfile();
    }
  }

  ngAfterViewInit() {
    const mainContent = document.querySelector('.main-content');
    if (mainContent) {
      this.scrollListener = (event: Event) => {
        const target = event.target as HTMLElement;
        const currentScrollTop = target.scrollTop;
        if (currentScrollTop > this.lastScrollTop && currentScrollTop > 30) {
          this.isScrollingDown = true;
        } else if (currentScrollTop < this.lastScrollTop) {
          this.isScrollingDown = false;
        }
        this.lastScrollTop = currentScrollTop;
      };
      mainContent.addEventListener('scroll', this.scrollListener);
    }
  }

  ngOnDestroy() {
    const mainContent = document.querySelector('.main-content');
    if (mainContent && this.scrollListener) {
      mainContent.removeEventListener('scroll', this.scrollListener);
    }
  }

  loadAllClients() {
    this.isLoading = true;
    this.clientHttpService.getAllClients().subscribe({
      next: res => {
        this.clients = res;
        this.isLoading = false;
      },
      error: err => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  loadMyProfile() {
    this.isLoading = true;
    const user = this.authService.currentUserValue;
    if (user?.clientId) {
      this.clientHttpService.getClientById(user.clientId).subscribe({
        next: res => {
          this.myProfile = res;
          
          // Fetch additional dashboard data
          this.accountHttpService.getAllAccounts(user.clientId).subscribe({
            next: accounts => {
              // Initial fallback calculation
              this.totalBalance = accounts.reduce((sum, acc) => sum + (acc.initialBalance || 0), 0);

              // Fetch movements to get the real current balance
              this.movementHttpService.getMovementsByClientId(user.clientId).subscribe({
                next: movements => {
                  this.recentMovements = movements.slice(0, 3);
                  
                  // Calculate true balance based on latest movement per account
                  let trueBalance = 0;
                  accounts.forEach(acc => {
                    const accMovements = movements.filter(m => m.accountNumber === acc.accountNumber);
                    if (accMovements.length > 0) {
                      // Find the latest movement by ID (assuming higher ID = more recent)
                      const latestMov = accMovements.reduce((prev, current) => 
                        (prev.movementId! > current.movementId!) ? prev : current
                      );
                      trueBalance += (latestMov.balance || 0);
                    } else {
                      trueBalance += (acc.initialBalance || 0);
                    }
                  });
                  this.totalBalance = trueBalance;
                },
                error: () => {
                  this.recentMovements = [];
                }
              });
            },
            error: () => this.totalBalance = 0
          });

          this.isLoading = false;
        },
        error: err => {
          console.error(err);
          this.isLoading = false;
        }
      });
    } else {
      this.isLoading = false;
    }
  }

  deleteClient(id: number) {
    this.notificationService.confirm('Inactivar Cliente', '¿Está seguro de inactivar este cliente? (Inactivación lógica)').subscribe(result => {
      if(result) {
        this.clientHttpService.patchClientStatus(id, false).subscribe({
          next: () => {
            this.notificationService.showSuccess('Cliente inactivado exitosamente');
            this.loadAllClients();
          },
          error: err => {
            console.error(err);
            this.notificationService.showError('Error al inactivar el cliente');
          }
        });
      }
    });
  }

  reactivateClient(id: number) {
    this.notificationService.confirm('Reactivar Cliente', '¿Está seguro de reactivar este cliente?').subscribe(result => {
      if(result) {
        this.clientHttpService.patchClientStatus(id, true).subscribe({
          next: () => {
            this.notificationService.showSuccess('Cliente reactivado exitosamente');
            this.loadAllClients();
          },
          error: err => {
            console.error(err);
            this.notificationService.showError('Error al reactivar el cliente');
          }
        });
      }
    });
  }

  editClient(client: Client) {
    const dialogRef = this.dialog.open(ClientFormComponent, {
      width: this.isMobile ? '100vw' : '600px',
      maxWidth: this.isMobile ? '100vw' : '80vw',
      height: this.isMobile ? '100vh' : 'auto',
      data: client,
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe({
      next: result => {
        if (result) {
          if (this.isAdmin) this.loadAllClients();
          else this.loadMyProfile();
        }
      }
    });
  }

  createClient() {
    const dialogRef = this.dialog.open(ClientFormComponent, {
      width: this.isMobile ? '100vw' : '600px',
      maxWidth: this.isMobile ? '100vw' : '80vw',
      height: this.isMobile ? '100vh' : 'auto',
      data: {},
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe({
      next: result => {
        if (result) {
          this.loadAllClients();
        }
      }
    });
  }
}


import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { AccountHttpService, Account } from './infrastructure/adapters/account-http.service';
import { AuthService } from '@core/services/auth/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { AccountFormComponent } from './infrastructure/ui/account-form/account-form.component';
import { MovementHttpService } from '../movimientos/infrastructure/adapters/movement-http.service';

@Component({
  selector: 'app-cuentas',
  templateUrl: './cuentas.component.html',
  styleUrls: ['./cuentas.component.css']
})
export class CuentasComponent implements OnInit, OnDestroy, AfterViewInit {

  isAdmin = false;
  accounts: Account[] = [];
  displayedColumns: string[] = ['accountId', 'clientName', 'accountNumber', 'accountType', 'initialBalance', 'status'];
  isLoading = true;
  totalAccounts: number = 0;
  totalBalance: number = 0;
  activationRate: number = 0;

  constructor(
    private accountHttpService: AccountHttpService,
    private authService: AuthService,
    private dialog: MatDialog,
    private movementHttpService: MovementHttpService
  ) { }

  isScrollingDown = false;
  private lastScrollTop = 0;
  private scrollListener: any;

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    this.loadAccounts();
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

  createAccount() {
    const isMobile = window.innerWidth < 900;
    const dialogRef = this.dialog.open(AccountFormComponent, {
      width: isMobile ? '100vw' : '450px',
      maxWidth: isMobile ? '100vw' : '80vw',
      height: isMobile ? '100vh' : 'auto',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadAccounts();
      }
    });
  }

  loadAccounts() {
    this.isLoading = true;
    const user = this.authService.currentUserValue;
    const clientId = this.isAdmin ? undefined : user?.clientId;

    this.accountHttpService.getAllAccounts(clientId).subscribe({
      next: res => {
        this.accounts = res;
        this.calculateKPIs();
        this.isLoading = false;
      },
      error: err => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  calculateKPIs() {
    this.totalAccounts = this.accounts.length;
    
    // Initial fallback calculation
    this.totalBalance = this.accounts.reduce((sum, acc) => sum + (Number(acc.initialBalance) || 0), 0);

    // Fetch all movements to calculate true current balance for all accounts
    this.movementHttpService.getAllMovements().subscribe({
      next: movements => {
        let trueBalance = 0;
        this.accounts.forEach(acc => {
          const accMovements = movements.filter(m => m.accountNumber === acc.accountNumber);
          if (accMovements.length > 0) {
            const latestMov = accMovements.reduce((prev, current) => 
              (prev.movementId! > current.movementId!) ? prev : current
            );
            trueBalance += (latestMov.balance || 0);
            acc.initialBalance = latestMov.balance;
          } else {
            trueBalance += (acc.initialBalance || 0);
          }
        });
        this.totalBalance = trueBalance;
        // Trigger change detection for MatTable
        this.accounts = [...this.accounts];
      },
      error: () => console.error('Failed to load movements for true balance calculation')
    });
    const activeCount = this.accounts.filter(a => a.status === true || String(a.status) === 'true').length;
    this.activationRate = this.totalAccounts > 0 ? (activeCount / this.totalAccounts) * 100 : 0;
  }

  maskAccount(accountNumber?: string): string {
    if (!accountNumber || accountNumber.length < 2) return accountNumber || '';
    if (accountNumber.length <= 4) return accountNumber[0] + 'X'.repeat(accountNumber.length - 2) + accountNumber[accountNumber.length - 1];
    return accountNumber[0] + 'X'.repeat(accountNumber.length - 2) + accountNumber[accountNumber.length - 1];
  }
}

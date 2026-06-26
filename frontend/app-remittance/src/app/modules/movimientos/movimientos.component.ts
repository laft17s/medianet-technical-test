import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { MovementHttpService, Movement } from './infrastructure/adapters/movement-http.service';
import { ReportHttpService } from './infrastructure/adapters/report-http.service';
import { AccountHttpService } from '../cuentas/infrastructure/adapters/account-http.service';
import { AuthService } from '@core/services/auth/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { TransactionFormComponent } from './infrastructure/ui/transaction-form/transaction-form.component';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { NotificationService } from '@core/services/notification.service';

@Component({
  selector: 'app-movimientos',
  templateUrl: './movimientos.component.html',
  styleUrls: ['./movimientos.component.css']
})
export class MovimientosComponent implements OnInit, OnDestroy, AfterViewInit {

  isAdmin = false;
  movements: Movement[] = [];
  displayedColumns: string[] = ['movementId', 'date', 'clientName', 'accountNumber', 'accountType', 'movementType', 'value', 'balance'];
  
  reportForm: FormGroup;
  isLoading = true;
  searchAccountId: string = '';

  constructor(
    private movementHttpService: MovementHttpService,
    private reportHttpService: ReportHttpService,
    private accountHttpService: AccountHttpService,
    private authService: AuthService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) {
    this.reportForm = this.fb.group({
      startDate: ['', Validators.required],
      endDate: ['', Validators.required]
    });
  }

  isScrollingDown = false;
  private lastScrollTop = 0;
  private scrollListener: any;

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    if (this.isAdmin) {
      this.loadMovements();
    } else {
      this.loadMyMovements();
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

  createMovement() {
    const isMobile = window.innerWidth < 900;
    const dialogRef = this.dialog.open(TransactionFormComponent, {
      width: isMobile ? '100vw' : '500px',
      maxWidth: isMobile ? '100vw' : '80vw',
      height: isMobile ? '100vh' : 'auto',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (this.isAdmin) {
          this.loadMovements();
        } else {
          this.loadMyMovements();
        }
      }
    });
  }

  loadMovements() {
    this.isLoading = true;
    this.movementHttpService.getAllMovements().subscribe({
      next: res => {
        this.movements = res;
        this.isLoading = false;
      },
      error: err => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  filterMovements() {
    if (this.searchAccountId) {
      this.isLoading = true;
      this.movementHttpService.getAllMovements(this.searchAccountId).subscribe({
        next: res => {
          this.movements = res;
          this.isLoading = false;
        },
        error: err => {
          console.error(err);
          this.isLoading = false;
        }
      });
    } else {
      this.loadMovements();
    }
  }

  clearSearch() {
    this.searchAccountId = '';
    this.loadMovements();
  }

  loadMyMovements() {
    this.isLoading = true;
    const user = this.authService.currentUserValue;
    if (user?.clientId) {
      this.movementHttpService.getMovementsByClientId(user.clientId).subscribe({
        next: res => {
          this.movements = res;
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

  downloadReport() {
    if (this.reportForm.invalid) return;

    const start = this.formatDate(this.reportForm.value.startDate);
    const end = this.formatDate(this.reportForm.value.endDate);
    const user = this.authService.currentUserValue;
    const clientId = this.isAdmin ? undefined : user?.clientId;

    this.reportHttpService.getReport(start, end, clientId).subscribe({
      next: res => {
        const doc = new jsPDF();
        doc.text(`Reporte de Movimientos`, 14, 15);
        doc.setFontSize(10);
        doc.text(`Desde: ${start} - Hasta: ${end}`, 14, 22);

        const tableColumn = ["Fecha", "Cliente", "Cuenta", "Tipo", "Valor", "Saldo"];
        const tableRows: any[] = [];

        if (Array.isArray(res)) {
          res.forEach(movement => {
            const movementData = [
              movement.date || '',
              movement.clientName || 'N/A',
              this.maskAccount(movement.accountNumber || ''),
              movement.movementType || '',
              movement.value !== undefined ? movement.value.toString() : '',
              movement.balance !== undefined ? movement.balance.toString() : ''
            ];
            tableRows.push(movementData);
          });
        }

        autoTable(doc, {
          head: [tableColumn],
          body: tableRows,
          startY: 30,
        });

        doc.save(`reporte_movimientos_${start}_${end}.pdf`);
      },
      error: err => {
        console.error('Error fetching report', err);
        this.notificationService.showError('Error al descargar el reporte');
      }
    });
  }

  private formatDate(date: Date): string {
    const d = new Date(date);
    let month = '' + (d.getMonth() + 1);
    let day = '' + d.getDate();
    const year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
  }

  maskAccount(accountNumber: string): string {
    if (!accountNumber || accountNumber.length < 2) return accountNumber;
    if (accountNumber.length <= 4) return accountNumber[0] + 'X'.repeat(accountNumber.length - 2) + accountNumber[accountNumber.length - 1];
    return accountNumber[0] + 'X'.repeat(accountNumber.length - 2) + accountNumber[accountNumber.length - 1];
  }
}

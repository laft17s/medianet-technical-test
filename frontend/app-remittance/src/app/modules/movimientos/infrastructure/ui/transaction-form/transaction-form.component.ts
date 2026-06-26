import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Movement, MovementHttpService } from '../../adapters/movement-http.service';
import { Account, AccountHttpService } from '../../../../cuentas/infrastructure/adapters/account-http.service';
import { AuthService } from '@core/services/auth/auth.service';
import { NotificationService } from '@core/services/notification.service';

@Component({
  selector: 'app-transaction-form',
  templateUrl: './transaction-form.component.html',
  styleUrls: ['./transaction-form.component.css']
})
export class TransactionFormComponent implements OnInit {

  transactionForm: FormGroup;
  isAdmin = false;
  accounts: Account[] = [];
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<TransactionFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private movementHttpService: MovementHttpService,
    private accountHttpService: AccountHttpService,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {
    this.isAdmin = this.authService.isAdmin();
    this.transactionForm = this.fb.group({
      accountId: ['', Validators.required],
      movementType: ['Deposito', Validators.required],
      value: ['', [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts() {
    const user = this.authService.currentUserValue;
    const clientId = this.isAdmin ? undefined : user?.clientId;

    this.accountHttpService.getAllAccounts(clientId).subscribe({
      next: (accounts) => {
        this.accounts = accounts;
      },
      error: (err) => {
        console.error('Error loading accounts', err);
      }
    });
  }

  maskAccount(accountNumber: string | undefined): string {
    return accountNumber || '';
  }

  onSubmit(): void {
    if (this.transactionForm.valid) {
      this.isLoading = true;
      const formValue = this.transactionForm.value;
      
      let movementValue = Number(formValue.value);
      if (formValue.movementType === 'Retiro') {
        movementValue = -Math.abs(movementValue);
      } else {
        movementValue = Math.abs(movementValue);
      }

      const movementData: Movement = {
        accountId: formValue.accountId,
        movementType: formValue.movementType,
        value: movementValue
      };
      
      this.movementHttpService.createMovement(movementData).subscribe({
        next: () => {
          this.isLoading = false;
          this.notificationService.showSuccess('Movimiento guardado con éxito');
          this.dialogRef.close(true);
        },
        error: (err) => {
          console.error('Error creating movement', err);
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

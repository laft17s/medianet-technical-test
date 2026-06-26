import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Account, AccountHttpService } from '../../adapters/account-http.service';
import { Client, ClientHttpService } from '../../../../clientes/infrastructure/adapters/client-http.service';
import { AuthService } from '@core/services/auth/auth.service';
import { NotificationService } from '@core/services/notification.service';

@Component({
  selector: 'app-account-form',
  templateUrl: './account-form.component.html',
  styleUrls: ['./account-form.component.css']
})
export class AccountFormComponent implements OnInit {

  accountForm: FormGroup;
  isAdmin = false;
  clients: Client[] = [];
  isLoading = false;

  constructor(
    private dialogRef: MatDialogRef<AccountFormComponent>,
    private fb: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private accountHttpService: AccountHttpService,
    private clientHttpService: ClientHttpService,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {
    this.isAdmin = this.authService.isAdmin();
    this.accountForm = this.fb.group({
      accountNumber: ['', [Validators.required, Validators.minLength(6), Validators.pattern(/^[A-Z0-9]+$/)]],
      accountType: ['Ahorros', Validators.required],
      initialBalance: [0, [Validators.required, Validators.min(0)]],
      status: [true],
      clientId: [null]
    });
    
    if (this.isAdmin) {
      this.accountForm.get('clientId')?.setValidators(Validators.required);
    }
  }

  ngOnInit(): void {
    if (this.isAdmin) {
      this.loadClients();
    }
  }

  onAccountNumberInput(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    let input = inputElement.value;
    // Replace anything that is not alphanumeric and convert to uppercase
    input = input.toUpperCase().replace(/[^A-Z0-9]/g, '');
    this.accountForm.get('accountNumber')?.setValue(input, { emitEvent: false });
  }

  loadClients() {
    this.clientHttpService.getAllClients().subscribe({
      next: (clients) => {
        this.clients = clients;
      },
      error: (err) => {
        console.error('Error loading clients', err);
      }
    });
  }

  onSubmit(): void {
    if (this.accountForm.valid) {
      const { clientId: formClientId, ...formValues } = this.accountForm.value;
      const accountData: Account = { ...formValues };

      if (this.isAdmin) {
        accountData.clientId = formClientId;
      } else {
        const user = this.authService.currentUserValue;
        if (!user?.clientId) {
          this.notificationService.showError('No se pudo identificar el cliente en sesión. Inicie sesión nuevamente.');
          return;
        }
        accountData.clientId = user.clientId;
      }

      this.isLoading = true;
      this.accountHttpService.createAccount(accountData).subscribe({
        next: () => {
          this.isLoading = false;
          this.notificationService.showSuccess('Cuenta guardada con éxito');
          this.dialogRef.close(true);
        },
        error: (err) => {
          console.error('Error creating account', err);
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

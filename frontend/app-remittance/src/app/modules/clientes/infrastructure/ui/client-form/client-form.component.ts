import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Client, ClientHttpService } from '../../../infrastructure/adapters/client-http.service';
import { AccountHttpService } from '../../../../cuentas/infrastructure/adapters/account-http.service';
import { MovementHttpService } from '../../../../movimientos/infrastructure/adapters/movement-http.service';
import { switchMap } from 'rxjs/operators';
import { AuthService } from '@core/services/auth/auth.service';

export function cedulaValidator(control: AbstractControl): ValidationErrors | null {
  const cedula = control.value;
  if (!cedula) return null;
  
  if (cedula.length !== 10) return { invalidCedula: true };
  if (!/^[0-9]+$/.test(cedula)) return { invalidCedula: true };

  const prov = parseInt(cedula.substring(0, 2), 10);
  if (prov < 1 || prov > 24) return { invalidCedula: true };

  const digits = cedula.split('').map(Number);
  const thirdDigit = digits[2];
  if (thirdDigit >= 6) return { invalidCedula: true };

  let sum = 0;
  for (let i = 0; i < 9; i++) {
    let val = digits[i] * (i % 2 === 0 ? 2 : 1);
    if (val > 9) val -= 9;
    sum += val;
  }

  const verifier = (Math.ceil(sum / 10) * 10 - sum);
  if (verifier !== digits[9]) return { invalidCedula: true };

  return null;
}

@Component({
  selector: 'app-client-form',
  templateUrl: './client-form.component.html',
  styleUrls: ['./client-form.component.css']
})
export class ClientFormComponent implements OnInit {

  clientForm: FormGroup;
  isEditMode = false;
  isAdmin = false;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ClientFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Client,
    private clientHttpService: ClientHttpService,
    private accountHttpService: AccountHttpService,
    private movementHttpService: MovementHttpService,
    private snackBar: MatSnackBar,
    private authService: AuthService
  ) {
    this.isEditMode = !!data?.clientId;
    this.isAdmin = this.authService.isAdmin();

    this.clientForm = this.fb.group({
      clientId: [data?.clientId || null],
      name: [data?.name || '', [Validators.required, Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/)]],
      identification: [data?.identification || '', [Validators.required, cedulaValidator]],
      age: [data?.age || '', [Validators.required, Validators.min(18), Validators.max(100)]],
      gender: [data?.gender || '', Validators.required],
      address: [data?.address || '', Validators.required],
      phone: [data?.phone || '', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      status: [data?.status !== undefined ? data?.status : true],
      accountType: ['Ahorros', Validators.required]
    });
  }

  private generateRandomPassword(): string {
    return Math.random().toString(36).slice(-6);
  }

  ngOnInit(): void {
  }

  onNameInput(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    let input = inputElement.value;
    input = input.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '');
    this.clientForm.get('name')?.setValue(input, { emitEvent: false });
  }

  onNumericInput(event: Event, controlName: string) {
    const inputElement = event.target as HTMLInputElement;
    let input = inputElement.value;
    input = input.replace(/[^0-9]/g, '');
    this.clientForm.get(controlName)?.setValue(input, { emitEvent: false });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onSave(): void {
    if (this.clientForm.invalid) {
      this.clientForm.markAllAsTouched();
      return;
    }

    const clientData: Client = this.clientForm.value;

    if (this.isEditMode) {
      this.clientHttpService.updateClient(clientData).subscribe(
        res => this.dialogRef.close(true),
        err => {
          console.error(err);
          this.snackBar.open('❌ Error actualizando el cliente', 'Cerrar', { duration: 3000, panelClass: ['error-snackbar'] });
        }
      );
    } else {
      clientData.password = this.generateRandomPassword();
      this.clientHttpService.createClient(clientData).subscribe(
        res => {
          this.snackBar.open('✅ Cliente creado. La cuenta se generará en segundo plano.', 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        err => {
          console.error(err);
          this.snackBar.open('❌ Error al crear el cliente', 'Cerrar', { duration: 3000, panelClass: ['error-snackbar'] });
        }
      );
    }
  }
}

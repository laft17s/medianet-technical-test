import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '@core/services/auth/auth.service';
import { ecuadorianIdValidator } from '../../../../../shared/validators/ecuadorian-id.validator';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  error: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    if (this.authService.currentUserValue) {
      this.router.navigate(['/clientes']);
    }

    this.registerForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.pattern('^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$')]],
      gender: ['', Validators.required],
      age: ['', [Validators.required, Validators.min(18), Validators.max(120)]],
      identification: ['', [Validators.required, Validators.pattern('^[0-9]{10}$'), ecuadorianIdValidator()]],
      address: ['', Validators.required],
      phone: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      accountType: ['', Validators.required]
    });
  }

  get f() { return this.registerForm.controls; }

  onSubmit() {
    if (this.registerForm.invalid) {
      return;
    }
    
    const clientData = this.registerForm.value;
    
    this.authService.register(clientData).subscribe({
      next: () => {
        this.router.navigate(['/clientes']);
      },
      error: error => {
        this.error = error;
      }
    });
  }
}

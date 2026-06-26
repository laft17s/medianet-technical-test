import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function ecuadorianIdValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const cedula = control.value;
    
    if (!cedula || typeof cedula !== 'string') {
      return null;
    }

    if (cedula.length !== 10) {
      return { invalidEcuadorianId: true };
    }

    const digito_region = parseInt(cedula.substring(0, 2), 10);
    if (digito_region < 1 || digito_region > 24) {
      return { invalidEcuadorianId: true };
    }

    const ultimo_digito = parseInt(cedula.substring(9, 10), 10);
    let suma = 0;

    for (let i = 0; i < 9; i++) {
      let valor = parseInt(cedula.charAt(i), 10);
      if (i % 2 === 0) {
        valor = valor * 2;
        if (valor > 9) {
          valor = valor - 9;
        }
      }
      suma += valor;
    }

    const decena = (Math.floor(suma / 10) + 1) * 10;
    let digito_validador = decena - suma;

    if (digito_validador === 10) {
      digito_validador = 0;
    }

    if (digito_validador !== ultimo_digito) {
      return { invalidEcuadorianId: true };
    }

    return null;
  };
}

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { API_ENDPOINTS } from '@core/constants/app.constants';

export interface Movement {
  movementId?: number;
  date?: string;
  movementType?: string;
  value?: number;
  balance?: number;
  accountId?: string;
  accountNumber?: string;
  maskedAccountNumber?: string;
  accountType?: string;
  clientName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MovementHttpService {

  private apiUrl = `${environment.apiUrl}${API_ENDPOINTS.MOVEMENTS}`;

  constructor(private http: HttpClient) { }

  getAllMovements(accountId?: string | number): Observable<Movement[]> {
    if (accountId) {
      return this.http.get<any[]>(`${this.apiUrl}/account/${accountId}`).pipe(
        map(res => res.map(m => this.mapToMovement(m)))
      );
    }
    return this.http.get<any[]>(this.apiUrl).pipe(
      map(res => res.map(m => this.mapToMovement(m)))
    );
  }

  private mapToMovement(data: any): Movement {
    return {
      movementId: data.movementId || data.movementid,
      date: data.date,
      movementType: data.movementType || data.movementtype,
      value: data.value,
      balance: data.balance,
      accountId: data.accountId || data.accountid,
      accountNumber: data.accountNumber || data.accountnumber,
      maskedAccountNumber: data.maskedAccountNumber || data.maskedaccountnumber,
      accountType: data.accountType || data.accounttype,
      clientName: data.clientName || data.clientname
    };
  }

  getMovementById(id: number): Observable<Movement> {
    return this.http.get<Movement>(`${this.apiUrl}/${id}`);
  }

  createMovement(movement: Movement): Observable<Movement> {
    return this.http.post<Movement>(this.apiUrl, movement);
  }

  getMovementsByClientId(clientId: string | number): Observable<Movement[]> {
    return this.http.get<any[]>(`${this.apiUrl}/client/${clientId}`).pipe(
      map(res => res.map(m => this.mapToMovement(m)))
    );
  }
}

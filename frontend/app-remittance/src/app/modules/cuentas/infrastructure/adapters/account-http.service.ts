import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { API_ENDPOINTS } from '@core/constants/app.constants';

export interface Account {
  accountId?: number;
  accountNumber?: string;
  accountType?: string;
  initialBalance?: number;
  status?: boolean;
  clientId?: number;
  clientName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AccountHttpService {

  private apiUrl = `${environment.apiUrl}${API_ENDPOINTS.ACCOUNTS}`;

  constructor(private http: HttpClient) { }

  getAllAccounts(clientId?: string | number): Observable<Account[]> {
    if (clientId) {
      return this.http.get<any[]>(`${this.apiUrl}/client/${clientId}`).pipe(
        map(res => res.map(a => this.mapToAccount(a)))
      );
    }
    return this.http.get<any[]>(this.apiUrl).pipe(
      map(res => res.map(a => this.mapToAccount(a)))
    );
  }

  private mapToAccount(data: any): Account {
    return {
      accountId: data.accountId || data.accountid,
      accountNumber: data.accountNumber || data.accountnumber,
      accountType: data.accountType || data.accounttype,
      initialBalance: data.initialBalance !== undefined ? data.initialBalance : data.initialbalance,
      status: data.status,
      clientId: data.clientId || data.clientid,
      clientName: data.clientName || data.clientname
    };
  }

  getAccountById(id: number): Observable<Account> {
    return this.http.get<Account>(`${this.apiUrl}/${id}`);
  }

  createAccount(account: Account): Observable<Account> {
    return this.http.post<Account>(this.apiUrl, account);
  }

  updateAccount(account: Account): Observable<Account> {
    return this.http.put<Account>(this.apiUrl, account);
  }

  deleteAccount(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}

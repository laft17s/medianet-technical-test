import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { API_ENDPOINTS } from '@core/constants/app.constants';

export interface Client {
  clientId?: number;
  name?: string;
  gender?: string;
  age?: number;
  identification?: string;
  address?: string;
  phone?: string;
  password?: string;
  status?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ClientHttpService {

  private apiUrl = `${environment.apiUrl}${API_ENDPOINTS.CLIENTS}`;

  constructor(private http: HttpClient) { }

  getAllClients(): Observable<Client[]> {
    return this.http.get<Client[]>(this.apiUrl);
  }

  getClientById(id: number): Observable<Client> {
    return this.http.get<Client>(`${this.apiUrl}/${id}`);
  }

  createClient(client: Client): Observable<Client> {
    return this.http.post<Client>(this.apiUrl, client);
  }

  updateClient(client: Client): Observable<Client> {
    return this.http.put<Client>(this.apiUrl, client);
  }

  deleteClient(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  patchClientStatus(id: number, status: boolean): Observable<Client> {
    return this.http.patch<Client>(`${this.apiUrl}/${id}/status`, { status });
  }
}

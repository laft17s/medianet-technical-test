import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { API_ENDPOINTS } from '@core/constants/app.constants';

@Injectable({
  providedIn: 'root'
})
export class ReportHttpService {

  private apiUrl = `${environment.apiUrl}${API_ENDPOINTS.REPORTS}`;

  constructor(private http: HttpClient) { }

  getReport(startDate: string, endDate: string, clientId?: number): Observable<any> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    if (clientId) {
      params = params.set('clientId', clientId.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }
}

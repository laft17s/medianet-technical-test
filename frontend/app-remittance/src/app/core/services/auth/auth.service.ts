import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '@env/environment';
import { API_ENDPOINTS, AUTH_CONSTANTS, ROUTER_PATHS } from '@core/constants/app.constants';

export interface UserSession {
  username: string;
  role: string;
  clientId: number;
  token: string;
  identification: string;
  status: boolean;
}

export interface AuthResponse {
  name: string;
  clientId: number;
  token: string;
  identification: string;
  status: boolean;
  role: string;
}

export class UserSessionBuilder {
  private session: UserSession = {
    username: '',
    role: '',
    clientId: 0,
    token: '',
    identification: '',
    status: true
  };

  withUsername(username: string): this {
    this.session.username = username;
    return this;
  }

  withRole(role: string): this {
    this.session.role = role;
    return this;
  }

  withClientId(clientId: number): this {
    this.session.clientId = clientId;
    return this;
  }

  withToken(token: string): this {
    this.session.token = token;
    return this;
  }

  withIdentification(identification: string): this {
    this.session.identification = identification;
    return this;
  }

  withStatus(status: boolean): this {
    this.session.status = status;
    return this;
  }

  build(): UserSession {
    return { ...this.session };
  }
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<UserSession | null>;
  public currentUser: Observable<UserSession | null>;

  private timeoutId: any;
  private readonly TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

  constructor(private http: HttpClient, private router: Router, private ngZone: NgZone) {
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<UserSession | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();

    if (this.currentUserValue) {
      this.initInactivityTimer();
    }
  }

  public get currentUserValue(): UserSession | null {
    return this.currentUserSubject.value;
  }

  login(identification: string, password: string): Observable<UserSession> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}${API_ENDPOINTS.AUTH_LOGIN}`, { identification, password })
      .pipe(
        map((response: AuthResponse) => {
          const role = response.role || AUTH_CONSTANTS.ROLE_USER;
          const user = new UserSessionBuilder()
            .withUsername(response.name)
            .withRole(role)
            .withClientId(response.clientId)
            .withToken(response.token)
            .withIdentification(response.identification)
            .withStatus(response.status)
            .build();
            
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
          this.initInactivityTimer();
          return user;
        }),
        catchError(error => {
          const message = typeof error === 'string' ? error : 'Error de autenticación';
          return throwError(() => message);
        })
      );
  }

  register(data: any): Observable<UserSession> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}${API_ENDPOINTS.AUTH_REGISTER}`, data)
      .pipe(
        map((response: AuthResponse) => {
          const role = response.role || AUTH_CONSTANTS.ROLE_USER;
          const user = new UserSessionBuilder()
            .withUsername(response.name)
            .withRole(role)
            .withClientId(response.clientId)
            .withToken(response.token)
            .withIdentification(response.identification)
            .build();

          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
          this.initInactivityTimer();
          return user;
        }),
        catchError(error => {
          const message = typeof error === 'string' ? error : 'Error de registro';
          return throwError(() => message);
        })
      );
  }

  logout(): void {
    this.clearInactivityTimer();
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate([ROUTER_PATHS.LOGIN]);
  }

  isAdmin(): boolean {
    return this.currentUserValue?.role === AUTH_CONSTANTS.ROLE_ADMIN;
  }

  getToken(): string | null {
    return this.currentUserValue?.token || null;
  }

  // Inactivity timeout logic
  private initInactivityTimer() {
    this.clearInactivityTimer();
    this.resetTimer();
    this.setupActivityListeners();
  }

  private clearInactivityTimer() {
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
    this.removeActivityListeners();
  }

  private resetTimer = () => {
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
    this.ngZone.runOutsideAngular(() => {
      this.timeoutId = setTimeout(() => {
        this.ngZone.run(() => {
          this.logout();
        });
      }, this.TIMEOUT_MS);
    });
  }

  private setupActivityListeners() {
    window.addEventListener('mousemove', this.resetTimer);
    window.addEventListener('keypress', this.resetTimer);
    window.addEventListener('scroll', this.resetTimer);
    window.addEventListener('click', this.resetTimer);
  }

  private removeActivityListeners() {
    window.removeEventListener('mousemove', this.resetTimer);
    window.removeEventListener('keypress', this.resetTimer);
    window.removeEventListener('scroll', this.resetTimer);
    window.removeEventListener('click', this.resetTimer);
  }
}

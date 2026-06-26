import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { mergeMap, catchError } from 'rxjs/operators';
import { NotificationService } from '../services/notification.service';
import { ApiResponse, isApiResponse } from '../models/api-response.model';

@Injectable()
export class ApiResponseInterceptor implements HttpInterceptor {

  constructor(private notificationService: NotificationService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      mergeMap((event: HttpEvent<unknown>) => {
        if (!(event instanceof HttpResponse)) {
          return of(event);
        }

        const body = event.body;
        if (!isApiResponse(body)) {
          return of(event);
        }

        const apiResponse = body as ApiResponse;

        if (apiResponse.type === 'error') {
          this.notificationService.showError(apiResponse.message);
          return throwError(() => apiResponse.message);
        }

        if (apiResponse.type === 'warning') {
          this.notificationService.showWarning(apiResponse.message);
        }

        const unwrapped = apiResponse.data !== undefined ? apiResponse.data : null;
        return of(event.clone({ body: unwrapped }));
      }),
      catchError((error: unknown) => {
        if (typeof error === 'string') {
          return throwError(() => error);
        }

        const httpError = error as { error?: ApiResponse | { message?: string; error?: string }; message?: string };
        const nested = httpError?.error;

        if (isApiResponse(nested)) {
          this.notificationService.showError(nested.message);
          return throwError(() => nested.message);
        }

        const legacyMessage = typeof nested === 'object' && nested !== null
          ? (nested as { error?: string }).error
          : undefined;

        if (legacyMessage) {
          this.notificationService.showError(legacyMessage);
          return throwError(() => legacyMessage);
        }

        this.notificationService.showError('Error de comunicación con el servidor');
        return throwError(() => httpError?.message || 'Error de comunicación con el servidor');
      })
    );
  }
}

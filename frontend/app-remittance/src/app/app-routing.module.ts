import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout.component';
import { AuthGuard } from './core/guards/auth.guard';

import { ROUTER_PATHS } from './core/constants/app.constants';

const routes: Routes = [
  { path: '', redirectTo: ROUTER_PATHS.LOGIN, pathMatch: 'full' },
  { path: 'auth', loadChildren: () => import('./modules/auth/auth.module').then(m => m.AuthModule) },
  { 
    path: '', 
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'clientes', loadChildren: () => import('./modules/clientes/clientes.module').then(m => m.ClientesModule) },
      { path: 'cuentas', loadChildren: () => import('./modules/cuentas/cuentas.module').then(m => m.CuentasModule) },
      { path: 'movimientos', loadChildren: () => import('./modules/movimientos/movimientos.module').then(m => m.MovimientosModule) }
    ]
  },
  { path: '**', redirectTo: ROUTER_PATHS.LOGIN }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

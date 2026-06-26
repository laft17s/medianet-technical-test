export const API_ENDPOINTS = {
  AUTH_LOGIN: '/auth/login',
  AUTH_REGISTER: '/auth/register',
  CLIENTS: '/clients',
  ACCOUNTS: '/accounts',
  MOVEMENTS: '/movements',
  REPORTS: '/reports'
};

export const ROUTER_PATHS = {
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',
  HOME: '/dashboard',
  CLIENTS: '/clientes'
};

export const AUTH_CONSTANTS = {
  ROLE_ADMIN: 'admin',
  ROLE_USER: 'user'
};

export const UI_MESSAGES = {
  CONFIRM_DELETE_CLIENT: '¿Estás seguro de que deseas eliminar (inactivar) este cliente?',
  NOT_IMPLEMENTED: 'Funcionalidad de editar/crear abrirá un modal aquí.'
};

export const TABLE_COLUMNS = {
  CLIENTS: ['clientId', 'name', 'identification', 'status', 'actions']
};

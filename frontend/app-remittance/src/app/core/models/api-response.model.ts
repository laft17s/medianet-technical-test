export type ResponseType = 'success' | 'error' | 'warning';

export interface ApiResponse<T = unknown> {
  code: string;
  type: ResponseType;
  message: string;
  data?: T;
}

export const RESPONSE_CODES = {
  OK_000: 'OK-000',
  ERR_001: 'ERR-001',
  ERR_002: 'ERR-002',
  ERR_003: 'ERR-003',
  ERR_004: 'ERR-004',
  ERR_005: 'ERR-005',
  ERR_006: 'ERR-006',
  ERR_999: 'ERR-999',
  WRN_001: 'WRN-001'
} as const;

export const RESPONSE_MESSAGES: Record<string, string> = {
  [RESPONSE_CODES.OK_000]: 'Operación exitosa',
  [RESPONSE_CODES.ERR_001]: 'Saldo no disponible',
  [RESPONSE_CODES.ERR_002]: 'Recurso no encontrado',
  [RESPONSE_CODES.ERR_003]: 'Operación no permitida',
  [RESPONSE_CODES.ERR_004]: 'Argumento inválido',
  [RESPONSE_CODES.ERR_005]: 'Violación de regla de negocio',
  [RESPONSE_CODES.ERR_006]: 'Credenciales inválidas o usuario inactivo',
  [RESPONSE_CODES.ERR_999]: 'Error interno del servidor',
  [RESPONSE_CODES.WRN_001]: 'Advertencia'
};

export function isApiResponse(body: unknown): body is ApiResponse {
  if (!body || typeof body !== 'object') {
    return false;
  }
  const candidate = body as ApiResponse;
  return typeof candidate.code === 'string'
    && typeof candidate.type === 'string'
    && typeof candidate.message === 'string';
}

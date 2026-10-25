const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

/** 서버 주소. 구글 로그인은 화면이 아니라 서버로 직접 이동해야 해서 따로 내보낸다 */
export const API_ORIGIN = BASE_URL || 'http://localhost:8080'

/**
 * 토큰을 어디서 가져오고, 401 이면 뭘 할지는 auth store 가 정한다.
 * client 가 store 를 직접 import 하지 않게 해서 의존 방향을 한쪽(store → client)으로 유지한다.
 */
let auth = { getToken: () => null, onUnauthorized: () => {} }
export function setAuthHandlers(handlers) {
  auth = handlers
}

/** 서버의 ErrorResponse({ code, message, fieldErrors })를 담는 에러 */
export class ApiError extends Error {
  constructor({ status, code, message, fieldErrors = [] }) {
    super(message)
    this.status = status
    this.code = code
    this.fieldErrors = fieldErrors
  }
}

/** fetch 를 감싸서 JSON 변환과 에러 처리를 한곳에서 한다 */
export async function request(path, { method = 'GET', body, query } = {}) {
  const qs = query ? `?${new URLSearchParams(query)}` : ''
  const headers = {}
  if (body) headers['Content-Type'] = 'application/json'
  const token = auth.getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  let res
  try {
    res = await fetch(`${BASE_URL}${path}${qs}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError({ status: 0, code: 'NETWORK', message: '서버에 연결할 수 없어요.' })
  }

  if (res.status === 204) return null
  const data = await res.json().catch(() => null)
  // 토큰이 만료됐거나 잘못됨 → 로그인 화면으로 (로그인 요청 자체의 401 은 화면에서 문구로 보여준다)
  if (res.status === 401 && !path.startsWith('/api/auth/')) auth.onUnauthorized()
  if (!res.ok) {
    throw new ApiError({
      status: res.status,
      code: data?.code ?? 'UNKNOWN',
      message: data?.message ?? '잠시 후 다시 시도해 주세요.',
      fieldErrors: data?.fieldErrors,
    })
  }
  return data
}

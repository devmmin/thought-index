const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

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
  let res
  try {
    res = await fetch(`${BASE_URL}${path}${qs}`, {
      method,
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError({ status: 0, code: 'NETWORK', message: '서버에 연결할 수 없어요.' })
  }

  if (res.status === 204) return null
  const data = await res.json().catch(() => null)
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

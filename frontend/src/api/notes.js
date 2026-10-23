import { request } from './client'

export const notesApi = {
  listByTab: (tabId, page = 0) => request('/api/notes', { query: { tabId, page, size: 20 } }),
  search: (q, page = 0) => request('/api/notes/search', { query: { q, page, size: 20 } }),
  get: (id) => request(`/api/notes/${id}`),
  create: (note) => request('/api/notes', { method: 'POST', body: note }),
  update: (id, note) => request(`/api/notes/${id}`, { method: 'PUT', body: note }),
  remove: (id) => request(`/api/notes/${id}`, { method: 'DELETE' }),
}

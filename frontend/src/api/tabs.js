import { request } from './client'

export const tabsApi = {
  list: () => request('/api/tabs'),
  create: (tab) => request('/api/tabs', { method: 'POST', body: tab }),
  update: (id, tab) => request(`/api/tabs/${id}`, { method: 'PUT', body: tab }),
  reorder: (tabIds) => request('/api/tabs/order', { method: 'PUT', body: { tabIds } }),
  remove: (id) => request(`/api/tabs/${id}`, { method: 'DELETE' }),
}

// CRM invoice stub - legacy component support
const api = {
  stub: () => Promise.resolve({ data: [] })
}
export default api
export function crminvoiceIndexAPI() { return Promise.resolve({ data: { list: [], total: 0 } }) }
export function crminvoiceSaveAPI() { return Promise.resolve({ data: {} }) }


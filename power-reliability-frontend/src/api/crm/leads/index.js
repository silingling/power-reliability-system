// CRM leads stub - legacy component support
const api = {
  stub: () => Promise.resolve({ data: [] })
}
export default api
export function crmleadsIndexAPI() { return Promise.resolve({ data: { list: [], total: 0 } }) }
export function crmleadsSaveAPI() { return Promise.resolve({ data: {} }) }


// CRM customer stub - legacy component support
const api = {
  stub: () => Promise.resolve({ data: [] })
}
export default api
export function crmcustomerIndexAPI() { return Promise.resolve({ data: { list: [], total: 0 } }) }
export function crmcustomerSaveAPI() { return Promise.resolve({ data: {} }) }


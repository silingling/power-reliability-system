// CRM contract stub - legacy component support
const api = {
  stub: () => Promise.resolve({ data: [] })
}
export default api
export function crmcontractIndexAPI() { return Promise.resolve({ data: { list: [], total: 0 } }) }
export function crmcontractSaveAPI() { return Promise.resolve({ data: {} }) }


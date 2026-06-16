// CRM business stub - legacy component support
const api = {
  stub: () => Promise.resolve({ data: [] })
}
export default api
export function crmbusinessIndexAPI() { return Promise.resolve({ data: { list: [], total: 0 } }) }
export function crmbusinessSaveAPI() { return Promise.resolve({ data: {} }) }


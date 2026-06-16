import request from '@/utils/request'

const BASE = '/api/ledger'

// 台区管理
export function getStationList(data) {
  return request({ url: `${BASE}/area/list`, method: 'get', params: data })
}
export function addStation(data) {
  return request({ url: `${BASE}/area/add`, method: 'post', data })
}
export function updateStation(data) {
  return request({ url: `${BASE}/area/update`, method: 'put', data })
}
export function deleteStation(id) {
  return request({ url: `${BASE}/area/delete/${id}`, method: 'delete' })
}

// 设备台账
export function getDeviceList(data) {
  return request({ url: `${BASE}/equipment/list`, method: 'get', params: data })
}
export function addDevice(data) {
  return request({ url: `${BASE}/equipment/add`, method: 'post', data })
}
export function updateDevice(data) {
  return request({ url: `${BASE}/equipment/update`, method: 'put', data })
}
export function deleteDevice(id) {
  return request({ url: `${BASE}/equipment/delete/${id}`, method: 'delete' })
}

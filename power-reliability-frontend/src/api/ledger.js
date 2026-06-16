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

// 用户管理（用电用户）
export function getUserList(data) {
  return request({ url: `${BASE}/consumer/list`, method: 'get', params: data })
}
export function addUser(data) {
  return request({ url: `${BASE}/consumer/add`, method: 'post', data })
}
export function updateUser(data) {
  return request({ url: `${BASE}/consumer/update`, method: 'put', data })
}
export function deleteUser(id) {
  return request({ url: `${BASE}/consumer/delete/${id}`, method: 'delete' })
}

// 线路管理
export function getLineList(data) {
  return request({ url: `${BASE}/line/list`, method: 'get', params: data })
}
export function addLine(data) {
  return request({ url: `${BASE}/line/add`, method: 'post', data })
}
export function updateLine(data) {
  return request({ url: `${BASE}/line/update`, method: 'put', data })
}
export function deleteLine(id) {
  return request({ url: `${BASE}/line/delete/${id}`, method: 'delete' })
}

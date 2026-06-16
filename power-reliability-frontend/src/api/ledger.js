import request from '@/utils/request'

const BASE = '/powerReliability/ledger'

// 台区管理
export function getStationList(data) {
  return request({ url: `${BASE}/station/list`, method: 'post', data })
}
export function addStation(data) {
  return request({ url: `${BASE}/station/add`, method: 'post', data })
}
export function updateStation(data) {
  return request({ url: `${BASE}/station/update`, method: 'post', data })
}
export function deleteStation(id) {
  return request({ url: `${BASE}/station/delete/${id}`, method: 'post' })
}

// 设备台账
export function getDeviceList(data) {
  return request({ url: `${BASE}/device/list`, method: 'post', data })
}
export function addDevice(data) {
  return request({ url: `${BASE}/device/add`, method: 'post', data })
}
export function updateDevice(data) {
  return request({ url: `${BASE}/device/update`, method: 'post', data })
}
export function deleteDevice(id) {
  return request({ url: `${BASE}/device/delete/${id}`, method: 'post' })
}

// 用户管理
export function getUserList(data) {
  return request({ url: `${BASE}/user/list`, method: 'post', data })
}
export function addUser(data) {
  return request({ url: `${BASE}/user/add`, method: 'post', data })
}
export function updateUser(data) {
  return request({ url: `${BASE}/user/update`, method: 'post', data })
}
export function deleteUser(id) {
  return request({ url: `${BASE}/user/delete/${id}`, method: 'post' })
}

// 线路管理
export function getLineList(data) {
  return request({ url: `${BASE}/line/list`, method: 'post', data })
}
export function addLine(data) {
  return request({ url: `${BASE}/line/add`, method: 'post', data })
}
export function updateLine(data) {
  return request({ url: `${BASE}/line/update`, method: 'post', data })
}
export function deleteLine(id) {
  return request({ url: `${BASE}/line/delete/${id}`, method: 'post' })
}

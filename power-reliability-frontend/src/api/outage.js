import request from '@/utils/request'

const BASE = '/api/outage'

// 停电事件列表
export function getOutageList(data) {
  return request({ url: `${BASE}/event/list`, method: 'get', params: data })
}
export function getOutageDetail(id) {
  return request({ url: `${BASE}/event/detail/${id}`, method: 'get' })
}
export function deleteOutage(id) {
  return request({ url: `${BASE}/event/delete/${id}`, method: 'delete' })
}

// 计划停电
export function getPlannedList(data) {
  return request({ url: `${BASE}/planned/list`, method: 'get', params: data })
}
export function addPlanned(data) {
  return request({ url: `${BASE}/planned/submit`, method: 'post', data })
}
export function updatePlanned(data) {
  return request({ url: `${BASE}/planned/update`, method: 'put', data })
}
export function submitPlanned(id) {
  return request({ url: `${BASE}/planned/approve`, method: 'post', data: { id } })
}

// 故障停电
export function getFaultList(data) {
  return request({ url: `${BASE}/fault/list`, method: 'get', params: data })
}
export function addFault(data) {
  return request({ url: `${BASE}/fault/report`, method: 'post', data })
}
export function updateFault(data) {
  return request({ url: `${BASE}/fault/update`, method: 'put', data })
}

// 豁免管理
export function getExemptionList(data) {
  return request({ url: `${BASE}/exemption/list`, method: 'get', params: data })
}
export function addExemption(data) {
  return request({ url: `${BASE}/exemption/manual`, method: 'post', data })
}
export function updateExemption(data) {
  return request({ url: `${BASE}/exemption/update`, method: 'put', data })
}

// 抢修工单
export function getRepairList(data) {
  return request({ url: `${BASE}/repair/list`, method: 'get', params: data })
}
export function addRepair(data) {
  return request({ url: `${BASE}/repair/dispatch`, method: 'post', data })
}
export function updateRepair(data) {
  return request({ url: `${BASE}/repair/update`, method: 'put', data })
}
export function dispatchRepair(id, data) {
  return request({ url: `${BASE}/repair/dispatch`, method: 'post', data: { ...data, id } })
}

import request from '@/utils/request'

const BASE = '/powerReliability/outage'

// 停电事件列表
export function getOutageList(data) {
  return request({ url: `${BASE}/list`, method: 'post', data })
}
export function getOutageDetail(id) {
  return request({ url: `${BASE}/detail/${id}`, method: 'post' })
}
export function deleteOutage(id) {
  return request({ url: `${BASE}/delete/${id}`, method: 'post' })
}

// 计划停电
export function getPlannedList(data) {
  return request({ url: `${BASE}/planned/list`, method: 'post', data })
}
export function addPlanned(data) {
  return request({ url: `${BASE}/planned/add`, method: 'post', data })
}
export function updatePlanned(data) {
  return request({ url: `${BASE}/planned/update`, method: 'post', data })
}
export function submitPlanned(id) {
  return request({ url: `${BASE}/planned/submit/${id}`, method: 'post' })
}

// 故障停电
export function getFaultList(data) {
  return request({ url: `${BASE}/fault/list`, method: 'post', data })
}
export function addFault(data) {
  return request({ url: `${BASE}/fault/add`, method: 'post', data })
}
export function updateFault(data) {
  return request({ url: `${BASE}/fault/update`, method: 'post', data })
}

// 豁免管理
export function getExemptionList(data) {
  return request({ url: `${BASE}/exemption/list`, method: 'post', data })
}
export function addExemption(data) {
  return request({ url: `${BASE}/exemption/add`, method: 'post', data })
}
export function updateExemption(data) {
  return request({ url: `${BASE}/exemption/update`, method: 'post', data })
}

// 抢修工单
export function getRepairList(data) {
  return request({ url: `${BASE}/repair/list`, method: 'post', data })
}
export function addRepair(data) {
  return request({ url: `${BASE}/repair/add`, method: 'post', data })
}
export function updateRepair(data) {
  return request({ url: `${BASE}/repair/update`, method: 'post', data })
}
export function dispatchRepair(id, data) {
  return request({ url: `${BASE}/repair/dispatch/${id}`, method: 'post', data })
}

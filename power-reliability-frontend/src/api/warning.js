import request from '@/utils/request'

const BASE = '/powerReliability/warning'

// 隐患预判
export function getPredictionList(data) {
  return request({ url: `${BASE}/prediction/list`, method: 'post', data })
}
export function getPredictionDetail(id) {
  return request({ url: `${BASE}/prediction/detail/${id}`, method: 'post' })
}

// 预警工单
export function getWarningOrderList(data) {
  return request({ url: `${BASE}/order/list`, method: 'post', data })
}
export function addWarningOrder(data) {
  return request({ url: `${BASE}/order/add`, method: 'post', data })
}
export function updateWarningOrder(data) {
  return request({ url: `${BASE}/order/update`, method: 'post', data })
}
export function handleWarningOrder(id, data) {
  return request({ url: `${BASE}/order/handle/${id}`, method: 'post', data })
}

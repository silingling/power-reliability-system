import request from '@/utils/request'

const BASE = '/api/warning'

// 隐患预判
export function getPredictionList(data) {
  return request({ url: `${BASE}/prediction/list`, method: 'get', params: data })
}
export function getPredictionDetail(id) {
  return request({ url: `${BASE}/prediction/detail/${id}`, method: 'get' })
}

// 预警工单
export function getWarningOrderList(data) {
  return request({ url: `${BASE}/order/list`, method: 'get', params: data })
}
export function addWarningOrder(data) {
  return request({ url: `${BASE}/order/dispatch`, method: 'post', data })
}
export function updateWarningOrder(data) {
  return request({ url: `${BASE}/order/update`, method: 'put', data })
}
export function handleWarningOrder(id, data) {
  return request({ url: `${BASE}/order/dispose/${id}`, method: 'post', data })
}

import request from '@/utils/request'

const BASE = '/powerReliability/governance'

// 频繁停电台账
export function getGovernanceList(data) {
  return request({ url: `${BASE}/list`, method: 'post', data })
}

// 原因分析
export function getAnalysisList(data) {
  return request({ url: `${BASE}/analysis/list`, method: 'post', data })
}
export function getAnalysisDetail(id) {
  return request({ url: `${BASE}/analysis/detail/${id}`, method: 'post' })
}

// 治理工单
export function getGovernOrderList(data) {
  return request({ url: `${BASE}/order/list`, method: 'post', data })
}
export function addGovernOrder(data) {
  return request({ url: `${BASE}/order/add`, method: 'post', data })
}
export function updateGovernOrder(data) {
  return request({ url: `${BASE}/order/update`, method: 'post', data })
}
export function executeGovernOrder(id) {
  return request({ url: `${BASE}/order/execute/${id}`, method: 'post' })
}

// 成效核验
export function getResultList(data) {
  return request({ url: `${BASE}/result/list`, method: 'post', data })
}
export function verifyResult(id) {
  return request({ url: `${BASE}/result/verify/${id}`, method: 'post' })
}

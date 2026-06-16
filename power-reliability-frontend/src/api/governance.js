import request from '@/utils/request'

const BASE = '/api/governance'

// 频繁停电台账
export function getGovernanceList(data) {
  return request({ url: `${BASE}/ledger/list`, method: 'get', params: data })
}

// 原因分析
export function getAnalysisList(data) {
  return request({ url: `${BASE}/cause/list`, method: 'get', params: data })
}
export function getAnalysisDetail(id) {
  return request({ url: `${BASE}/ledger/detail/${id}`, method: 'get' })
}

// 治理工单
export function getGovernOrderList(data) {
  return request({ url: `${BASE}/order/list`, method: 'get', params: data })
}
export function addGovernOrder(data) {
  return request({ url: `${BASE}/order/create`, method: 'post', data })
}
export function updateGovernOrder(data) {
  return request({ url: `${BASE}/order/update`, method: 'put', data })
}
export function executeGovernOrder(id) {
  return request({ url: `${BASE}/order/dispatch`, method: 'post', data: { id } })
}

// 成效核验（映射到 review 模块评估端点）
export function getResultList(data) {
  return request({ url: `/api/review/assessment/list`, method: 'get', params: data })
}
export function verifyResult(id) {
  return request({ url: `/api/review/assessment/verify/${id}`, method: 'post' })
}

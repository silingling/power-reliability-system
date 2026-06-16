import request from '@/utils/request'

const BASE = '/api/index'

// 指标核算
export function getIndexStatistics(data) {
  return request({ url: `${BASE}/list`, method: 'get', params: data })
}
export function calculateIndex(data) {
  return request({ url: `${BASE}/calculate`, method: 'post', data })
}

// 趋势分析
export function getTrendData(data) {
  return request({ url: `${BASE}/list`, method: 'get', params: data })
}

// 对比分析
export function getCompareData(data) {
  return request({ url: `${BASE}/compare`, method: 'get', params: data })
}

// 预警列表
export function getIndexWarningList(data) {
  return request({ url: `${BASE}/alert/list`, method: 'get', params: data })
}

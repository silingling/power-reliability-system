import request from '@/utils/request'

const BASE = '/powerReliability/index'

// 指标核算
export function getIndexStatistics(data) {
  return request({ url: `${BASE}/statistics`, method: 'post', data })
}
export function calculateIndex(data) {
  return request({ url: `${BASE}/calculate`, method: 'post', data })
}

// 趋势分析
export function getTrendData(data) {
  return request({ url: `${BASE}/trend`, method: 'post', data })
}

// 对比分析
export function getCompareData(data) {
  return request({ url: `${BASE}/compare`, method: 'post', data })
}

// 预警列表
export function getIndexWarningList(data) {
  return request({ url: `${BASE}/warning/list`, method: 'post', data })
}

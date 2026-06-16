import request from '@/utils/request'

const BASE = '/powerReliability/dashboard'

// 总览数据
export function getOverviewData() {
  return request({ url: `${BASE}/overview`, method: 'post' })
}

// 关键指标
export function getKeyIndicators() {
  return request({ url: `${BASE}/keyIndicators`, method: 'post' })
}

// 停电趋势
export function getOutageTrend(data) {
  return request({ url: `${BASE}/outageTrend`, method: 'post', data })
}

// 台区可靠性排名
export function getStationRanking() {
  return request({ url: `${BASE}/stationRanking`, method: 'post' })
}

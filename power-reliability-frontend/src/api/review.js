import request from '@/utils/request'

const BASE = '/api/review'

// 复盘报告
export function getReportList(data) {
  return request({ url: `${BASE}/report/list`, method: 'get', params: data })
}
export function addReport(data) {
  return request({ url: `${BASE}/report/create`, method: 'post', data })
}
export function getReportDetail(id) {
  return request({ url: `${BASE}/report/detail/${id}`, method: 'get' })
}

// 整改任务
export function getTaskList(data) {
  return request({ url: `${BASE}/rectification/list`, method: 'get', params: data })
}
export function addTask(data) {
  return request({ url: `${BASE}/rectification/create`, method: 'post', data })
}
export function updateTask(data) {
  return request({ url: `${BASE}/rectification/update`, method: 'put', data })
}
export function completeTask(id) {
  return request({ url: `${BASE}/rectification/complete/${id}`, method: 'post' })
}

// 绩效考核
export function getPerformanceList(data) {
  return request({ url: `${BASE}/assessment/list`, method: 'get', params: data })
}
export function evaluatePerformance(data) {
  return request({ url: `${BASE}/assessment/calculate`, method: 'post', data })
}

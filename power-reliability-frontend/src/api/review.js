import request from '@/utils/request'

const BASE = '/powerReliability/review'

// 复盘报告
export function getReportList(data) {
  return request({ url: `${BASE}/report/list`, method: 'post', data })
}
export function addReport(data) {
  return request({ url: `${BASE}/report/add`, method: 'post', data })
}
export function getReportDetail(id) {
  return request({ url: `${BASE}/report/detail/${id}`, method: 'post' })
}

// 整改任务
export function getTaskList(data) {
  return request({ url: `${BASE}/task/list`, method: 'post', data })
}
export function addTask(data) {
  return request({ url: `${BASE}/task/add`, method: 'post', data })
}
export function updateTask(data) {
  return request({ url: `${BASE}/task/update`, method: 'post', data })
}
export function completeTask(id) {
  return request({ url: `${BASE}/task/complete/${id}`, method: 'post' })
}

// 绩效考核
export function getPerformanceList(data) {
  return request({ url: `${BASE}/performance/list`, method: 'post', data })
}
export function evaluatePerformance(data) {
  return request({ url: `${BASE}/performance/evaluate`, method: 'post', data })
}

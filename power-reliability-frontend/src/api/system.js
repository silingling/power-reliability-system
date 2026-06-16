import request from '@/utils/request'

const BASE = '/powerReliability/system'

// 用户管理
export function getSystemUserList(data) {
  return request({ url: `${BASE}/user/list`, method: 'post', data })
}
export function addSystemUser(data) {
  return request({ url: `${BASE}/user/add`, method: 'post', data })
}
export function updateSystemUser(data) {
  return request({ url: `${BASE}/user/update`, method: 'post', data })
}
export function deleteSystemUser(id) {
  return request({ url: `${BASE}/user/delete/${id}`, method: 'post' })
}
export function resetUserPassword(id) {
  return request({ url: `${BASE}/user/resetPwd/${id}`, method: 'post' })
}

// 角色管理
export function getRoleList(data) {
  return request({ url: `${BASE}/role/list`, method: 'post', data })
}
export function addRole(data) {
  return request({ url: `${BASE}/role/add`, method: 'post', data })
}
export function updateRole(data) {
  return request({ url: `${BASE}/role/update`, method: 'post', data })
}
export function deleteRole(id) {
  return request({ url: `${BASE}/role/delete/${id}`, method: 'post' })
}
export function setRoleMenu(data) {
  return request({ url: `${BASE}/role/setMenu`, method: 'post', data })
}

// 菜单管理
export function getMenuList(data) {
  return request({ url: `${BASE}/menu/list`, method: 'post', data })
}
export function addMenu(data) {
  return request({ url: `${BASE}/menu/add`, method: 'post', data })
}
export function updateMenu(data) {
  return request({ url: `${BASE}/menu/update`, method: 'post', data })
}
export function deleteMenu(id) {
  return request({ url: `${BASE}/menu/delete/${id}`, method: 'post' })
}

// 系统配置
export function getConfigList(data) {
  return request({ url: `${BASE}/config/list`, method: 'post', data })
}
export function updateConfig(data) {
  return request({ url: `${BASE}/config/update`, method: 'post', data })
}

// 操作日志
export function getLogList(data) {
  return request({ url: `${BASE}/log/list`, method: 'post', data })
}

// 部门管理
export function getDeptList() {
  return request({ url: `${BASE}/dept/list`, method: 'post' })
}
export function addDept(data) {
  return request({ url: `${BASE}/dept/add`, method: 'post', data })
}
export function updateDept(data) {
  return request({ url: `${BASE}/dept/update`, method: 'post', data })
}
export function deleteDept(id) {
  return request({ url: `${BASE}/dept/delete/${id}`, method: 'post' })
}

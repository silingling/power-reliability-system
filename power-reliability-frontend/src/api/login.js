import request from '@/utils/request'

export function initUserAPI() {
  return request({
    url: '/api/auth/init',
    method: 'get'
  })
}

export function querySystemStatusAPI() {
  return request({
    url: '/api/auth/status',
    method: 'get'
  })
}

export function verfyCodeAPI() {
  return request({
    url: '/api/auth/verifyCode',
    method: 'get'
  })
}

export function loginAPI(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

export function logoutAPI() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

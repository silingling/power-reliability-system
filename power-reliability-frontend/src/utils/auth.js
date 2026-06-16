import axios from 'axios'
import cache from './cache'
import Lockr from 'lockr'
import store from '@/store'

/** 移除授权信息 */
export function removeAuth() {
  return new Promise((resolve, reject) => {
    cache.rmAxiosCache()
    store.commit('SET_ALLAUTH', null)
    delete axios.defaults.headers['Authorization']
    resolve(true)
  })
}

/** 注入授权信息 (存储JWT token, 发送时添加 Authorization: Bearer 前缀) */
export function addAuth(token) {
  return new Promise((resolve, reject) => {
    Lockr.set('Admin-Token', token)
    axios.defaults.headers['Authorization'] = 'Bearer ' + token
    resolve(true)
  })
}

/** 获取授权状态 */
export function getAuth() {
  /** 从Lockr恢复授权状态 */
  if (Lockr.get('Admin-Token') && !axios.defaults.headers['Authorization']) {
    cache.updateAxiosHeaders()
  }

  if (axios.defaults.headers['Authorization']) {
    return true
  }
  return false
}

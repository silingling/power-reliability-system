import store from '@/store'
import Lockr from 'lockr'
import axios from 'axios'

const cache = {
  /**
   * 载入全部登陆信息
   */
  loadingCache: function() {
    if (Lockr.get('Admin-Token') && !axios.defaults.headers['Authorization']) {
      /** 将用户信息放入缓存 */
      const userInfo = Lockr.get('loginUserInfo')
      if (userInfo) {
        store.commit('SET_USERINFO', userInfo)
      }
    }
    store.commit('SET_APPNAME', Lockr.get('systemName'))
    store.commit('SET_APPLOGO', Lockr.get('systemLogo'))
  },
  /**
   * 请求和更新登录缓存
   */
  updateAxiosCache: function() {
    const token = Lockr.get('Admin-Token')
    if (token) {
      axios.defaults.headers['Authorization'] = 'Bearer ' + token
    }
    store.dispatch('GetUserInfo')
    store.dispatch('SystemLogoAndName')
    store.dispatch('pageDataReflushTime')
    store.dispatch('PM10standard')
  },
  updateAxiosHeaders: function() {
    const token = Lockr.get('Admin-Token')
    if (token) {
      axios.defaults.headers['Authorization'] = 'Bearer ' + token
    }
  },
  /**
   * 移除登录信息
   * @param {*}
   */
  rmAxiosCache: function() {
    Lockr.rm('Admin-Token')
    delete axios.defaults.headers['Authorization']
  }
}

export default cache

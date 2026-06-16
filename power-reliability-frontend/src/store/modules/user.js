import { loginAPI, logoutAPI } from '@/api/login'
import { getAuth, addAuth, removeAuth } from '@/utils/auth'

const user = {
  state: {
    id: '',
    name: '',
    token: getAuth(),
    roles: []
  },
  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_ID: (state, id) => {
      state.id = id
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    }
  },
  actions: {
    Login({ commit }, userInfo) {
      return new Promise((resolve, reject) => {
        loginAPI(userInfo).then(response => {
          const data = response.data || response
          addAuth(data.token)
          commit('SET_TOKEN', true)
          commit('SET_ID', data.userInfo ? data.userInfo.id : '')
          commit('SET_NAME', data.userInfo ? data.userInfo.username : '')
          commit('SET_ROLES', data.permissions || [])
          resolve(response)
        }).catch(error => {
          reject(error)
        })
      })
    },
    getAuth({ commit }) {
      return new Promise((resolve) => {
        const token = getAuth()
        if (token) {
          commit('SET_TOKEN', token)
        }
        resolve({ allAuth: true })
      })
    },
    LogOut({ commit }) {
      return new Promise((resolve) => {
        logoutAPI().catch(() => {})
        removeAuth()
        commit('SET_TOKEN', '')
        commit('SET_ROLES', [])
        resolve()
      })
    }
  }
}

export default user

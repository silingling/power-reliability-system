import { getAuth, setAuth, removeAuth } from '@/utils/auth'

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
        removeAuth()
        commit('SET_TOKEN', '')
        commit('SET_ROLES', [])
        resolve()
      })
    }
  }
}

export default user

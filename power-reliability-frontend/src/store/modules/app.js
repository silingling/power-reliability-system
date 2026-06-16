/** 记录 侧边索引 */
const app = {
  state: {
    sidebar: {
      activeIndex: '',
      collapse: false
    },
    navbar: {
      activeIndex: ''
    },
    name: '供电可靠性系统'
  },
  mutations: {
    SET_ACTIVEINDEX: (state, path) => {
      state.sidebar.activeIndex = path
    },
    SET_COLLAPSE: (state, collapse) => {
      state.sidebar.collapse = collapse
    },
    SET_NAVACTIVEINDEX: (state, path) => {
      state.navbar.activeIndex = path + ''
    },
    SET_APPNAME: (state, name) => {
      state.name = name
    }
  },
  actions: {}
}

export default app

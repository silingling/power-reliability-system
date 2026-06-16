import { asyncRouterMap } from '@/router'

const permission = {
  state: {
    routers: [],
    addRouters: []
  },
  mutations: {
    SET_ROUTERS: (state, routers) => {
      state.addRouters = routers
    }
  },
  actions: {
    GenerateRoutes({ commit }, data) {
      return new Promise(resolve => {
        const accessedRouters = []
        asyncRouterMap.forEach(module => {
          const routes = module.router
          routes.forEach(route => {
            if (route.redirect) {
              const firstChild = route.children ? route.children[0] : null
              if (firstChild) {
                route.redirect = route.path + '/' + firstChild.path
              }
            }
            accessedRouters.push(route)
          })
        })

        // 添加根路径重定向
        const homeRedirect = accessedRouters.length > 0 ? accessedRouters[0].redirect || accessedRouters[0].path : '/dashboard/overview'
        accessedRouters.push({
          path: '/',
          redirect: homeRedirect,
          hidden: true
        })

        commit('SET_ROUTERS', accessedRouters)
        resolve(accessedRouters)
      })
    }
  }
}

export default permission

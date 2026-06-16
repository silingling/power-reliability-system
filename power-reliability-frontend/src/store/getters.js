const getters = {
  sidebar: state => state.app.sidebar,
  navbar: state => state.app.navbar,
  name: state => state.app.name,
  token: state => state.user.token,
  roles: state => state.user.roles,
  addRouters: state => state.permission.addRouters,
  allAuth: state => state.user.token ? { all: true } : null
}
export default getters

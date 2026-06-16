import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

import ledgerRouter from './modules/ledger'
import outageRouter from './modules/outage'
import governanceRouter from './modules/governance'
import indexRouter from './modules/index'
import warningRouter from './modules/warning'
import reviewRouter from './modules/review'
import dashboardRouter from './modules/dashboard'
import systemRouter from './modules/system'

export const constantRouterMap = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },
  {
    path: '*',
    redirect: '/404',
    hidden: true
  }
]

const createRouter = () => new Router({
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRouterMap
})

const router = createRouter()

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher
}

export default router

export const asyncRouterMap = [
  { type: 'ledger', router: ledgerRouter },
  { type: 'outage', router: outageRouter },
  { type: 'governance', router: governanceRouter },
  { type: 'index', router: indexRouter },
  { type: 'warning', router: warningRouter },
  { type: 'review', router: reviewRouter },
  { type: 'dashboard', router: dashboardRouter },
  { type: 'system', router: systemRouter }
]

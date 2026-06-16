/** 台账管理路由 */
import Layout from '@/views/layout/MainLayout'

export default [
  {
    path: '/ledger',
    component: Layout,
    meta: {
      requiresAuth: true,
      permissions: ['ledger']
    },
    alwaysShow: true,
    redirect: '/ledger/station',
    name: 'ledger',
    children: [
      {
        path: 'station',
        name: 'ledgerStation',
        component: () => import('@/views/ledger/station'),
        meta: { title: '台区管理', icon: 'table' }
      },
      {
        path: 'device',
        name: 'ledgerDevice',
        component: () => import('@/views/ledger/device'),
        meta: { title: '设备台账', icon: 'device' }
      },
      {
        path: 'user',
        name: 'ledgerUser',
        component: () => import('@/views/ledger/user'),
        meta: { title: '用户管理', icon: 'user' }
      },
      {
        path: 'line',
        name: 'ledgerLine',
        component: () => import('@/views/ledger/line'),
        meta: { title: '线路管理', icon: 'tree' }
      }
    ]
  }
]

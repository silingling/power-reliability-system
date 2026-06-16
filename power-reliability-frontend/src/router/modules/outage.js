/** 停电事件管理路由 */
import Layout from '@/views/layout/MainLayout'

export default [
  {
    path: '/outage',
    component: Layout,
    meta: {
      requiresAuth: true,
      permissions: ['outage']
    },
    alwaysShow: true,
    redirect: '/outage/list',
    name: 'outage',
    children: [
      {
        path: 'list',
        name: 'outageList',
        component: () => import('@/views/outage/list'),
        meta: { title: '停电事件列表', icon: 'list' }
      },
      {
        path: 'planned',
        name: 'outagePlanned',
        component: () => import('@/views/outage/planned'),
        meta: { title: '计划停电', icon: 'time' }
      },
      {
        path: 'fault',
        name: 'outageFault',
        component: () => import('@/views/outage/fault'),
        meta: { title: '故障停电', icon: 'warning' }
      },
      {
        path: 'exemption',
        name: 'outageExemption',
        component: () => import('@/views/outage/exemption'),
        meta: { title: '豁免管理', icon: 'circle-check' }
      },
      {
        path: 'repair',
        name: 'outageRepair',
        component: () => import('@/views/outage/repair'),
        meta: { title: '抢修工单', icon: 'document' }
      }
    ]
  }
]

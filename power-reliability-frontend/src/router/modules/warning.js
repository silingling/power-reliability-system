/** 隐患预判与预警路由 */
import Layout from '@/views/layout/MainLayout'

export default [
  {
    path: '/warning',
    component: Layout,
    meta: {
      requiresAuth: true,
      permissions: ['warning']
    },
    alwaysShow: true,
    redirect: '/warning/prediction',
    name: 'warningMgt',
    children: [
      {
        path: 'prediction',
        name: 'warningPrediction',
        component: () => import('@/views/warning/prediction'),
        meta: { title: '隐患预判', icon: 'warning-filled' }
      },
      {
        path: 'order',
        name: 'warningOrder',
        component: () => import('@/views/warning/order'),
        meta: { title: '预警工单', icon: 'bell' }
      }
    ]
  }
]

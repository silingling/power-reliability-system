/** 频繁停电治理路由 */
import Layout from '@/views/layout/MainLayout'

export default [
  {
    path: '/governance',
    component: Layout,
    meta: {
      requiresAuth: true,
      permissions: ['governance']
    },
    alwaysShow: true,
    redirect: '/governance/list',
    name: 'governance',
    children: [
      {
        path: 'list',
        name: 'governanceList',
        component: () => import('@/views/governance/list'),
        meta: { title: '频繁停电台账', icon: 'table' }
      },
      {
        path: 'analysis',
        name: 'governanceAnalysis',
        component: () => import('@/views/governance/analysis'),
        meta: { title: '原因分析', icon: 'search' }
      },
      {
        path: 'order',
        name: 'governanceOrder',
        component: () => import('@/views/governance/order'),
        meta: { title: '治理工单', icon: 'document' }
      },
      {
        path: 'result',
        name: 'governanceResult',
        component: () => import('@/views/governance/result'),
        meta: { title: '成效核验', icon: 'circle-check' }
      }
    ]
  }
]

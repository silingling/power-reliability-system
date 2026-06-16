/** 复盘考核路由 */
import Layout from '@/views/layout/MainLayout'

export default [
  {
    path: '/review',
    component: Layout,
    meta: {
      requiresAuth: true,
      permissions: ['review']
    },
    alwaysShow: true,
    redirect: '/review/report',
    name: 'review',
    children: [
      {
        path: 'report',
        name: 'reviewReport',
        component: () => import('@/views/review/report'),
        meta: { title: '复盘报告', icon: 'document' }
      },
      {
        path: 'task',
        name: 'reviewTask',
        component: () => import('@/views/review/task'),
        meta: { title: '整改任务', icon: 'list' }
      },
      {
        path: 'performance',
        name: 'reviewPerformance',
        component: () => import('@/views/review/performance'),
        meta: { title: '绩效考核', icon: 'data-board' }
      }
    ]
  }
]

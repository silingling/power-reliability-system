/** 可靠性指标路由 */
import Layout from '@/views/layout/MainLayout'

export default [
  {
    path: '/index-mgt',
    component: Layout,
    meta: {
      requiresAuth: true,
      permissions: ['index']
    },
    alwaysShow: true,
    redirect: '/index-mgt/statistics',
    name: 'indexMgt',
    children: [
      {
        path: 'statistics',
        name: 'indexStatistics',
        component: () => import('@/views/index-mgt/statistics'),
        meta: { title: '指标核算', icon: 'data-analysis' }
      },
      {
        path: 'trend',
        name: 'indexTrend',
        component: () => import('@/views/index-mgt/trend'),
        meta: { title: '趋势分析', icon: 'trend' }
      },
      {
        path: 'compare',
        name: 'indexCompare',
        component: () => import('@/views/index-mgt/compare'),
        meta: { title: '对比分析', icon: 'data-board' }
      },
      {
        path: 'warning',
        name: 'indexWarning',
        component: () => import('@/views/index-mgt/warning'),
        meta: { title: '预警列表', icon: 'bell' }
      }
    ]
  }
]

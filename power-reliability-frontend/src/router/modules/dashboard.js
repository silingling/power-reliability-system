/** 可视化大屏路由 */
import Layout from '@/views/layout/MainLayout'

export default [
  {
    path: '/dashboard',
    component: Layout,
    meta: {
      requiresAuth: true,
      permissions: ['dashboard']
    },
    alwaysShow: true,
    redirect: '/dashboard/overview',
    name: 'dashboard',
    children: [
      {
        path: 'overview',
        name: 'dashboardOverview',
        component: () => import('@/views/dashboard/overview'),
        meta: { title: '总览大屏', icon: 'board' }
      }
    ]
  }
]

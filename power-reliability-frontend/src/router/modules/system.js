/** 系统管理路由 */
import LayoutAdmin from '@/views/layout/AdminLayout'

export default [
  {
    path: '/system',
    component: LayoutAdmin,
    meta: {
      requiresAuth: true,
      permissions: ['system']
    },
    alwaysShow: true,
    redirect: '/system/user',
    name: 'system',
    children: [
      {
        path: 'user',
        name: 'systemUser',
        component: () => import('@/views/system/user'),
        meta: { title: '用户管理', icon: 'user' }
      },
      {
        path: 'role',
        name: 'systemRole',
        component: () => import('@/views/system/role'),
        meta: { title: '角色管理', icon: 'user' }
      },
      {
        path: 'menu',
        name: 'systemMenu',
        component: () => import('@/views/system/menu'),
        meta: { title: '菜单管理', icon: 'tree' }
      },
      {
        path: 'config',
        name: 'systemConfig',
        component: () => import('@/views/system/config'),
        meta: { title: '系统配置', icon: 'setting' }
      },
      {
        path: 'log',
        name: 'systemLog',
        component: () => import('@/views/system/log'),
        meta: { title: '操作日志', icon: 'document' }
      },
      {
        path: 'dept',
        name: 'systemDept',
        component: () => import('@/views/system/dept'),
        meta: { title: '部门管理', icon: 'tree' }
      }
    ]
  }
]

import router from './router'
import store from './store'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { Message } from 'element-ui'
import { getAuth } from '@/utils/auth'

let loadAsyncRouter = false
const whiteList = ['/login']

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getAuth()) {
    if (whiteList.includes(to.path)) {
      next({ path: '/' })
      NProgress.done()
    } else {
      if (!loadAsyncRouter) {
        loadAsyncRouter = true
        store.dispatch('GenerateRoutes').then((addRouters) => {
          router.addRoutes(addRouters)
          next({ ...to, replace: true })
        }).catch((err) => {
          loadAsyncRouter = false
          store.dispatch('LogOut').then(() => {
            Message.error(err || '获取用户信息失败')
            next({ path: '/' })
          })
        })
      } else {
        next()
      }
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next('/login')
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

router.onError((error) => {
  const pattern = /Loading chunk (\d)+ failed/g
  const isChunkLoadFailed = error.message.match(pattern)
  const targetPath = router.history.pending ? router.history.pending.fullPath : '/'
  if (isChunkLoadFailed) {
    router.replace(targetPath)
  }
})

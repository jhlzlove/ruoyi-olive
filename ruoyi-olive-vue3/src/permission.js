import usePermissionStore from '@/store/modules/permission'
import useSettingsStore from '@/store/modules/settings'
import useUserStore from '@/store/modules/user'
import { getToken } from '@/utils/auth'
import { isRelogin } from '@/utils/request'
import { isHttp } from '@/utils/validate'
import { ElMessage } from 'element-plus'
import * as minimatch from "minimatch"
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import router from './router'

NProgress.configure({ showSpinner: false });

const whiteList = ['/login', '/auth-redirect', '/bind', '/register'];
const whiteListPatterns = whiteList.map(
  (pattern) => new minimatch.Minimatch(pattern)
);

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    let title = typeof to.meta.title === 'function' ? to.meta.title(to) : to.meta.title
    title && useSettingsStore().setTitle(title)
    
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    /* has token*/
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    }
    else if (whiteListPatterns.some((pattern) => pattern.match(to.path))) {
      // 在免登录白名单，直接进入
      next()
    }
    else {
      if (useUserStore().roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        useUserStore().getInfo().then(() => {
          isRelogin.show = false
          usePermissionStore().generateRoutes().then(accessRoutes => {
            // 根据roles权限生成可访问的路由表
            accessRoutes.forEach(route => {
              if (!isHttp(route.path)) {
                router.addRoute(route) // 动态添加可访问路由表
              }
            })
            next({ ...to, replace: true }) // hack方法 确保addRoutes已完成

            // // 去掉首页，直接进入导航第一个路由
            // let path = '';
            // path = accessRoutes[0].path  + accessRoutes[0].children[0].path //获取第一路由路径
            // if (accessRoutes[0].children[0].query !== undefined) { //如果当前路由存在路由参数，则带入
            //   let query = JSON.parse(accessRoutes[0].children[0].query);
            //   let temp = '';
            //   for (var val in query) {
            //   if (temp.length == 0) {
            //     temp = "?";
            //   } else {
            //     temp = temp + "&";
            //   }
            //     temp = temp + val + "=" + query[val];
            //   }
            //     path = path + temp;
            // }
            // var temp = {...to, replace: true};
            // var curPath = temp.path;
            // if (from.path == '/login' || curPath == "/" ) {
            //   next({path, replace: true}) // hack方法 确保addRoutes已完成
            // } else {
            //   next(temp) // hack方法 确保addRoutes已完成
            // }

          })
        }).catch(err => {
          useUserStore().logOut().then(() => {
            ElMessage.error(err)
            next({ path: '/' })
          })
        })
      } else {
        next()
      }
    }
  } else {
    // 没有token
    if (whiteListPatterns.some((pattern) => pattern.match(to.path))) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

import { getInfo, login, logout } from '@/api/login'
import defAva from '@/assets/images/profile.jpg'
import { getToken, removeToken, setToken } from '@/utils/auth'
import { defineStore } from 'pinia'

export interface LoginForm {
  username: string
  password: string
  code: string
  uuid: string
}

const useUserStore = defineStore(
  'user',
  {
    state: () => ({
      token: getToken(),
      name: '',
      avatar: '',
      roles: Array(),
      permissions: []
    }),
    actions: {
      // 登录
      login(userInfo: LoginForm) {
        const username = userInfo.username.trim()
        const password = userInfo.password
        const code = userInfo.code
        const uuid = userInfo.uuid
        return new Promise((resolve, reject) => {
          login(username, password, code, uuid).then((res:any) => {
            setToken(res.data.token)
            this.token = res.data.token
            resolve(null)
          }).catch(error => {
            console.log(error);
            reject(error)
          })
        })
      },
      // 获取用户信息
      getInfo() {
        return new Promise((resolve, reject) => {
          getInfo().then((res:any) => {
            const user = res.data.user
            // @ts-ignore
            const avatar = (user.avatar == "" || user.avatar == null) ? defAva : import.meta.env.VITE_APP_BASE_API + user.avatar;

            if (res.data.roles && res.data.roles.length > 0) { // 验证返回的roles是否是一个非空数组
              this.roles = res.data.roles
              this.permissions = res.data.permissions
            } else {
              this.roles = ['ROLE_DEFAULT']
            }
            this.name = user.userName
            this.avatar = avatar;
            resolve(res)
          }).catch(error => {
            console.log(`getInfo errorr: ${error}`);
            reject(error)
          })
        })
      },
      // 退出系统
      logOut() {
        return new Promise((resolve, reject) => {
          logout().then(() => {
            this.token = ''
            this.roles = []
            this.permissions = []
            removeToken()
            resolve(null)
          }).catch(error => {
            reject(error)
          })
        })
      }
    }
  })

export default useUserStore

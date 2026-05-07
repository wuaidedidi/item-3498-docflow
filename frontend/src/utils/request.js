import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import router from '@/router'

const recentMessages = new Set()

const showError = (message) => {
  if (!message || recentMessages.has(message)) return
  recentMessages.add(message)
  setTimeout(() => recentMessages.delete(message), 2000)
  Message.error({ message, duration: 3000 })
}

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

service.interceptors.request.use(
  config => {
    const token = store.getters.token
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      showError(res.message || '操作失败')
      const error = new Error(res.message || '操作失败')
      error._isBusinessError = true
      return Promise.reject(error)
    }
    return res
  },
  error => {
    if (error._isBusinessError) {
      return Promise.reject(error)
    }

    if (error.response) {
      const status = error.response.status
      const data = error.response.data

      if (status === 401) {
        showError('登录已过期，请重新登录')
        store.dispatch('logout')
        router.push('/login')
      } else if (status === 403) {
        showError('权限不足，无法执行此操作')
      } else if (data && data.message) {
        showError(data.message)
      } else {
        showError('服务器错误，请稍后重试')
      }
    } else if (error.code === 'ECONNABORTED') {
      showError('请求超时，请重试')
    } else if (error.code === 'ERR_CANCELED') {
      // request canceled, no message needed
    } else {
      showError('网络异常，请检查网络连接后重试')
    }

    return Promise.reject(error)
  }
)

export default service

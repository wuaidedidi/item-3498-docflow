import request from '@/utils/request'

export function getCurrentUser() {
  return request({ url: '/users/me', method: 'get' })
}

export function updateCurrentUser(data) {
  return request({ url: '/users/me', method: 'put', data })
}

export function updatePassword(data) {
  return request({ url: '/users/me/password', method: 'put', data })
}

export function getUserList(params) {
  return request({ url: '/users', method: 'get', params })
}

export function getAllUsers() {
  return request({ url: '/users/all', method: 'get' })
}

export function updateUser(id, data) {
  return request({ url: `/users/${id}`, method: 'put', data })
}

export function deleteUser(id) {
  return request({ url: `/users/${id}`, method: 'delete' })
}

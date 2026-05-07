import request from '@/utils/request'

export function getDocumentList(params) {
  return request({ url: '/documents', method: 'get', params })
}

export function getDocumentDetail(id) {
  return request({ url: `/documents/${id}`, method: 'get' })
}

export function createDocument(data) {
  return request({
    url: '/documents',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export function updateDocument(id, data) {
  return request({ url: `/documents/${id}`, method: 'put', params: data })
}

export function uploadNewVersion(id, data) {
  return request({
    url: `/documents/${id}/versions`,
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export function getDocumentVersions(id) {
  return request({ url: `/documents/${id}/versions`, method: 'get' })
}

export function deleteDocument(id) {
  return request({ url: `/documents/${id}`, method: 'delete' })
}

export function getDownloadUrl(filePath, fileName) {
  return `/api/documents/download/${filePath}?fileName=${encodeURIComponent(fileName || '')}`
}

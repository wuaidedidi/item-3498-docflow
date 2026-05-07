import request from '@/utils/request'

export function getFlowList(params) {
  return request({ url: '/approval/flows', method: 'get', params })
}

export function getFlowDetail(id) {
  return request({ url: `/approval/flows/${id}`, method: 'get' })
}

export function createFlow(data) {
  return request({ url: '/approval/flows', method: 'post', data })
}

export function updateFlow(id, data) {
  return request({ url: `/approval/flows/${id}`, method: 'put', data })
}

export function deleteFlow(id) {
  return request({ url: `/approval/flows/${id}`, method: 'delete' })
}

export function toggleFlowStatus(id) {
  return request({ url: `/approval/flows/${id}/toggle`, method: 'put' })
}

export function getMyApprovals(params) {
  return request({ url: '/approval/records/my', method: 'get', params })
}

export function getDocumentApprovalHistory(documentId) {
  return request({ url: `/approval/records/document/${documentId}`, method: 'get' })
}

export function processApproval(data) {
  return request({ url: '/approval/process', method: 'post', data })
}

export function getApprovalStats() {
  return request({ url: '/approval/stats', method: 'get' })
}

import request from '@/utils/request'

export function listKbDocument(query) {
  return request({
    url: '/kb/document/list',
    method: 'get',
    params: query
  })
}

export function getKbDocument(docId) {
  return request({
    url: '/kb/document/' + docId,
    method: 'get'
  })
}

export function previewKbDocument(docId) {
  return request({
    url: '/kb/document/' + docId + '/preview',
    method: 'get'
  })
}

export function statsKbDocument(params) {
  return request({
    url: '/kb/document/stats',
    method: 'get',
    params
  })
}

export function getKbModules() {
  return request({
    url: '/kb/document/modules',
    method: 'get'
  })
}

export function uploadKbDocument(data) {
  return request({
    url: '/kb/document/upload',
    method: 'post',
    data
  })
}

export function delKbDocument(docId) {
  return request({
    url: '/kb/document/' + docId,
    method: 'delete'
  })
}

export function reindexKbDocument(docId) {
  return request({
    url: '/kb/document/reindex/' + docId,
    method: 'post'
  })
}

export function updateKbDocumentStatus(docId, status) {
  return request({
    url: '/kb/document/status/' + docId,
    method: 'put',
    params: { status }
  })
}

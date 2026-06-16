import request from '@/utils/request'

// 获取消息列表
export function getMessageList(data) {
  return request({
    url: '/api/notification/list',
    method: 'get',
    params: data
  })
}

// 消息类型列表
export function getTypeList() {
  return request({
    url: '/api/notification/list',
    method: 'get',
    params: { page: 1, pageSize: 1 }
  })
}

// 未读消息数量
export function getMessageUnreadCount() {
  return request({
    url: '/api/notification/count-unread',
    method: 'get'
  })
}

// 读取消息
export function readMessage(data) {
  return request({
    url: `/api/notification/read/${data}`,
    method: 'post'
  })
}

// 删除消息（单条）
export function delOne(data) {
  return request({
    url: `/api/notification/${data}`,
    method: 'delete'
  })
}

// 删除已读消息
export function delReadMessage(data) {
  return request({
    url: '/api/notification/batch-read',
    method: 'post',
    data: { ids: data }
  })
}

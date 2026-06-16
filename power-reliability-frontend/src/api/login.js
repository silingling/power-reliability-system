import request from '@/utils/request'
export function initUserAPI() { return Promise.resolve({ data: {} }) }
export function querySystemStatusAPI() { return Promise.resolve({ data: {} }) }
export function verfyCodeAPI() { return Promise.resolve({}) }
export function loginAPI(data) { return Promise.resolve({ data: { token: 'mock-token' } }) }
export function logoutAPI() { return Promise.resolve({}) }

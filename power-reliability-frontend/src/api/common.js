import request from '@/utils/request'
export function adminGetBTypeAPI() { return Promise.resolve({ data: [] }) }
export function userListAPI() { return Promise.resolve({ data: [] }) }
export function depListAPI() { return Promise.resolve({ data: [] }) }
export function downloadFileAPI() { return Promise.resolve({}) }
export function crmFileSaveAPI() { return Promise.resolve({ data: { fileId: '' } }) }
export function crmFileSaveUrl() { return '' }
export function crmFileDeleteAPI() { return Promise.resolve({}) }
export function webFileSaveAPI() { return Promise.resolve({ data: { url: '' } }) }
export function systemUserInfoAPI() { return Promise.resolve({ data: {} }) }
export function systemMessageListAPI() { return Promise.resolve({ data: { list: [] } }) }
export function systemMessageDeleteByIdAPI() { return Promise.resolve({}) }
export function excelImportAPI() { return Promise.resolve({}) }
export function excelImpModelExport() { return Promise.resolve({}) }
export function adminFileQueryFileListAPI() { return Promise.resolve({ data: [] }) }
export function adminRouterListAPI() { return Promise.resolve({ data: { routers: [], addRouters: [], firstRoute: null } }) }
export function getDeptPid() { return Promise.resolve({ data: {} }) }
export function adminGetBTypeListAPI() { return Promise.resolve({ data: [] }) }

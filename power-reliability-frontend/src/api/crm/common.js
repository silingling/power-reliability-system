// CRM common.js - stubs for legacy CRM components
import request from '@/utils/request'

export function crmDownImportErrorAPI() { return Promise.resolve({ data: [] }) }
export function filedValidatesAPI() { return Promise.resolve({ data: [] }) }
export function crmSceneIndexAPI() { return Promise.resolve({ data: { list: [], total: 0 } }) }
export function sysConfigQueryPhraseAPI() { return Promise.resolve({ data: [] }) }
export function sysConfigSetPhraseAPI() { return Promise.resolve({}) }
export default {}

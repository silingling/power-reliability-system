<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="操作用户"><el-input v-model="query.operator" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="操作类型"><el-select v-model="query.operation" placeholder="请选择" clearable><el-option label="新增" value="create" /><el-option label="修改" value="update" /><el-option label="删除" value="delete" /><el-option label="查询" value="query" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="operator" label="操作用户" width="120" />
      <el-table-column prop="operation" label="操作类型" width="100" /><el-table-column prop="module" label="操作模块" width="150" />
      <el-table-column prop="detail" label="操作详情" min-width="300" /><el-table-column prop="ip" label="IP地址" width="140" />
      <el-table-column prop="createTime" label="操作时间" width="170" />
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
  </div>
</template>
<script>
import { getLogList } from '@/api/system'
export default {
  name: 'SystemLog', data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { operator: '', operation: '' } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getLogList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { operator: '', operation: '' }; this.handleSearch() }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
</style>

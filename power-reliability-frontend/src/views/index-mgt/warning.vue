<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="预警级别"><el-select v-model="query.level" placeholder="请选择" clearable><el-option label="红色" value="red" /><el-option label="橙色" value="orange" /><el-option label="黄色" value="yellow" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="stationName" label="台区名称" width="140" />
      <el-table-column prop="indicator" label="预警指标" width="120" /><el-table-column prop="currentValue" label="当前值" width="100" /><el-table-column prop="threshold" label="阈值" width="100" />
      <el-table-column prop="level" label="预警级别" width="100"><template slot-scope="{row}"><el-tag :type="{red:'danger',orange:'warning',yellow:''}[row.level]" size="small">{{ {red:'红色',orange:'橙色',yellow:'黄色'}[row.level]||row.level }}</el-tag></template></el-table-column>
      <el-table-column prop="alertTime" label="预警时间" width="160" /><el-table-column prop="status" label="状态" width="80"><template slot-scope="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'已处理':'未处理' }}</el-tag></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
  </div>
</template>
<script>
import { getIndexWarningList } from '@/api/index'
export default {
  name: 'IndexWarning', data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '', level: '' } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getIndexWarningList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '', level: '' }; this.handleSearch() }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
</style>

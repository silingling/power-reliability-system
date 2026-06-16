<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="stationName" label="台区名称" width="140" />
      <el-table-column prop="outageCount" label="停电次数" width="100" /><el-table-column prop="reason" label="主要原因" min-width="250" /><el-table-column prop="reasonCategory" label="原因分类" width="120" /><el-table-column prop="suggestion" label="治理建议" min-width="250" />
      <el-table-column label="操作" width="120" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleDetail(row)">详情</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
  </div>
</template>
<script>
import { getAnalysisList } from '@/api/governance'
export default {
  name: 'GovernanceAnalysis', data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '' } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getAnalysisList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '' }; this.handleSearch() },
    handleDetail() { this.$message.info('详情功能待接入后端') }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
</style>

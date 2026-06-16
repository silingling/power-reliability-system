<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="风险等级"><el-select v-model="query.riskLevel" placeholder="请选择" clearable><el-option label="高" value="high" /><el-option label="中" value="medium" /><el-option label="低" value="low" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="stationName" label="台区名称" width="140" />
      <el-table-column prop="riskType" label="隐患类型" width="120" /><el-table-column prop="riskDesc" label="隐患描述" min-width="250" />
      <el-table-column prop="riskLevel" label="风险等级" width="100"><template slot-scope="{row}"><el-tag :type="{high:'danger',medium:'warning',low:'info'}[row.riskLevel]" size="small">{{ {high:'高',medium:'中',low:'低'}[row.riskLevel]||row.riskLevel }}</el-tag></template></el-table-column>
      <el-table-column prop="predictTime" label="预判时间" width="160" /><el-table-column prop="accuracy" label="准确率(%)" width="100" />
      <el-table-column label="操作" width="150" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleDetail(row)">详情</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
  </div>
</template>
<script>
import { getPredictionList } from '@/api/warning'
export default {
  name: 'WarningPrediction', data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '', riskLevel: '' } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getPredictionList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '', riskLevel: '' }; this.handleSearch() },
    handleDetail() { this.$message.info('详情功能待接入后端') }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
</style>

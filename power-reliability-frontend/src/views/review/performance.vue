<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="考核周期"><el-select v-model="query.period" placeholder="请选择"><el-option label="月度" value="month" /><el-option label="季度" value="quarter" /><el-option label="年度" value="year" /></el-select></el-form-item>
        <el-form-item label="年份"><el-date-picker v-model="query.year" type="year" value-format="yyyy" placeholder="选择年份" /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="deptName" label="部门/单位" width="140" />
      <el-table-column prop="asi" label="ASI得分" width="100" /><el-table-column prop="saidi" label="SAIDI得分" width="110" /><el-table-column prop="saifi" label="SAIFI得分" width="110" />
      <el-table-column prop="governance" label="治理得分" width="100" /><el-table-column prop="totalScore" label="总分" width="100"><template slot-scope="{row}"><el-tag type="primary" size="medium">{{ row.totalScore }}</el-tag></template></el-table-column>
      <el-table-column prop="rank" label="排名" width="70" /><el-table-column prop="evaluator" label="评估人" width="120" />
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
  </div>
</template>
<script>
import { getPerformanceList } from '@/api/review'
export default {
  name: 'ReviewPerformance', data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { period: 'month', year: '2026' } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getPerformanceList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
</style>

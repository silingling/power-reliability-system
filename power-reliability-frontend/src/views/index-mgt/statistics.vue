<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="统计周期"><el-select v-model="query.period" placeholder="请选择"><el-option label="月度" value="month" /><el-option label="季度" value="quarter" /><el-option label="年度" value="year" /></el-select></el-form-item>
        <el-form-item label="年份"><el-date-picker v-model="query.year" type="year" value-format="yyyy" placeholder="选择年份" /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button></el-form-item>
      </el-form>
    </div>
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6" v-for="(item,i) in kpiCards" :key="i">
        <el-card shadow="hover"><div class="kpi-value">{{ item.value }}</div><div class="kpi-label">{{ item.label }}</div></el-card>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="stationName" label="台区名称" width="140" /><el-table-column prop="totalOutages" label="停电次数" width="100" /><el-table-column prop="totalDuration" label="停电时长(min)" width="120" />
      <el-table-column prop="asi" label="ASI" width="100" /><el-table-column prop="saidi" label="SAIDI" width="100" /><el-table-column prop="saifi" label="SAIFI" width="100" /><el-table-column prop="caidi" label="CAIDI" width="100" />
      <el-table-column prop="rs" label="RS(%)" width="100" />
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
  </div>
</template>
<script>
import { getIndexStatistics, calculateIndex } from '@/api/index'
export default {
  name: 'IndexStatistics',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { period: 'month', year: '2026' }, kpiCards: [{ label: 'ASI(平均停电次数)', value: '-' }, { label: 'SAIDI(平均停电时长)', value: '-' }, { label: 'SAIFI(系统平均停电频率)', value: '-' }, { label: 'RS(供电可靠率)', value: '-' }] } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getIndexStatistics({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0; if (r.data.kpi) { this.kpiCards = r.data.kpi } }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.kpi-value { font-size: 28px; font-weight: bold; color: #409eff; text-align: center; }
.kpi-label { text-align: center; color: #666; margin-top: 8px; font-size: 14px; }
</style>

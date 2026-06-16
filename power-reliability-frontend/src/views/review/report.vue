<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="报告名称"><el-input v-model="query.reportName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="时间段"><el-date-picker v-model="query.period" type="monthrange" value-format="yyyy-MM" range-separator="至" start-placeholder="开始月份" end-placeholder="结束月份" /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新建报告</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="reportName" label="报告名称" min-width="200" /><el-table-column prop="period" label="统计周期" width="180" />
      <el-table-column prop="asi" label="ASI" width="80" /><el-table-column prop="saidi" label="SAIDI" width="80" /><el-table-column prop="rs" label="RS(%)" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="160" /><el-table-column prop="status" label="状态" width="80"><template slot-scope="{row}"><el-tag :type="row.status===1?'success':'primary'" size="small">{{ row.status===1?'已发布':'草稿' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="150" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleDetail(row)">查看</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
  </div>
</template>
<script>
import { getReportList, addReport } from '@/api/review'
export default {
  name: 'ReviewReport', data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { reportName: '', period: [] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getReportList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { reportName: '', period: [] }; this.handleSearch() },
    handleAdd() { this.$prompt('请输入报告名称','新建报告').then(({value})=>{ addReport({reportName:value}).then(()=>{ this.$message.success('创建成功');this.fetchList() }) }).catch(()=>{}) },
    handleDetail() { this.$message.info('查看功能待接入后端') }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>

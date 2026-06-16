<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="频繁等级"><el-select v-model="query.level" placeholder="请选择" clearable><el-option label="严重" value="severe" /><el-option label="中度" value="moderate" /><el-option label="一般" value="mild" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="stationName" label="台区名称" width="140" />
      <el-table-column prop="totalOutages" label="停电次数" width="100" /><el-table-column prop="totalDuration" label="累计时长(min)" width="120" /><el-table-column prop="avgDuration" label="平均时长(min)" width="120" />
      <el-table-column prop="level" label="频繁等级" width="100"><template slot-scope="{row}"><el-tag :type="{severe:'danger',moderate:'warning',mild:'info'}[row.level]" size="small">{{ {severe:'严重',moderate:'中度',mild:'一般'}[row.level]||row.level }}</el-tag></template></el-table-column>
      <el-table-column prop="status" label="治理状态" width="100"><template slot-scope="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'已治理':'未治理' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="150" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleDetail(row)">详情</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
  </div>
</template>
<script>
import { getGovernanceList } from '@/api/governance'
export default {
  name: 'GovernanceList', data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '', level: '' } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getGovernanceList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '', level: '' }; this.handleSearch() },
    handleDetail() { this.$message.info('详情功能待接入后端') }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
</style>

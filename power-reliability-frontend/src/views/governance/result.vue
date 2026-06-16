<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="核验状态"><el-select v-model="query.status" placeholder="请选择" clearable><el-option label="待核验" value="pending" /><el-option label="已通过" value="passed" /><el-option label="未通过" value="failed" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="stationName" label="台区名称" width="140" />
      <el-table-column prop="beforeOutages" label="治理前次数" width="110" /><el-table-column prop="afterOutages" label="治理后次数" width="110" /><el-table-column prop="reductionRate" label="降幅(%)" width="100" />
      <el-table-column prop="status" label="核验状态" width="100"><template slot-scope="{row}"><el-tag :type="{pending:'warning',passed:'success',failed:'danger'}[row.status]" size="small">{{ {pending:'待核验',passed:'已通过',failed:'未通过'}[row.status]||row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="150" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleVerify(row)">{{ row.status==='pending'?'核验':'查看' }}</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
  </div>
</template>
<script>
import { getResultList, verifyResult } from '@/api/governance'
export default {
  name: 'GovernanceResult', data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '', status: '' } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getResultList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '', status: '' }; this.handleSearch() },
    handleVerify(row) { this.$confirm('确认核验？','提示').then(()=>{ verifyResult(row.id).then(()=>{ this.$message.success('核验完成');this.fetchList() }) }).catch(()=>{}) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
</style>

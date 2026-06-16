<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="停电类型"><el-select v-model="query.outageType" placeholder="请选择" clearable><el-option label="计划停电" value="planned" /><el-option label="故障停电" value="fault" /><el-option label="临时停电" value="temporary" /><el-option label="限电" value="curtailment" /></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="请选择" clearable><el-option label="待处理" value="pending" /><el-option label="处理中" value="processing" /><el-option label="已恢复" value="restored" /><el-option label="已归档" value="archived" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="stationName" label="台区名称" min-width="130" />
      <el-table-column prop="outageType" label="停电类型" width="100"><template slot-scope="{row}">{{ {planned:'计划停电',fault:'故障停电',temporary:'临时停电',curtailment:'限电'}[row.outageType]||row.outageType }}</template></el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="160" /><el-table-column prop="endTime" label="恢复时间" width="160" /><el-table-column prop="duration" label="持续时间(min)" width="120" /><el-table-column prop="affectedUsers" label="影响用户数" width="100" />
      <el-table-column prop="status" label="状态" width="90"><template slot-scope="{row}"><el-tag :type="{pending:'warning',processing:'primary',restored:'success',archived:'info'}[row.status]||'info'" size="small">{{ {pending:'待处理',processing:'处理中',restored:'已恢复',archived:'已归档'}[row.status]||row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleDetail(row)">详情</el-button><el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
  </div>
</template>
<script>
import { getOutageList, deleteOutage } from '@/api/outage'
export default {
  name: 'OutageList', data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '', outageType: '', status: '' } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getOutageList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '', outageType: '', status: '' }; this.handleSearch() },
    handleDetail() { this.$message.info('详情功能待接入后端') },
    handleDelete(row) { this.$confirm('确认删除？','提示',{type:'warning'}).then(()=>{ deleteOutage(row.id).then(()=>{ this.$message.success('删除成功');this.fetchList() }) }).catch(()=>{}) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
</style>

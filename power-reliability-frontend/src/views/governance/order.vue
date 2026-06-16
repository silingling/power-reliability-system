<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="请选择" clearable><el-option label="待执行" value="pending" /><el-option label="执行中" value="processing" /><el-option label="已完成" value="completed" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新建治理工单</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="orderCode" label="工单编号" width="150" /><el-table-column prop="stationName" label="台区名称" width="140" />
      <el-table-column prop="measure" label="治理措施" min-width="220" /><el-table-column prop="responsible" label="责任人" width="120" /><el-table-column prop="deadline" label="完成期限" width="120" />
      <el-table-column prop="status" label="状态" width="90"><template slot-scope="{row}"><el-tag :type="{pending:'danger',processing:'warning',completed:'success'}[row.status]" size="small">{{ {pending:'待执行',processing:'执行中',completed:'已完成'}[row.status]||row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" @click="handleExecute(row)">执行</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新建治理工单':'编辑治理工单'" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工单编号" prop="orderCode"><el-input v-model="form.orderCode" /></el-form-item>
        <el-form-item label="台区名称" prop="stationName"><el-input v-model="form.stationName" /></el-form-item>
        <el-form-item label="治理措施" prop="measure"><el-input v-model="form.measure" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="责任人"><el-input v-model="form.responsible" /></el-form-item>
        <el-form-item label="完成期限"><el-date-picker v-model="form.deadline" type="date" value-format="yyyy-MM-dd" style="width:100%" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getGovernOrderList, addGovernOrder, updateGovernOrder, executeGovernOrder } from '@/api/governance'
export default {
  name: 'GovernanceOrder',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '', status: '' }, dialogVisible: false, dialogType: 'add', form: { orderCode: '', stationName: '', measure: '', responsible: '', deadline: '' }, rules: { orderCode: [{ required: true, trigger: 'blur' }], stationName: [{ required: true, trigger: 'blur' }], measure: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getGovernOrderList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '', status: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { orderCode: '', stationName: '', measure: '', responsible: '', deadline: '' }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleExecute(row) { this.$confirm('确认执行？','提示').then(()=>{ executeGovernOrder(row.id).then(()=>{ this.$message.success('执行成功');this.fetchList() }) }).catch(()=>{}) },
    handleSave() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addGovernOrder : updateGovernOrder)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>

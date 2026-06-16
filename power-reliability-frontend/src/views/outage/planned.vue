<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="计划编号"><el-input v-model="query.planCode" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="请选择" clearable><el-option label="待审批" value="pending" /><el-option label="已批准" value="approved" /><el-option label="已执行" value="executed" /><el-option label="已取消" value="cancelled" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新建计划停电</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="planCode" label="计划编号" width="150" /><el-table-column prop="stationName" label="台区名称" min-width="130" />
      <el-table-column prop="plannedTime" label="计划时间" width="160" /><el-table-column prop="duration" label="预计时长(min)" width="120" /><el-table-column prop="reason" label="停电原因" min-width="200" />
      <el-table-column prop="status" label="状态" width="90"><template slot-scope="{row}"><el-tag :type="{pending:'warning',approved:'primary',executed:'success',cancelled:'info'}[row.status]||'info'" size="small">{{ {pending:'待审批',approved:'已批准',executed:'已执行',cancelled:'已取消'}[row.status]||row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" @click="handleSubmitApproval(row)">提交审批</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新建计划停电':'编辑计划停电'" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="计划编号" prop="planCode"><el-input v-model="form.planCode" /></el-form-item>
        <el-form-item label="台区名称" prop="stationName"><el-input v-model="form.stationName" /></el-form-item>
        <el-form-item label="计划时间" prop="plannedTime"><el-date-picker v-model="form.plannedTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="预计时长(min)"><el-input-number v-model="form.duration" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="停电原因" prop="reason"><el-input v-model="form.reason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getPlannedList, addPlanned, updatePlanned, submitPlanned } from '@/api/outage'
export default {
  name: 'OutagePlanned',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '', planCode: '', status: '' }, dialogVisible: false, dialogType: 'add', form: { planCode: '', stationName: '', plannedTime: '', duration: 60, reason: '' }, rules: { planCode: [{ required: true, trigger: 'blur' }], stationName: [{ required: true, trigger: 'blur' }], plannedTime: [{ required: true, trigger: 'blur' }], reason: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getPlannedList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '', planCode: '', status: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { planCode: '', stationName: '', plannedTime: '', duration: 60, reason: '' }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleSubmitApproval(row) { this.$confirm('确认提交审批？','提示').then(()=>{ submitPlanned(row.id).then(()=>{ this.$message.success('已提交');this.fetchList() }) }).catch(()=>{}) },
    handleSave() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addPlanned : updatePlanned)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>

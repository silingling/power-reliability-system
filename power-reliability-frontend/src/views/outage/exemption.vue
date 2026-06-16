<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="豁免类型"><el-select v-model="query.exemptionType" placeholder="请选择" clearable><el-option label="计划停电豁免" value="planned" /><el-option label="故障停电豁免" value="fault" /><el-option label="全部豁免" value="all" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增豁免</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="stationName" label="台区名称" width="130" /><el-table-column prop="exemptionType" label="豁免类型" width="120"><template slot-scope="{row}">{{ {planned:'计划停电豁免',fault:'故障停电豁免',all:'全部豁免'}[row.exemptionType]||row.exemptionType }}</template></el-table-column>
      <el-table-column prop="reason" label="豁免原因" min-width="250" /><el-table-column prop="startDate" label="开始日期" width="120" /><el-table-column prop="endDate" label="结束日期" width="120" /><el-table-column prop="approver" label="审批人" width="120" />
      <el-table-column prop="status" label="状态" width="80"><template slot-scope="{row}"><el-tag :type="row.status===1?'success':'warning'" size="small">{{ row.status===1?'有效':'待审批' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新增豁免':'编辑豁免'" :visible.sync="dialogVisible" width="550px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="台区名称" prop="stationName"><el-input v-model="form.stationName" /></el-form-item>
        <el-form-item label="豁免类型" prop="exemptionType"><el-select v-model="form.exemptionType" style="width:100%"><el-option label="计划停电豁免" value="planned" /><el-option label="故障停电豁免" value="fault" /><el-option label="全部豁免" value="all" /></el-select></el-form-item>
        <el-form-item label="豁免原因" prop="reason"><el-input v-model="form.reason" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="开始日期" prop="startDate"><el-date-picker v-model="form.startDate" type="date" value-format="yyyy-MM-dd" style="width:100%" /></el-form-item>
        <el-form-item label="结束日期" prop="endDate"><el-date-picker v-model="form.endDate" type="date" value-format="yyyy-MM-dd" style="width:100%" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getExemptionList, addExemption, updateExemption } from '@/api/outage'
export default {
  name: 'OutageExemption',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '', exemptionType: '' }, dialogVisible: false, dialogType: 'add', form: { stationName: '', exemptionType: 'planned', reason: '', startDate: '', endDate: '' }, rules: { stationName: [{ required: true, trigger: 'blur' }], reason: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getExemptionList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '', exemptionType: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { stationName: '', exemptionType: 'planned', reason: '', startDate: '', endDate: '' }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleDelete(row) { this.$confirm('确认删除？','提示',{type:'warning'}).then(()=>{ this.$message.success('删除成功'); this.fetchList() }).catch(()=>{}) },
    handleSave() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addExemption : updateExemption)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>

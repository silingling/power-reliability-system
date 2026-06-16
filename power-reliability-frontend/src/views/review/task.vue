<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="任务名称"><el-input v-model="query.taskName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="请选择" clearable><el-option label="待整改" value="pending" /><el-option label="整改中" value="processing" /><el-option label="已完成" value="completed" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新建整改任务</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="taskName" label="任务名称" min-width="180" /><el-table-column prop="stationName" label="台区名称" width="140" />
      <el-table-column prop="content" label="整改内容" min-width="250" /><el-table-column prop="responsible" label="责任人" width="120" /><el-table-column prop="deadline" label="截止日期" width="120" />
      <el-table-column prop="status" label="状态" width="90"><template slot-scope="{row}"><el-tag :type="{pending:'danger',processing:'warning',completed:'success'}[row.status]" size="small">{{ {pending:'待整改',processing:'整改中',completed:'已完成'}[row.status]||row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" @click="handleComplete(row)">完成</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新建整改任务':'编辑整改任务'" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务名称" prop="taskName"><el-input v-model="form.taskName" /></el-form-item>
        <el-form-item label="台区名称" prop="stationName"><el-input v-model="form.stationName" /></el-form-item>
        <el-form-item label="整改内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="责任人"><el-input v-model="form.responsible" /></el-form-item>
        <el-form-item label="截止日期"><el-date-picker v-model="form.deadline" type="date" value-format="yyyy-MM-dd" style="width:100%" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getTaskList, addTask, updateTask, completeTask } from '@/api/review'
export default {
  name: 'ReviewTask',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { taskName: '', status: '' }, dialogVisible: false, dialogType: 'add', form: { taskName: '', stationName: '', content: '', responsible: '', deadline: '' }, rules: { taskName: [{ required: true, trigger: 'blur' }], stationName: [{ required: true, trigger: 'blur' }], content: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getTaskList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { taskName: '', status: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { taskName: '', stationName: '', content: '', responsible: '', deadline: '' }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleComplete(row) { this.$confirm('确认完成该任务？','提示',{type:'success'}).then(()=>{ completeTask(row.id).then(()=>{ this.$message.success('已完成');this.fetchList() }) }).catch(()=>{}) },
    handleSave() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addTask : updateTask)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>

<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="用户名"><el-input v-model="query.username" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="部门"><el-input v-model="query.deptName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增用户</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="username" label="用户名" width="120" /><el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="email" label="邮箱" width="180" /><el-table-column prop="phone" label="手机号" width="130" /><el-table-column prop="deptName" label="部门" width="140" />
      <el-table-column prop="roleNames" label="角色" min-width="150" />
      <el-table-column prop="status" label="状态" width="80"><template slot-scope="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" @click="handleResetPwd(row)">重置密码</el-button><el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新增用户':'编辑用户'" :visible.sync="dialogVisible" width="550px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="真实姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :label="1">启用</el-radio><el-radio :label="0">禁用</el-radio></el-radio-group></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getSystemUserList, addSystemUser, updateSystemUser, deleteSystemUser, resetUserPassword } from '@/api/system'
export default {
  name: 'SystemUser',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { username: '', deptName: '' }, dialogVisible: false, dialogType: 'add', form: { username: '', realName: '', email: '', phone: '', deptName: '', status: 1 }, rules: { username: [{ required: true, trigger: 'blur' }], realName: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getSystemUserList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { username: '', deptName: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { username: '', realName: '', email: '', phone: '', deptName: '', status: 1 }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleResetPwd(row) { this.$confirm(`确认重置用户"${row.username}"的密码？`,'提示').then(()=>{ resetUserPassword(row.id).then(()=>{ this.$message.success('密码已重置') }) }).catch(()=>{}) },
    handleDelete(row) { this.$confirm('确认删除？','提示',{type:'warning'}).then(()=>{ deleteSystemUser(row.id).then(()=>{ this.$message.success('删除成功');this.fetchList() }) }).catch(()=>{}) },
    handleSave() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addSystemUser : updateSystemUser)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>

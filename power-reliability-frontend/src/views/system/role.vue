<template>
  <div class="app-container">
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增角色</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="roleName" label="角色名称" width="150" /><el-table-column prop="roleKey" label="权限标识" width="150" />
      <el-table-column prop="description" label="描述" min-width="300" /><el-table-column prop="status" label="状态" width="80"><template slot-scope="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-dialog :title="dialogType==='add'?'新增角色':'编辑角色'" :visible.sync="dialogVisible" width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="权限标识" prop="roleKey"><el-input v-model="form.roleKey" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :label="1">启用</el-radio><el-radio :label="0">禁用</el-radio></el-radio-group></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getRoleList, addRole, updateRole, deleteRole } from '@/api/system'
export default {
  name: 'SystemRole',
  data() { return { loading: false, list: [], dialogVisible: false, dialogType: 'add', form: { roleName: '', roleKey: '', description: '', status: 1 }, rules: { roleName: [{ required: true, trigger: 'blur' }], roleKey: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getRoleList({}).then(r => { this.list = r.data.list || [] }).finally(() => { this.loading = false }) },
    handleAdd() { this.dialogType = 'add'; this.form = { roleName: '', roleKey: '', description: '', status: 1 }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleDelete(row) { this.$confirm('确认删除？','提示',{type:'warning'}).then(()=>{ deleteRole(row.id).then(()=>{ this.$message.success('删除成功');this.fetchList() }) }).catch(()=>{}) },
    handleSave() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addRole : updateRole)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.table-header { margin-bottom: 15px; }
</style>

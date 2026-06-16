<template>
  <div class="app-container">
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增菜单</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe row-key="id" default-expand-all :tree-props="{children:'children'}" style="width:100%">
      <el-table-column prop="menuName" label="菜单名称" width="200" />
      <el-table-column prop="icon" label="图标" width="80" />
      <el-table-column prop="path" label="路由路径" width="200" />
      <el-table-column prop="component" label="组件路径" min-width="200" />
      <el-table-column prop="permission" label="权限标识" width="150" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="操作" width="180" fixed="right">
        <template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-dialog :title="dialogType==='add'?'新增菜单':'编辑菜单'" :visible.sync="dialogVisible" width="550px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="菜单名称" prop="menuName"><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item label="路由路径"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="组件路径"><el-input v-model="form.component" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="form.permission" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getMenuList, addMenu, updateMenu, deleteMenu } from '@/api/system'
export default {
  name: 'SystemMenu',
  data() { return { loading: false, list: [], dialogVisible: false, dialogType: 'add', form: { menuName: '', path: '', component: '', permission: '', sort: 0, parentId: 0 }, rules: { menuName: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getMenuList({}).then(r => { this.list = r.data.list || [] }).finally(() => { this.loading = false }) },
    handleAdd() { this.dialogType = 'add'; this.form = { menuName: '', path: '', component: '', permission: '', sort: 0, parentId: 0 }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleDelete(row) { this.$confirm('确认删除？','提示',{type:'warning'}).then(()=>{ deleteMenu(row.id).then(()=>{ this.$message.success('删除成功');this.fetchList() }) }).catch(()=>{}) },
    handleSave() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addMenu : updateMenu)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.table-header { margin-bottom: 15px; }
</style>

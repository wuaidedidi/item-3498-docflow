<template>
  <div class="page-container">
    <el-card>
      <div slot="header" class="page-header">
        <h3>分类管理</h3>
        <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog">新建分类</el-button>
      </div>

      <el-table :data="categories" v-loading="loading" stripe>
        <el-table-column prop="name" label="分类名称" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="isEdit ? '编辑分类' : '新建分类'" :visible.sync="dialogVisible" width="480px" :close-on-click-modal="false">
      <el-form ref="catForm" :model="catForm" :rules="catRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="catForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="catForm.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="catForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/category'

export default {
  name: 'CategoryList',
  data() {
    return {
      categories: [],
      loading: false,
      dialogVisible: false,
      isEdit: false,
      editId: null,
      submitLoading: false,
      catForm: { name: '', description: '', sortOrder: 0 },
      catRules: { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }
    }
  },
  methods: {
    async fetchData() {
      this.loading = true
      try { const res = await getCategoryList(); this.categories = res.data } catch (e) {} finally { this.loading = false }
    },
    showCreateDialog() {
      this.isEdit = false; this.editId = null
      this.catForm = { name: '', description: '', sortOrder: 0 }
      this.dialogVisible = true
      this.$nextTick(() => { this.$refs.catForm && this.$refs.catForm.clearValidate() })
    },
    showEditDialog(row) {
      this.isEdit = true; this.editId = row.id
      this.catForm = { name: row.name, description: row.description, sortOrder: row.sortOrder || 0 }
      this.dialogVisible = true
    },
    async submitForm() {
      this.$refs.catForm.validate(async (valid) => {
        if (!valid) return
        this.submitLoading = true
        try {
          if (this.isEdit) {
            await updateCategory(this.editId, this.catForm)
            this.$message.success('分类更新成功')
          } else {
            await createCategory(this.catForm)
            this.$message.success('分类创建成功')
          }
          this.dialogVisible = false
          this.fetchData()
        } catch (e) {} finally { this.submitLoading = false }
      })
    },
    handleDelete(row) {
      this.$confirm(`确定要删除分类「${row.name}」吗？`, '确认删除', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try { await deleteCategory(row.id); this.$message.success('删除成功'); this.fetchData() } catch (e) {}
      }).catch(() => {})
    }
  },
  created() { this.fetchData() }
}
</script>

<style lang="scss" scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; h3 { font-size: 18px; font-weight: 600; color: #1a1a2e; } }
</style>

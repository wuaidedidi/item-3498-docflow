<template>
  <div class="page-container">
    <el-card>
      <div slot="header" class="page-header">
        <h3>文档管理</h3>
        <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog">新建文档</el-button>
      </div>

      <div class="filter-bar">
        <el-input v-model="query.title" placeholder="搜索文档标题" prefix-icon="el-icon-search" clearable style="width: 240px" @clear="fetchData" @keyup.enter.native="fetchData" />
        <el-select v-model="query.categoryId" placeholder="选择分类" clearable @change="fetchData" style="width: 160px">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="选择状态" clearable @change="fetchData" style="width: 140px">
          <el-option label="已发布" value="published" />
          <el-option label="审批中" value="pending_approval" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="fetchData">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </div>

      <el-table :data="documents" v-loading="loading" stripe>
        <el-table-column prop="title" label="文档名称" min-width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="doc-title-link" @click="$router.push(`/documents/${scope.row.id}`)">
              <i class="el-icon-document" style="margin-right: 6px; color: #409eff"></i>
              {{ scope.row.title }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120">
          <template slot-scope="scope">
            <el-tag size="small" type="info">{{ scope.row.categoryName || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="类型" width="80" align="center">
          <template slot-scope="scope">
            <span class="file-type">{{ (scope.row.fileType || '').toUpperCase() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="70" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" type="info">V{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建者" width="100" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="$router.push(`/documents/${scope.row.id}`)">详情</el-button>
            <el-button type="text" size="small" @click="handleDownload(scope.row)">下载</el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        :current-page="query.page"
        :page-sizes="[10, 20, 50]"
        :page-size="query.size"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </el-card>

    <!-- Create Document Dialog -->
    <el-dialog title="新建文档" :visible.sync="createVisible" width="560px" :close-on-click-modal="false">
      <el-form ref="createForm" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="文档标题" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入文档标题" />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="createForm.categoryId" placeholder="请选择分类" clearable style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入文档描述" />
        </el-form-item>
        <el-form-item label="上传文件" prop="file">
          <el-upload
            ref="upload"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            drag
            action=""
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div class="el-upload__tip" slot="tip">支持所有常见文档格式，最大50MB</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="createVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCreate">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getDocumentList, createDocument, deleteDocument, getDownloadUrl } from '@/api/document'
import { getCategoryList } from '@/api/category'

export default {
  name: 'DocumentList',
  data() {
    return {
      documents: [],
      categories: [],
      total: 0,
      loading: false,
      query: { page: 1, size: 10, title: '', categoryId: null, status: '' },
      createVisible: false,
      submitLoading: false,
      createForm: { title: '', categoryId: null, description: '', file: null },
      createRules: {
        title: [{ required: true, message: '请输入文档标题', trigger: 'blur' }],
        file: [{ required: true, message: '请上传文件', trigger: 'change' }]
      },
      fileList: []
    }
  },
  methods: {
    getStatusType(status) {
      const map = { published: 'success', pending_approval: 'warning', rejected: 'danger', draft: 'info' }
      return map[status] || 'info'
    },
    getStatusText(status) {
      const map = { published: '已发布', pending_approval: '审批中', rejected: '已拒绝', draft: '草稿' }
      return map[status] || status
    },
    async fetchData() {
      this.loading = true
      try {
        const res = await getDocumentList(this.query)
        this.documents = res.data.records
        this.total = res.data.total
      } catch (e) { /* handled */ } finally { this.loading = false }
    },
    async fetchCategories() {
      try {
        const res = await getCategoryList()
        this.categories = res.data
      } catch (e) { /* handled */ }
    },
    resetQuery() {
      this.query = { page: 1, size: 10, title: '', categoryId: null, status: '' }
      this.fetchData()
    },
    handlePageChange(page) { this.query.page = page; this.fetchData() },
    handleSizeChange(size) { this.query.size = size; this.query.page = 1; this.fetchData() },
    showCreateDialog() {
      this.createForm = { title: '', categoryId: null, description: '', file: null }
      this.fileList = []
      this.createVisible = true
      this.$nextTick(() => { this.$refs.createForm && this.$refs.createForm.clearValidate() })
    },
    handleFileChange(file) {
      this.createForm.file = file.raw
      this.$refs.createForm && this.$refs.createForm.validateField('file')
    },
    handleFileRemove() { this.createForm.file = null; this.$refs.createForm && this.$refs.createForm.validateField('file') },
    async submitCreate() {
      this.$refs.createForm.validate(async (valid) => {
        if (!valid) return
        this.submitLoading = true
        try {
          const formData = new FormData()
          formData.append('title', this.createForm.title)
          if (this.createForm.description) formData.append('description', this.createForm.description)
          if (this.createForm.categoryId) formData.append('categoryId', this.createForm.categoryId)
          formData.append('file', this.createForm.file)
          await createDocument(formData)
          this.$message.success('文档创建成功')
          this.createVisible = false
          this.fetchData()
        } catch (e) {
          if (!e._isBusinessError) this.$message.error(e.message || '文档上传失败，请重试')
        } finally { this.submitLoading = false }
      })
    },
    handleDownload(row) {
      const url = getDownloadUrl(row.filePath, row.fileName)
      const token = this.$store.getters.token
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', row.fileName)

      const xhr = new XMLHttpRequest()
      xhr.open('GET', url, true)
      xhr.responseType = 'blob'
      xhr.setRequestHeader('Authorization', 'Bearer ' + token)
      xhr.onload = () => {
        if (xhr.status === 200) {
          const blobUrl = window.URL.createObjectURL(xhr.response)
          link.href = blobUrl
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(blobUrl)
        } else {
          this.$message.error('文件下载失败，请重试')
        }
      }
      xhr.onerror = () => {
        this.$message.error('网络错误，文件下载失败')
      }
      xhr.send()
    },
    handleDelete(row) {
      this.$confirm(`确定要删除文档「${row.title}」吗？`, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteDocument(row.id)
          this.$message.success('删除成功')
          if (this.documents.length === 1 && this.query.page > 1) {
            this.query.page--
          }
          this.fetchData()
        } catch (e) { /* handled */ }
      }).catch(() => {})
    }
  },
  created() {
    this.fetchData()
    this.fetchCategories()
  }
}
</script>

<style lang="scss" scoped>
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  h3 { font-size: 18px; font-weight: 600; color: #1a1a2e; }
}
.doc-title-link {
  color: #409eff; cursor: pointer; font-weight: 500;
  &:hover { text-decoration: underline; }
}
.file-type { font-size: 12px; color: #909399; font-weight: 600; }
</style>

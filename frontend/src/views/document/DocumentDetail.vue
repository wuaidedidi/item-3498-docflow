<template>
  <div class="page-container">
    <el-page-header @back="$router.push('/documents')" content="文档详情" style="margin-bottom: 20px" />

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="16">
        <el-card class="detail-card">
          <div class="doc-header">
            <div class="doc-title-row">
              <h2>{{ doc.title }}</h2>
              <el-tag :type="getStatusType(doc.status)">{{ getStatusText(doc.status) }}</el-tag>
            </div>
            <div class="doc-meta">
              <span><i class="el-icon-user"></i> {{ doc.creatorName }}</span>
              <span><i class="el-icon-folder"></i> {{ doc.categoryName || '未分类' }}</span>
              <span><i class="el-icon-time"></i> {{ doc.updateTime }}</span>
              <span><i class="el-icon-document"></i> V{{ doc.version }}</span>
            </div>
          </div>
          <el-divider />
          <div class="doc-desc">
            <h4>文档描述</h4>
            <p>{{ doc.description || '暂无描述' }}</p>
          </div>
          <el-divider />
          <div class="doc-file">
            <h4>文件信息</h4>
            <div class="file-info-grid">
              <div class="file-info-item"><span class="label">文件名</span><span>{{ doc.fileName }}</span></div>
              <div class="file-info-item"><span class="label">文件类型</span><span>{{ (doc.fileType || '').toUpperCase() }}</span></div>
              <div class="file-info-item"><span class="label">文件大小</span><span>{{ formatSize(doc.fileSize) }}</span></div>
              <div class="file-info-item"><span class="label">当前版本</span><span>V{{ doc.version }}</span></div>
            </div>
          </div>
          <div class="doc-actions" style="margin-top: 20px">
            <el-button type="primary" icon="el-icon-download" @click="handleDownload(doc)">下载文件</el-button>
            <el-button icon="el-icon-upload2" @click="showUploadDialog">上传新版本</el-button>
            <el-button icon="el-icon-edit" @click="showEditDialog">编辑信息</el-button>
          </div>
        </el-card>

        <!-- Version History -->
        <el-card style="margin-top: 20px">
          <div slot="header"><h4 style="margin:0">版本历史</h4></div>
          <el-timeline>
            <el-timeline-item
              v-for="v in versions"
              :key="v.id"
              :timestamp="v.createTime"
              :type="v.approvalStatus === 'approved' ? 'success' : v.approvalStatus === 'pending' ? 'warning' : 'danger'"
            >
              <div class="version-item">
                <div class="version-header">
                  <strong>V{{ v.version }}</strong>
                  <el-tag size="mini" :type="getApprovalType(v.approvalStatus)">{{ getApprovalText(v.approvalStatus) }}</el-tag>
                </div>
                <p class="version-log">{{ v.changeLog || '无更新说明' }}</p>
                <p class="version-meta">上传者: {{ v.uploaderName }} | 文件: {{ v.fileName }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <div v-if="versions.length === 0" class="empty-text">暂无版本记录</div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- Approval History -->
        <el-card>
          <div slot="header"><h4 style="margin:0">审批记录</h4></div>
          <el-timeline v-if="approvalRecords.length > 0">
            <el-timeline-item
              v-for="r in approvalRecords"
              :key="r.id"
              :timestamp="r.updateTime || r.createTime"
              :type="r.status === 'approved' ? 'success' : r.status === 'rejected' ? 'danger' : 'warning'"
              :icon="r.status === 'approved' ? 'el-icon-check' : r.status === 'rejected' ? 'el-icon-close' : 'el-icon-time'"
            >
              <div class="approval-item">
                <div><strong>{{ r.nodeName }}</strong></div>
                <div class="approval-meta">审批人: {{ r.approverName }}</div>
                <el-tag size="mini" :type="getApprovalType(r.status)">{{ getApprovalText(r.status) }}</el-tag>
                <p v-if="r.comment" class="approval-comment">{{ r.comment }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <div v-else class="empty-text">暂无审批记录</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Upload Version Dialog -->
    <el-dialog title="上传新版本" :visible.sync="uploadVisible" width="560px" :close-on-click-modal="false">
      <el-form ref="uploadForm" :model="uploadForm" :rules="uploadRules" label-width="100px">
        <el-form-item label="上传文件" prop="file">
          <el-upload ref="uploadRef" :auto-upload="false" :limit="1" :on-change="onUploadFileChange" :on-remove="onUploadFileRemove" :file-list="uploadFileList" drag action="">
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">拖拽或<em>点击上传</em></div>
          </el-upload>
        </el-form-item>
        <el-form-item label="更新说明">
          <el-input v-model="uploadForm.changeLog" type="textarea" :rows="3" placeholder="请描述本次更新的内容" />
        </el-form-item>
        <el-form-item label="审批流程">
          <el-select v-model="uploadForm.flowId" placeholder="选择审批流程（可选）" clearable style="width: 100%">
            <el-option v-for="f in flows" :key="f.id" :label="f.name" :value="f.id" />
          </el-select>
          <div class="form-tip">不选择则直接发布，选择后需审批通过才能发布</div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="submitUpload">确定</el-button>
      </div>
    </el-dialog>

    <!-- Edit Info Dialog -->
    <el-dialog title="编辑文档信息" :visible.sync="editVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="文档标题" prop="title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="所属分类">
          <el-select v-model="editForm.categoryId" placeholder="选择分类" clearable style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getDocumentDetail, getDocumentVersions, uploadNewVersion, updateDocument, getDownloadUrl } from '@/api/document'
import { getDocumentApprovalHistory, getFlowList } from '@/api/approval'
import { getCategoryList } from '@/api/category'

export default {
  name: 'DocumentDetail',
  data() {
    return {
      doc: {},
      versions: [],
      approvalRecords: [],
      flows: [],
      categories: [],
      loading: false,
      uploadVisible: false,
      uploadLoading: false,
      uploadForm: { file: null, changeLog: '', flowId: null },
      uploadRules: { file: [{ required: true, message: '请上传文件', trigger: 'change' }] },
      uploadFileList: [],
      editVisible: false,
      editLoading: false,
      editForm: { title: '', categoryId: null, description: '' },
      editRules: { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }
    }
  },
  methods: {
    getStatusType(s) { return { published: 'success', pending_approval: 'warning', rejected: 'danger' }[s] || 'info' },
    getStatusText(s) { return { published: '已发布', pending_approval: '审批中', rejected: '已拒绝', draft: '草稿' }[s] || s },
    getApprovalType(s) { return { approved: 'success', pending: 'warning', rejected: 'danger' }[s] || 'info' },
    getApprovalText(s) { return { approved: '已通过', pending: '待审批', rejected: '已拒绝' }[s] || s },
    formatSize(bytes) {
      if (!bytes) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB']
      let i = 0
      let size = bytes
      while (size >= 1024 && i < units.length - 1) { size /= 1024; i++ }
      return size.toFixed(1) + ' ' + units[i]
    },
    async fetchData() {
      this.loading = true
      const id = this.$route.params.id
      try {
        const [docRes, versionsRes, recordsRes] = await Promise.all([
          getDocumentDetail(id),
          getDocumentVersions(id),
          getDocumentApprovalHistory(id)
        ])
        this.doc = docRes.data
        this.versions = versionsRes.data
        this.approvalRecords = recordsRes.data
      } catch (e) { /* handled */ } finally { this.loading = false }
    },
    async fetchFlows() {
      try { const res = await getFlowList(); this.flows = res.data.filter(f => f.status === 1) } catch (e) {}
    },
    async fetchCategories() {
      try { const res = await getCategoryList(); this.categories = res.data } catch (e) {}
    },
    handleDownload(row) {
      const url = getDownloadUrl(row.filePath, row.fileName)
      const token = this.$store.getters.token
      const xhr = new XMLHttpRequest()
      xhr.open('GET', url, true)
      xhr.responseType = 'blob'
      xhr.setRequestHeader('Authorization', 'Bearer ' + token)
      xhr.onload = () => {
        if (xhr.status === 200) {
          const blobUrl = window.URL.createObjectURL(xhr.response)
          const link = document.createElement('a')
          link.href = blobUrl
          link.setAttribute('download', row.fileName)
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
    showUploadDialog() {
      this.uploadForm = { file: null, changeLog: '', flowId: null }
      this.uploadFileList = []
      this.uploadVisible = true
      this.fetchFlows()
    },
    onUploadFileChange(f) { this.uploadForm.file = f.raw; this.$refs.uploadForm && this.$refs.uploadForm.validateField('file') },
    onUploadFileRemove() { this.uploadForm.file = null; this.$refs.uploadForm && this.$refs.uploadForm.validateField('file') },
    async submitUpload() {
      this.$refs.uploadForm.validate(async (valid) => {
        if (!valid) return
        this.uploadLoading = true
        try {
          const fd = new FormData()
          fd.append('file', this.uploadForm.file)
          if (this.uploadForm.changeLog) fd.append('changeLog', this.uploadForm.changeLog)
          if (this.uploadForm.flowId) fd.append('flowId', this.uploadForm.flowId)
          await uploadNewVersion(this.doc.id, fd)
          this.$message.success('版本上传成功')
          this.uploadVisible = false
          this.fetchData()
        } catch (e) {
          if (!e._isBusinessError) this.$message.error(e.message || '版本上传失败，请重试')
        } finally { this.uploadLoading = false }
      })
    },
    showEditDialog() {
      this.editForm = { title: this.doc.title, categoryId: this.doc.categoryId, description: this.doc.description }
      this.editVisible = true
      this.fetchCategories()
    },
    async submitEdit() {
      this.$refs.editForm.validate(async (valid) => {
        if (!valid) return
        this.editLoading = true
        try {
          await updateDocument(this.doc.id, this.editForm)
          this.$message.success('文档信息更新成功')
          this.editVisible = false
          this.fetchData()
        } catch (e) {
          if (!e._isBusinessError) this.$message.error(e.message || '更新失败，请重试')
        } finally { this.editLoading = false }
      })
    }
  },
  created() {
    this.fetchData()
    this.fetchCategories()
  }
}
</script>

<style lang="scss" scoped>
.detail-card { .doc-header { margin-bottom: 8px; } }
.doc-title-row { display: flex; align-items: center; gap: 12px; h2 { font-size: 22px; color: #1a1a2e; } }
.doc-meta { display: flex; gap: 20px; margin-top: 10px; color: #909399; font-size: 13px; i { margin-right: 4px; } }
.doc-desc { h4 { font-size: 15px; color: #333; margin-bottom: 8px; } p { color: #606266; line-height: 1.8; } }
.doc-file { h4 { font-size: 15px; color: #333; margin-bottom: 12px; } }
.file-info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.file-info-item {
  display: flex; justify-content: space-between; padding: 10px 14px; background: #f8f9fb; border-radius: 8px;
  .label { color: #909399; font-size: 13px; } span:last-child { color: #333; font-weight: 500; font-size: 13px; }
}
.version-item { .version-header { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; } }
.version-log { color: #606266; font-size: 13px; margin: 4px 0; }
.version-meta { color: #909399; font-size: 12px; }
.approval-item { .approval-meta { color: #909399; font-size: 12px; margin: 4px 0; } }
.approval-comment { color: #606266; font-size: 13px; margin-top: 6px; padding: 8px; background: #f5f7fa; border-radius: 6px; }
.empty-text { text-align: center; color: #c0c4cc; padding: 20px; }
.form-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>

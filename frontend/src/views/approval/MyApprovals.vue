<template>
  <div class="page-container">
    <el-card>
      <div slot="header" class="page-header">
        <h3>我的审批</h3>
      </div>

      <div class="filter-bar">
        <el-radio-group v-model="query.status" @change="fetchData" size="medium">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="pending">待审批</el-radio-button>
          <el-radio-button label="approved">已通过</el-radio-button>
          <el-radio-button label="rejected">已拒绝</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="documentTitle" label="文档名称" min-width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="doc-link" @click="$router.push(`/documents/${scope.row.documentId}`)">
              <i class="el-icon-document" style="margin-right: 4px"></i>
              {{ scope.row.documentTitle }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="flowName" label="审批流程" width="160" />
        <el-table-column prop="nodeName" label="审批节点" width="140" />
        <el-table-column prop="submitterName" label="提交人" width="100" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <template v-if="scope.row.status === 'pending'">
              <el-button type="text" size="small" style="color: #67c23a" @click="handleApprove(scope.row)">通过</el-button>
              <el-button type="text" size="small" style="color: #f56c6c" @click="handleReject(scope.row)">拒绝</el-button>
            </template>
            <span v-else class="handled-text">
              <i :class="scope.row.status === 'approved' ? 'el-icon-check' : 'el-icon-close'"></i>
              已处理
            </span>
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

    <!-- Approval Dialog -->
    <el-dialog :title="approvalAction === 'approve' ? '审批通过' : '审批拒绝'" :visible.sync="approvalVisible" width="480px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="approvalComment" type="textarea" :rows="3" :placeholder="approvalAction === 'approve' ? '请输入审批意见（可选）' : '请输入拒绝原因'" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="approvalVisible = false">取消</el-button>
        <el-button :type="approvalAction === 'approve' ? 'success' : 'danger'" :loading="approvalLoading" @click="submitApproval">
          {{ approvalAction === 'approve' ? '确认通过' : '确认拒绝' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyApprovals, processApproval } from '@/api/approval'

export default {
  name: 'MyApprovals',
  data() {
    return {
      records: [],
      total: 0,
      loading: false,
      query: { page: 1, size: 10, status: '' },
      approvalVisible: false,
      approvalLoading: false,
      approvalAction: '',
      approvalRecordId: null,
      approvalComment: ''
    }
  },
  methods: {
    getStatusType(s) { return { approved: 'success', pending: 'warning', rejected: 'danger' }[s] || 'info' },
    getStatusText(s) { return { approved: '已通过', pending: '待审批', rejected: '已拒绝' }[s] || s },
    async fetchData() {
      this.loading = true
      try {
        const res = await getMyApprovals(this.query)
        this.records = res.data.records
        this.total = res.data.total
      } catch (e) {} finally { this.loading = false }
    },
    handlePageChange(page) { this.query.page = page; this.fetchData() },
    handleSizeChange(size) { this.query.size = size; this.query.page = 1; this.fetchData() },
    handleApprove(row) {
      this.approvalAction = 'approve'
      this.approvalRecordId = row.id
      this.approvalComment = ''
      this.approvalVisible = true
    },
    handleReject(row) {
      this.approvalAction = 'reject'
      this.approvalRecordId = row.id
      this.approvalComment = ''
      this.approvalVisible = true
    },
    async submitApproval() {
      if (this.approvalAction === 'reject' && !this.approvalComment.trim()) {
        this.$message.warning('请输入拒绝原因')
        return
      }
      this.approvalLoading = true
      try {
        await processApproval({
          recordId: this.approvalRecordId,
          action: this.approvalAction,
          comment: this.approvalComment
        })
        this.$message.success(this.approvalAction === 'approve' ? '审批通过' : '已拒绝')
        this.approvalVisible = false
        this.fetchData()
      } catch (e) {} finally { this.approvalLoading = false }
    }
  },
  created() { this.fetchData() }
}
</script>

<style lang="scss" scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; h3 { font-size: 18px; font-weight: 600; color: #1a1a2e; } }
.doc-link { color: #409eff; cursor: pointer; &:hover { text-decoration: underline; } }
.handled-text { color: #909399; font-size: 13px; i { margin-right: 4px; } }
</style>

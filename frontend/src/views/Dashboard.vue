<template>
  <div class="dashboard">
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>欢迎回来，{{ user ? user.nickname : '' }}</h2>
        <p>以下是您的工作概览</p>
      </div>
      <div class="welcome-date">
        <i class="el-icon-date"></i>
        {{ currentDate }}
      </div>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-blue">
          <div class="stat-icon"><i class="el-icon-document"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalDocuments || 0 }}</div>
            <div class="stat-label">文档总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-orange">
          <div class="stat-icon"><i class="el-icon-time"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingApprovals || 0 }}</div>
            <div class="stat-label">待审批</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-green">
          <div class="stat-icon"><i class="el-icon-circle-check"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.approvedCount || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-purple">
          <div class="stat-icon"><i class="el-icon-user"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="section-card">
          <div slot="header" class="section-header">
            <span class="section-title">最近文档</span>
            <el-button type="text" @click="$router.push('/documents')">查看全部 <i class="el-icon-arrow-right"></i></el-button>
          </div>
          <el-table :data="recentDocs" style="width: 100%" v-loading="loading">
            <el-table-column prop="title" label="文档名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="categoryName" label="分类" width="120">
              <template slot-scope="scope">
                <el-tag size="small" type="info">{{ scope.row.categoryName || '未分类' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag size="small" :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="170" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="section-card">
          <div slot="header" class="section-header">
            <span class="section-title">快捷操作</span>
          </div>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/documents')">
              <div class="action-icon action-blue"><i class="el-icon-upload2"></i></div>
              <span>上传文档</span>
            </div>
            <div class="action-item" @click="$router.push('/approval/my')">
              <div class="action-icon action-orange"><i class="el-icon-s-check"></i></div>
              <span>处理审批</span>
            </div>
            <div class="action-item" @click="$router.push('/profile')">
              <div class="action-icon action-green"><i class="el-icon-user"></i></div>
              <span>个人中心</span>
            </div>
            <div v-if="isAdmin" class="action-item" @click="$router.push('/approval/flows')">
              <div class="action-icon action-purple"><i class="el-icon-set-up"></i></div>
              <span>流程管理</span>
            </div>
          </div>
        </el-card>
        <el-card class="section-card" style="margin-top: 20px">
          <div slot="header" class="section-header">
            <span class="section-title">分类统计</span>
          </div>
          <div class="category-stats">
            <div class="cat-item" v-for="cat in stats.categoryStats" :key="cat.name">
              <span class="cat-name">{{ cat.name }}</span>
              <span class="cat-count">{{ cat.count }} 个文档</span>
            </div>
            <div v-if="!stats.categoryStats || stats.categoryStats.length === 0" class="empty-text">
              暂无分类数据
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getDashboardStats } from '@/api/dashboard'
import { getDocumentList } from '@/api/document'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {},
      recentDocs: [],
      loading: false,
      currentDate: ''
    }
  },
  computed: {
    ...mapGetters(['user', 'isAdmin'])
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
        const [statsRes, docsRes] = await Promise.all([
          getDashboardStats(),
          getDocumentList({ page: 1, size: 5 })
        ])
        this.stats = statsRes.data
        this.recentDocs = docsRes.data.records
      } catch (e) {
        // handled by interceptor
      } finally {
        this.loading = false
      }
    },
    formatDate() {
      const d = new Date()
      const days = ['日', '一', '二', '三', '四', '五', '六']
      this.currentDate = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${days[d.getDay()]}`
    }
  },
  created() {
    this.formatDate()
    this.fetchData()
  }
}
</script>

<style lang="scss" scoped>
.dashboard { max-width: 1400px; margin: 0 auto; }

.welcome-section {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px; padding: 24px 28px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border-radius: 16px; color: #fff;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
  h2 { font-size: 22px; font-weight: 600; margin-bottom: 4px; }
  p { opacity: 0.85; font-size: 14px; }
}
.welcome-date { font-size: 14px; opacity: 0.9; i { margin-right: 6px; } }

.stats-row { margin-bottom: 20px; }
.stat-card {
  display: flex; align-items: center; gap: 16px;
  padding: 24px; border-radius: 14px; background: #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: transform 0.3s, box-shadow 0.3s;
  &:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
}
.stat-icon {
  width: 52px; height: 52px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: #fff;
}
.stat-blue .stat-icon { background: linear-gradient(135deg, #409eff, #66b1ff); }
.stat-orange .stat-icon { background: linear-gradient(135deg, #e6a23c, #f0c78a); }
.stat-green .stat-icon { background: linear-gradient(135deg, #67c23a, #95d475); }
.stat-purple .stat-icon { background: linear-gradient(135deg, #6366f1, #818cf8); }
.stat-value { font-size: 28px; font-weight: 700; color: #1a1a2e; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }

.section-card { margin-bottom: 20px; }
.section-header {
  display: flex; justify-content: space-between; align-items: center;
}
.section-title { font-size: 16px; font-weight: 600; color: #1a1a2e; }

.quick-actions {
  display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
}
.action-item {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 20px; border-radius: 12px; cursor: pointer;
  background: #f8f9fb; transition: all 0.3s;
  &:hover { background: #eef5ff; transform: translateY(-2px); }
  span { font-size: 13px; color: #606266; font-weight: 500; }
}
.action-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; color: #fff;
}
.action-blue { background: linear-gradient(135deg, #409eff, #66b1ff); }
.action-orange { background: linear-gradient(135deg, #e6a23c, #f0c78a); }
.action-green { background: linear-gradient(135deg, #67c23a, #95d475); }
.action-purple { background: linear-gradient(135deg, #6366f1, #818cf8); }

.category-stats { display: flex; flex-direction: column; gap: 12px; }
.cat-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 14px; background: #f8f9fb; border-radius: 8px;
}
.cat-name { font-size: 14px; color: #333; font-weight: 500; }
.cat-count { font-size: 13px; color: #909399; }
.empty-text { text-align: center; color: #c0c4cc; padding: 20px; font-size: 14px; }
</style>

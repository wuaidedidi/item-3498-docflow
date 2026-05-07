<template>
  <div class="page-container">
    <el-card>
      <div slot="header" class="page-header">
        <h3>审批流程管理</h3>
        <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog">新建流程</el-button>
      </div>

      <div class="filter-bar">
        <el-input v-model="searchName" placeholder="搜索流程名称" prefix-icon="el-icon-search" clearable style="width: 240px" @clear="fetchData" @keyup.enter.native="fetchData" />
        <el-button type="primary" icon="el-icon-search" @click="fetchData">搜索</el-button>
      </div>

      <el-table :data="flows" v-loading="loading" stripe>
        <el-table-column prop="name" label="流程名称" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建者" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showDetail(scope.row)">查看</el-button>
            <el-button type="text" size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleToggle(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog :title="isEdit ? '编辑审批流程' : '新建审批流程'" :visible.sync="dialogVisible" width="640px" :close-on-click-modal="false">
      <el-form ref="flowForm" :model="flowForm" :rules="flowRules" label-width="100px">
        <el-form-item label="流程名称" prop="name">
          <el-input v-model="flowForm.name" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="流程描述">
          <el-input v-model="flowForm.description" type="textarea" :rows="2" placeholder="请输入流程描述" />
        </el-form-item>
        <el-form-item label="审批节点" prop="nodes">
          <div class="nodes-container">
            <div v-for="(node, index) in flowForm.nodes" :key="index" class="node-item">
              <div class="node-order">{{ index + 1 }}</div>
              <el-input v-model="node.name" placeholder="节点名称" style="width: 180px" />
              <el-select v-model="node.approverId" placeholder="选择审批人" style="width: 180px">
                <el-option v-for="u in users" :key="u.id" :label="u.nickname + ' (' + u.username + ')'" :value="u.id" />
              </el-select>
              <el-button type="danger" icon="el-icon-delete" circle size="mini" @click="removeNode(index)" :disabled="flowForm.nodes.length <= 1" />
            </div>
            <el-button type="primary" plain icon="el-icon-plus" size="small" @click="addNode">添加节点</el-button>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitFlow">确定</el-button>
      </div>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog title="流程详情" :visible.sync="detailVisible" width="560px">
      <div v-if="detailData" class="flow-detail">
        <div class="detail-row"><span class="label">流程名称：</span><span>{{ detailData.flow.name }}</span></div>
        <div class="detail-row"><span class="label">描述：</span><span>{{ detailData.flow.description || '无' }}</span></div>
        <div class="detail-row"><span class="label">状态：</span>
          <el-tag size="small" :type="detailData.flow.status === 1 ? 'success' : 'info'">
            {{ detailData.flow.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
        <el-divider content-position="left">审批节点</el-divider>
        <el-steps :active="detailData.nodes.length" direction="vertical" :space="60">
          <el-step v-for="n in detailData.nodes" :key="n.id" :title="n.name" :description="'审批人: ' + n.approverName" />
        </el-steps>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getFlowList, getFlowDetail, createFlow, updateFlow, deleteFlow, toggleFlowStatus } from '@/api/approval'
import { getAllUsers } from '@/api/user'

export default {
  name: 'FlowList',
  data() {
    return {
      flows: [],
      users: [],
      loading: false,
      searchName: '',
      dialogVisible: false,
      isEdit: false,
      editId: null,
      submitLoading: false,
      flowForm: { name: '', description: '', nodes: [{ name: '', approverId: null }] },
      flowRules: {
        name: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
        nodes: [{ required: true, message: '请添加审批节点', trigger: 'change' }]
      },
      detailVisible: false,
      detailData: null
    }
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await getFlowList({ name: this.searchName })
        this.flows = res.data
      } catch (e) {} finally { this.loading = false }
    },
    async fetchUsers() {
      try { const res = await getAllUsers(); this.users = res.data } catch (e) {}
    },
    showCreateDialog() {
      this.isEdit = false
      this.editId = null
      this.flowForm = { name: '', description: '', nodes: [{ name: '', approverId: null }] }
      this.dialogVisible = true
      this.fetchUsers()
      this.$nextTick(() => { this.$refs.flowForm && this.$refs.flowForm.clearValidate() })
    },
    async showEditDialog(row) {
      this.isEdit = true
      this.editId = row.id
      this.fetchUsers()
      try {
        const res = await getFlowDetail(row.id)
        this.flowForm = {
          name: res.data.flow.name,
          description: res.data.flow.description,
          nodes: res.data.nodes.map(n => ({ name: n.name, approverId: n.approverId }))
        }
        this.dialogVisible = true
      } catch (e) {}
    },
    async showDetail(row) {
      try {
        const res = await getFlowDetail(row.id)
        this.detailData = res.data
        this.detailVisible = true
      } catch (e) {}
    },
    addNode() { this.flowForm.nodes.push({ name: '', approverId: null }) },
    removeNode(index) { this.flowForm.nodes.splice(index, 1) },
    async submitFlow() {
      this.$refs.flowForm.validate(async (valid) => {
        if (!valid) return
        for (const node of this.flowForm.nodes) {
          if (!node.name || !node.approverId) {
            this.$message.warning('请完善所有审批节点信息')
            return
          }
        }
        this.submitLoading = true
        try {
          const data = {
            name: this.flowForm.name,
            description: this.flowForm.description,
            nodes: this.flowForm.nodes.map((n, i) => ({ ...n, nodeOrder: i + 1 }))
          }
          if (this.isEdit) {
            await updateFlow(this.editId, data)
            this.$message.success('流程更新成功')
          } else {
            await createFlow(data)
            this.$message.success('流程创建成功')
          }
          this.dialogVisible = false
          this.fetchData()
        } catch (e) {} finally { this.submitLoading = false }
      })
    },
    async handleToggle(row) {
      try {
        await toggleFlowStatus(row.id)
        this.$message.success('状态切换成功')
        this.fetchData()
      } catch (e) {}
    },
    handleDelete(row) {
      this.$confirm(`确定要删除流程「${row.name}」吗？`, '确认删除', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try { await deleteFlow(row.id); this.$message.success('删除成功'); this.fetchData() } catch (e) {}
      }).catch(() => {})
    }
  },
  created() { this.fetchData() }
}
</script>

<style lang="scss" scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; h3 { font-size: 18px; font-weight: 600; color: #1a1a2e; } }
.nodes-container { display: flex; flex-direction: column; gap: 12px; }
.node-item { display: flex; align-items: center; gap: 12px; padding: 12px; background: #f8f9fb; border-radius: 10px; }
.node-order {
  width: 28px; height: 28px; border-radius: 50%; background: #409eff; color: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; flex-shrink: 0;
}
.flow-detail { .detail-row { margin-bottom: 12px; .label { color: #909399; margin-right: 8px; } } }
</style>

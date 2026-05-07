INSERT INTO sys_user (username, password, nickname, email, phone, role, status, deleted)
SELECT 'admin', '$2a$10$placeholder_will_be_reset_by_app', '系统管理员', 'admin@docmanager.com', '13800000001', 'admin', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

INSERT INTO sys_user (username, password, nickname, email, phone, role, status, deleted)
SELECT 'zhangsan', '$2a$10$placeholder_will_be_reset_by_app', '张三', 'zhangsan@docmanager.com', '13800000002', 'user', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'zhangsan');

INSERT INTO sys_user (username, password, nickname, email, phone, role, status, deleted)
SELECT 'lisi', '$2a$10$placeholder_will_be_reset_by_app', '李四', 'lisi@docmanager.com', '13800000003', 'user', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'lisi');

INSERT INTO doc_category (name, description, sort_order, deleted)
SELECT '技术文档', '技术规范、API文档、架构设计等', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM doc_category WHERE name = '技术文档');

INSERT INTO doc_category (name, description, sort_order, deleted)
SELECT '项目管理', '项目计划、进度报告、会议纪要等', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM doc_category WHERE name = '项目管理');

INSERT INTO doc_category (name, description, sort_order, deleted)
SELECT '产品需求', '需求规格、产品原型、用户故事等', 3, 0
WHERE NOT EXISTS (SELECT 1 FROM doc_category WHERE name = '产品需求');

INSERT INTO doc_category (name, description, sort_order, deleted)
SELECT '规章制度', '公司制度、操作规范、流程指引等', 4, 0
WHERE NOT EXISTS (SELECT 1 FROM doc_category WHERE name = '规章制度');

INSERT INTO doc_category (name, description, sort_order, deleted)
SELECT '培训资料', '培训PPT、学习手册、考核题库等', 5, 0
WHERE NOT EXISTS (SELECT 1 FROM doc_category WHERE name = '培训资料');

INSERT INTO doc_approval_flow (name, description, status, creator_id, deleted)
SELECT '两级审批流程', '适用于一般文档更新，由部门负责人和管理员依次审批', 1, id, 0
FROM sys_user
WHERE username = 'admin'
AND NOT EXISTS (SELECT 1 FROM doc_approval_flow WHERE name = '两级审批流程');

INSERT INTO doc_approval_node (flow_id, name, approver_id, node_order)
SELECT f.id, '部门负责人审批', u.id, 1
FROM doc_approval_flow f, sys_user u
WHERE f.name = '两级审批流程'
AND u.username = 'zhangsan'
AND NOT EXISTS (
    SELECT 1 FROM doc_approval_node n
    WHERE n.flow_id = f.id AND n.node_order = 1
);

INSERT INTO doc_approval_node (flow_id, name, approver_id, node_order)
SELECT f.id, '管理员审批', u.id, 2
FROM doc_approval_flow f, sys_user u
WHERE f.name = '两级审批流程'
AND u.username = 'admin'
AND NOT EXISTS (
    SELECT 1 FROM doc_approval_node n
    WHERE n.flow_id = f.id AND n.node_order = 2
);

INSERT INTO doc_approval_flow (name, description, status, creator_id, deleted)
SELECT '快速审批流程', '适用于紧急文档更新，仅需管理员审批', 1, id, 0
FROM sys_user
WHERE username = 'admin'
AND NOT EXISTS (SELECT 1 FROM doc_approval_flow WHERE name = '快速审批流程');

INSERT INTO doc_approval_node (flow_id, name, approver_id, node_order)
SELECT f.id, '管理员审批', u.id, 1
FROM doc_approval_flow f, sys_user u
WHERE f.name = '快速审批流程'
AND u.username = 'admin'
AND NOT EXISTS (
    SELECT 1 FROM doc_approval_node n
    WHERE n.flow_id = f.id AND n.node_order = 1
);

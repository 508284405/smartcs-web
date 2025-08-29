-- 添加好友系统相关表
-- 创建时间: 2024-08-29

-- 创建好友关系表
CREATE TABLE IF NOT EXISTS cs_friend (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    from_user_id VARCHAR(64) NOT NULL COMMENT '发起用户ID',
    to_user_id VARCHAR(64) NOT NULL COMMENT '目标用户ID',
    status INT NOT NULL DEFAULT 0 COMMENT '好友状态 0-待审核 1-已同意 2-已拒绝 3-已拉黑',
    remark_name VARCHAR(50) DEFAULT NULL COMMENT '好友备注名',
    friend_group VARCHAR(32) DEFAULT NULL COMMENT '好友分组',
    apply_message VARCHAR(200) DEFAULT NULL COMMENT '申请消息',
    applied_at BIGINT NOT NULL COMMENT '申请时间',
    processed_at BIGINT DEFAULT NULL COMMENT '处理时间',
    processed_by VARCHAR(64) DEFAULT NULL COMMENT '处理者用户ID',
    reject_reason VARCHAR(200) DEFAULT NULL COMMENT '拒绝原因',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    
    -- 索引
    KEY idx_from_user_id (from_user_id),
    KEY idx_to_user_id (to_user_id),
    KEY idx_status (status),
    KEY idx_applied_at (applied_at),
    KEY idx_friend_group (friend_group),
    
    -- 唯一约束：同一对用户之间只能有一条记录
    UNIQUE KEY uk_users (from_user_id, to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- 创建好友分组表
CREATE TABLE IF NOT EXISTS cs_friend_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    group_name VARCHAR(32) NOT NULL COMMENT '分组名称',
    group_order INT DEFAULT 0 COMMENT '分组排序',
    group_color VARCHAR(7) DEFAULT NULL COMMENT '分组颜色',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    
    -- 索引
    KEY idx_user_id (user_id),
    KEY idx_group_order (group_order),
    
    -- 唯一约束：同一用户的分组名不能重复
    UNIQUE KEY uk_user_group (user_id, group_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友分组表';

-- 插入默认分组
INSERT INTO cs_friend_group (user_id, group_name, group_order, created_at, updated_at, created_by, updated_by)
VALUES 
    ('system', '默认分组', 0, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 'system'),
    ('system', '同事', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 'system'),
    ('system', '朋友', 2, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 'system'),
    ('system', '家人', 3, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 'system');

-- 创建黑名单表（用于更精细的黑名单管理）
CREATE TABLE IF NOT EXISTS cs_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    blocked_user_id VARCHAR(64) NOT NULL COMMENT '被拉黑的用户ID',
    block_reason VARCHAR(200) DEFAULT NULL COMMENT '拉黑原因',
    blocked_at BIGINT NOT NULL COMMENT '拉黑时间',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    
    -- 索引
    KEY idx_user_id (user_id),
    KEY idx_blocked_user_id (blocked_user_id),
    KEY idx_blocked_at (blocked_at),
    
    -- 唯一约束：同一对用户之间只能有一条拉黑记录
    UNIQUE KEY uk_user_blocked (user_id, blocked_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

-- 优化索引
CREATE INDEX idx_cs_friend_users_status ON cs_friend(from_user_id, to_user_id, status);
CREATE INDEX idx_cs_friend_pending ON cs_friend(to_user_id, status, applied_at) WHERE status = 0;
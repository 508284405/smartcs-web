-- 聊天模块 - 群组与成员表（MVP）

-- 群组表
CREATE TABLE IF NOT EXISTS t_im_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    group_id BIGINT NOT NULL COMMENT '群ID',
    group_name VARCHAR(100) NOT NULL COMMENT '群名称',
    owner_id BIGINT NOT NULL COMMENT '群主用户ID',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT COMMENT '更新时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_group_id (group_id),
    KEY idx_owner (owner_id)
) COMMENT='IM 群组表';

-- 群成员表
CREATE TABLE IF NOT EXISTS t_im_group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    group_id BIGINT NOT NULL COMMENT '群ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) DEFAULT 'MEMBER' COMMENT '角色：OWNER/ADMIN/MEMBER',
    joined_at BIGINT NOT NULL COMMENT '加群时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_group_user (group_id, user_id),
    KEY idx_user (user_id)
) COMMENT='IM 群成员表';


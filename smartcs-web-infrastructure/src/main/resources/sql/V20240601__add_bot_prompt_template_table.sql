-- 创建Bot模块的Prompt模板表
CREATE TABLE IF NOT EXISTS `t_bot_prompt_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `created_at` bigint(20) DEFAULT NULL COMMENT '创建时间（毫秒）',
  `updated_at` bigint(20) DEFAULT NULL COMMENT '更新时间（毫秒）',
  `template_key` varchar(64) NOT NULL COMMENT '模板标识',
  `template_content` text NOT NULL COMMENT '模板内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_key` (`template_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bot模块的Prompt模板表';

-- 插入一个基本的RAG查询模板
INSERT INTO `t_bot_prompt_template` (`is_deleted`, `created_by`, `created_at`, `updated_by`, `updated_at`, `template_key`, `template_content`)
VALUES (0, 'system', UNIX_TIMESTAMP() * 1000, 'system', UNIX_TIMESTAMP() * 1000, 'RAG_QUERY', 
'你是一个智能客服助手，根据以下知识信息回答用户问题，如果无法从知识中找到答案，请诚实地告知用户。
历史对话：
{{history}}

相关知识：
{{docs}}

用户问题：{{question}}

请根据以上知识和对话历史，以专业、友好的口吻回答用户问题。如果能够从知识中找到答案，请直接回答。如果知识中没有相关信息，请说明无法提供答案。不要编造信息。'); 
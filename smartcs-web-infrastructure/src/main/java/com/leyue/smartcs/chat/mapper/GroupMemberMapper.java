package com.leyue.smartcs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.dataobject.GroupMemberDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群成员Mapper接口
 * 
 * @author Claude
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMemberDO> {
    
    /**
     * 获取群成员列表
     *
     * @param groupId 群ID
     * @return 群成员列表
     */
    List<GroupMemberDO> findByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 获取群成员ID列表
     *
     * @param groupId 群ID
     * @return 成员ID列表
     */
    List<Long> findMemberIdsByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 检查用户是否为群成员
     *
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 成员记录（如果存在）
     */
    GroupMemberDO findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * 更新群成员角色
     *
     * @param groupId 群ID
     * @param userId 用户ID
     * @param role 新角色
     * @return 影响的行数
     */
    int updateMemberRole(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("role") String role);
    
    /**
     * 移除群成员（软删除）
     *
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 影响的行数
     */
    int removeMember(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * 获取群成员数量
     *
     * @param groupId 群ID
     * @return 成员数量
     */
    int countByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 批量添加群成员
     * 
     * @param members 群成员列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<GroupMemberDO> members);
}
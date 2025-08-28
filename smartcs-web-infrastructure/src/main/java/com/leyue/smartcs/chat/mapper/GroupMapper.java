package com.leyue.smartcs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.dataobject.GroupDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群组Mapper接口
 * 
 * @author Claude
 */
@Mapper
public interface GroupMapper extends BaseMapper<GroupDO> {
    
    /**
     * 根据群ID查询群组信息
     *
     * @param groupId 群ID
     * @return 群组信息
     */
    GroupDO findByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 根据群主ID查询群组列表
     *
     * @param ownerId 群主ID
     * @return 群组列表
     */
    List<GroupDO> findByOwnerId(@Param("ownerId") Long ownerId);
    
    /**
     * 获取用户参与的群组列表（通过群成员表关联）
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    List<GroupDO> findUserGroups(@Param("userId") Long userId);
    
    /**
     * 软删除群组
     *
     * @param groupId 群ID
     * @return 影响的行数
     */
    int deleteByGroupId(@Param("groupId") Long groupId);
}
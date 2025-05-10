package com.leyue.smartcs.chat.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.chat.database.dataobject.CsUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface CsUserMapper extends BaseMapper<CsUserDO> {
    
    /**
     * 根据用户类型查询用户列表
     *
     * @param userType 用户类型
     * @return 用户列表
     */
    @Select("SELECT * FROM t_cs_user WHERE user_type = #{userType} AND status = 1 AND is_deleted = 0")
    List<CsUserDO> findUsersByType(@Param("userType") Integer userType);
    
    /**
     * 根据状态查询客服列表
     *
     * @param status 状态
     * @return 客服列表
     */
    @Select("SELECT * FROM t_cs_user WHERE user_type = 1 AND status = #{status} AND is_deleted = 0")
    List<CsUserDO> findAgentsByStatus(@Param("status") Integer status);
}

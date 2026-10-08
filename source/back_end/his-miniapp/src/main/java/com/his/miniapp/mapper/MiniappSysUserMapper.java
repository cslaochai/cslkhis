package com.his.miniapp.mapper;

import com.his.miniapp.vo.MiniUserRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 微信登录
 */
@Mapper
public interface MiniappSysUserMapper {

    @Select("""
            SELECT id, user_name, real_name, user_type, patient_id, status
            FROM sys_user
            WHERE openid = #{openid} AND del_flag = 0
            LIMIT 1
            """)
    MiniUserRowVO selectByOpenid(@Param("openid") String openid);
}

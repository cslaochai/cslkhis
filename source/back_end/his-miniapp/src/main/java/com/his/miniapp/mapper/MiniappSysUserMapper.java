package com.his.miniapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * 微信登录用用户只读 Mapper（裸 SQL，铁律：跨模块读异模块表用裸 SQL Mapper）。
 *
 * <p>只做「openid ↔ 患者账号」的查找；openid 绑定写操作走
 * his-patient 的 {@code PatientGuardianService.bindOpenid}（唯一性冲突语义已实现）。
 */
@Mapper
public interface MiniappSysUserMapper {

    @Select("""
            SELECT id, user_name, real_name, user_type, patient_id, status
            FROM sys_user
            WHERE openid = #{openid} AND del_flag = 0
            LIMIT 1
            """)
    Map<String, Object> selectByOpenid(@Param("openid") String openid);
}

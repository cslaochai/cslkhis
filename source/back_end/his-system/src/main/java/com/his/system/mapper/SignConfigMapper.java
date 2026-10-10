package com.his.system.mapper;

import org.apache.ibatis.annotations.*;

/**
 * 签名模块读取系统参数表的运维参数。
 */
@Mapper
public interface SignConfigMapper {

    @Select("SELECT config_value FROM sys_config WHERE config_key = #{key} LIMIT 1")
    String selectValue(@Param("key") String key);

    /**
     * G6b 运维接口用：更新配置值。
     * config_id 非自增（应用层雪花），所以拆成「先更后插」两步——
     * 配置行几乎总是已存在，UPDATE 命中即完事；没有才走 {@link #insertValue}。
     */
    @Update("UPDATE sys_config SET config_value = #{value} WHERE config_key = #{key}")
    int updateValue(@Param("key") String key, @Param("value") String value);

    /**
     * 新建配置行（id 由调用方经序列生成；config_key 唯一键冲突由调用方按并发处理）
     */
    @Insert("INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark) "
            + "VALUES (#{id}, #{name}, #{key}, #{value}, 1, 1, #{remark})")
    int insertValue(@Param("id") long id, @Param("key") String key, @Param("value") String value,
                    @Param("name") String name, @Param("remark") String remark);
}

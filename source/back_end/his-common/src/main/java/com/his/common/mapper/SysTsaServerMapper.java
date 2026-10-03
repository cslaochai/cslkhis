package com.his.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.common.entity.SysTsaServer;
import org.apache.ibatis.annotations.Mapper;

/**
 * TSA 服务注册 Mapper。
 *
 * <p>查询单点走 {@code selectOne(tsa_code)}：一张表一行服务，
 * 逻辑删除由 MP {@code @TableLogic} 自动过滤（BaseEntity 带 del_flag）。
 */
@Mapper
public interface SysTsaServerMapper extends BaseMapper<SysTsaServer> {
}

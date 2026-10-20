package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizYbCatalog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 国家医保目录 Mapper（分页/候选搜索走 MP 条件构造器，见 YbCatalogServiceImpl）。
 */
@Mapper
public interface BizYbCatalogMapper extends BaseMapper<BizYbCatalog> {
}

package com.his.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.common.entity.BizTsaToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 时间戳令牌台账 Mapper（只增不改，无删除端点）。
 */
@Mapper
public interface BizTsaTokenMapper extends BaseMapper<BizTsaToken> {

    /** 台账总条数（Redis 不可用时的取号兜底：当天前缀计数 +1） */
    @Select("SELECT COUNT(*) FROM biz_tsa_token WHERE serial LIKE CONCAT(#{prefix}, '%')")
    long countBySerialPrefix(String prefix);
}

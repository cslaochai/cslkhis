package com.his.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.platform.entity.SysWard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 病区主数据 Mapper。
 */
@Mapper
public interface SysWardMapper extends BaseMapper<SysWard> {

    /**
     * 取现有 W 型病区编码的最大序号（W001 → 1），用于新病区编码续号。
     *
     * <p>只认 {@code W + 纯数字} 的编码；造数留下的 P0WARD / BCUI 之类不参与续号，
     * 避免 CAST 成 0 把序号拉回 001 撞唯一键。
     */
    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(ward_code, 2) AS UNSIGNED)), 0) "
            + "FROM sys_ward WHERE ward_code REGEXP '^W[0-9]+$'")
    int selectMaxWardSeq();
}

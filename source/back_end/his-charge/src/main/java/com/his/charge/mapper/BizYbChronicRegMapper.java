package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizYbChronicReg;
import com.his.charge.vo.ChronicRegSummaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 慢特病人员备案 Mapper（uk_chronic_active 撞键在 service 里预检成业务提示）。
 */
@Mapper
public interface BizYbChronicRegMapper extends BaseMapper<BizYbChronicReg> {

    /**
     * 备案台账汇总：有效/已过期待续备/已注销/已驳回。
     * 「已过期」是展示态（reg_status=1 且 valid_end 早于当天），由这里现算，库里不落状态列。
     */
    @Select("SELECT "
            + "SUM(CASE WHEN reg_status = 1 AND (valid_end IS NULL OR valid_end >= CURDATE()) THEN 1 ELSE 0 END) AS validCount, "
            + "SUM(CASE WHEN reg_status = 1 AND valid_end IS NOT NULL AND valid_end < CURDATE() THEN 1 ELSE 0 END) AS expiredCount, "
            + "SUM(CASE WHEN reg_status = 2 THEN 1 ELSE 0 END) AS cancelledCount, "
            + "SUM(CASE WHEN reg_status = 3 THEN 1 ELSE 0 END) AS rejectedCount "
            + "FROM biz_yb_chronic_reg WHERE del_flag = 0")
    ChronicRegSummaryVO summary();
}


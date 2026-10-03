package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizPatientMergeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 患者主索引合并审计 Mapper（P5.1 EMPI）
 */
@Mapper
public interface BizPatientMergeLogMapper extends BaseMapper<BizPatientMergeLog> {

    /**
     * 统计某天已生成的合并流水号数量，用于生成当日序号。
     *
     * <p>用 COUNT 而不是 MAX(merge_no) + 1：一旦某天出现序号空洞（例如撤销/补偿），
     * MAX 会一直顶在空洞上不前进。COUNT 配合唯一索引重试更稳。
     */
    @Select("SELECT COUNT(*) FROM biz_patient_merge_log WHERE merge_no LIKE CONCAT(#{prefix}, '%')")
    long countByNoPrefix(@Param("prefix") String prefix);
}

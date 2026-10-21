package com.his.outpatient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.enums.ClinicSourceChangeTypeEnum;
import com.his.outpatient.dto.ClinicSourceLogQueryDTO;
import com.his.outpatient.entity.BizClinicSource;
import com.his.outpatient.vo.ClinicSourceLogVO;

/**
 * 门诊排班变更留痕服务。
 *
 * <p>停诊/启用/加号/批量退号/段级编辑/新增/修改/删除/复刻都经此落账，
 * 与业务事务同生共死；原因列超宽截断，防止把业务顶成 500。
 */
public interface ClinicSourceLogService {

    /**
     * 记录一条变更留痕。
     *
     * @param scheduleId 门诊排班ID（批量/复刻按每条落账，必填）
     * @param type       变更动作
     * @param amount     变更数量（可空）
     * @param reason     变更原因（可空；超 200 字符截断）
     * @param schedule   排班快照（取 日期/科室/医生名；可为 null，删除类调用方务必传——排班随即物理删，
     *                   不冗余快照列，审计将只剩一条查不到任何上下文的裸 ID）
     */
    Long record(Long scheduleId, ClinicSourceChangeTypeEnum type, Integer amount, String reason, BizClinicSource schedule);

    /**
     * 分页查询（按发生时间倒序）。
     */
    IPage<ClinicSourceLogVO> pageByQuery(ClinicSourceLogQueryDTO queryDTO);
}

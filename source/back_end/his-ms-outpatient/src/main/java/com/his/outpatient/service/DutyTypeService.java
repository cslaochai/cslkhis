package com.his.outpatient.service;

import com.his.outpatient.dto.DutyTypeUpsertDTO;
import com.his.outpatient.vo.DutyTypeVO;

import java.util.List;

/**
 * 值班类型（值班线）维护服务。
 */
public interface DutyTypeService {

    /**
     * 全部值班类型（含停用）：列表页与排班页的类型筛选/下拉共用一次取数
     */
    List<DutyTypeVO> listVO();

    /**
     * 新增/修改值班类型
     */
    Long upsert(DutyTypeUpsertDTO upsertDTO);

    /**
     * 删除值班类型：仍挂着点位的类型不允许删（否则那条线的班次无人可排）
     */
    void deleteById(Long id);

    /**
     * 按ID取启用中的类型，取不到抛业务异常（排班侧调用，typo 与停用都在这里拦）
     */
    com.his.outpatient.entity.BizDutyType requireEnabled(Long dutyTypeId);

    /**
     * 类型编码 → 类型ID（配置缺失时返回 null，调用侧按「不限类型」回退）
     */
    Long idOfCode(String typeCode);
}

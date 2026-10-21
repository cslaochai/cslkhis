package com.his.outpatient.service;

import com.his.common.base.PageResult;
import com.his.outpatient.dto.DutyPostPageQueryDTO;
import com.his.outpatient.dto.DutyPostUpsertDTO;
import com.his.outpatient.vo.DutyPostSelectListVO;
import com.his.outpatient.vo.DutyPostVO;

import java.util.List;

/**
 * 值守点位服务。
 */
public interface DutyPostService {

    /**
     * 点位下拉：默认只列启用的；责任范围/单元传了则只列该范围内的。
     */
    List<DutyPostSelectListVO> selectListVO(Integer dutyScope, Integer orgType, Long orgId);

    /**
     * 点位分页：按责任范围、单元类型与点位状态筛选，数据范围按岗位科室收口。
     * 用于「值守点位」页的表格与查询卡。
     */
    PageResult<DutyPostVO> pageVO(DutyPostPageQueryDTO dto);

    /**
     * 新增/修改点位。点位编码全局唯一（同码重建会撞键），班次必须存在且启用。
     */
    Long upsert(DutyPostUpsertDTO dto);

    /**
     * 删除点位（物理删）。
     */
    void deleteById(Long id);
}

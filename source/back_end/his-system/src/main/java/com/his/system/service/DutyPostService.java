package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.DutyPostQueryPageDTO;
import com.his.system.dto.DutyPostUpsertDTO;
import com.his.system.vo.DutyPostSelectListVO;
import com.his.system.vo.DutyPostVO;

import java.util.List;

/**
 * 值守点位服务。
 */
public interface DutyPostService {

    /**
     * 点位下拉：默认只列启用的；责任范围/单元传了则只列该范围内的。
     */
    List<DutyPostSelectListVO> selectListVO(Integer dutyScope, Integer orgType, Long orgId);

    PageResult<DutyPostVO> pageVO(DutyPostQueryPageDTO dto);

    /**
     * 新增/修改点位。点位编码全局唯一（同码重建会撞键），班次必须存在且启用。
     */
    Long upsert(DutyPostUpsertDTO dto);

    /**
     * 删除点位（物理删）。
     */
    void deleteById(Long id);
}

package com.his.charge.service;

import com.his.charge.dto.*;
import com.his.charge.vo.ChronicCatalogVO;
import com.his.charge.vo.ChronicRegListVO;
import com.his.charge.vo.ChronicRegSummaryVO;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 门诊慢特病服务：病种目录（参照数据，只启停不删）+ 人员备案（谁办的备案是这张表的核心字段）。
 */
public interface YbChronicService {

    /**
     * 病种目录分页
     */
    PageResult<ChronicCatalogVO> catalogListPage(ChronicCatalogQueryPageDTO queryDTO);

    /**
     * 启用中的病种目录（备案表单的下拉候选）
     */
    List<ChronicCatalogVO> selectCatalogList();

    /**
     * 病种目录新增/修改（disease_code 唯一；只启停不删）
     */
    ChronicCatalogVO catalogUpsert(ChronicCatalogUpsertDTO dto);

    /**
     * 病种目录启停
     */
    void changeCatalogStatus(Long id, Integer status);

    /**
     * 备案台账分页（附展示态：有效/已过期/已注销/已驳回）
     */
    PageResult<ChronicRegListVO> regListPage(ChronicRegQueryPageDTO queryDTO);

    /**
     * 备案详情
     */
    ChronicRegListVO regGetById(Long id);

    /**
     * 患者在用的门特资格（医生站判断能不能走门特报销）
     */
    List<ChronicRegListVO> regActiveOfPatient(Long patientId);

    /**
     * 备案汇总
     */
    ChronicRegSummaryVO regSummary();

    /**
     * 新增/修改备案（单号服务端生成；经办人默认当前登录人；仅「有效」可改）
     */
    ChronicRegListVO regUpsert(ChronicRegUpsertDTO dto);

    /**
     * 注销（有效 → 已注销，终态不可逆）
     */
    void regCancel(ChronicRegTerminalDTO dto);

    /**
     * 驳回（有效 → 已驳回，终态不可逆；患者补材料后另起新单）
     */
    void regReject(ChronicRegTerminalDTO dto);
}

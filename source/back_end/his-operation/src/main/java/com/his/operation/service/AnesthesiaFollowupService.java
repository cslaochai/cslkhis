package com.his.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.AnesthesiaFollowupQueryPageDTO;
import com.his.operation.dto.AnesthesiaFollowupUpsertDTO;
import com.his.operation.vo.AnesthesiaFollowupVO;
import com.his.operation.vo.OperationApplyVO;

import java.util.List;

/**
 * 麻醉术后随访服务
 */
public interface AnesthesiaFollowupService {

    /**
     * 随访分页
     */
    IPage<AnesthesiaFollowupVO> listPage(AnesthesiaFollowupQueryPageDTO query);

    /**
     * 随访详情（含并发症字典）
     */
    AnesthesiaFollowupVO getDetailById(Long id);

    /**
     * 某条麻醉记录的全部随访（按轮次升序 = 这条链的发生顺序）
     */
    List<AnesthesiaFollowupVO> listByRecord(Long recordId);

    /**
     * 新增/修改草稿（返回随访单号；轮次服务端定）
     */
    String save(AnesthesiaFollowupUpsertDTO dto);

    /**
     * 完成随访（草稿 → 已完成；疼痛/恢复必填，并发症三件套齐）
     */
    void finish(Long id);

    /**
     * 删除（仅草稿）
     */
    void deleteById(Long id);

    /**
     * 随访欠账数（工作台角标）
     */
    long countOverduePending();

    /**
     * 并发症要点字典
     */
    List<OperationApplyVO.CheckItem> adverseItems();
}

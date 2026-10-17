package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.WardDispenseActionDTO;
import com.his.pharmacy.dto.WardDispenseGenerateDTO;
import com.his.pharmacy.dto.WardDispenseQueryPageDTO;
import com.his.pharmacy.vo.WardDispenseCandidateVO;
import com.his.pharmacy.vo.WardDispenseStatsVO;
import com.his.pharmacy.vo.WardDispenseVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 住院摆药服务（G13）。
 */
public interface WardDispenseService {

    /**
     * 可摆药医嘱候选（生成前预览）
     */
    List<WardDispenseCandidateVO> candidates(Long wardId, Long admissionId, java.time.LocalDate dispenseDate);

    /**
     * 生成摆药单（同入院同日复用主单、明细追加；重复生成幂等）
     *
     * @return 主单 VO（items 为本次生成后的全量明细）
     */
    WardDispenseVO generate(WardDispenseGenerateDTO dto);

    /**
     * 摆药单分页
     */
    PageResult<WardDispenseVO> listPage(WardDispenseQueryPageDTO dto);

    /**
     * 详情（主单 + 明细）
     */
    WardDispenseVO getDetailById(Long id);

    /**
     * 配药：FEFO 扣库存 + 计费进住院费用单（1 → 2）
     */
    WardDispenseVO dispenseItem(WardDispenseActionDTO dto);

    /**
     * 病区核对（2 → 3）
     */
    WardDispenseVO checkItem(WardDispenseActionDTO dto);

    /**
     * 退药：回库 + 负冲账（2/3 → 4，终态不可逆）
     */
    WardDispenseVO returnItem(WardDispenseActionDTO dto);

    /**
     * 统计（按日期 + 可选病区，明细状态计数）
     */
    WardDispenseStatsVO stats(LocalDate dispenseDate, Long wardId);
}

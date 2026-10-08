package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.AntibioticStatsGenerateDTO;
import com.his.pharmacy.dto.AntibioticStatsQueryPageDTO;
import com.his.pharmacy.dto.IncisionReviewQueryPageDTO;
import com.his.pharmacy.dto.IncisionReviewUpsertDTO;
import com.his.pharmacy.vo.AntibioticStatsVO;
import com.his.pharmacy.vo.IncisionCandidateVO;
import com.his.pharmacy.vo.IncisionReviewVO;

import java.util.List;

/**
 * 抗菌药物使用监测（使用率 / 使用强度 AUD / 微生物送检率）+ I 类切口预防用药点评。
 */
public interface AntibioticMonitorService {

    /** 已生成的监测指标分页 */
    PageResult<AntibioticStatsVO> listPage(AntibioticStatsQueryPageDTO query);

    /** 实时试算（不落库；页面顶部"当前试算"用） */
    AntibioticStatsVO previewStats(String statMonth);

    /** 生成/重算月度快照（同月同范围覆盖） */
    List<AntibioticStatsVO> generateStats(AntibioticStatsGenerateDTO dto);

    /** 导出监测指标 CSV（BOM + 上限 5000 行） */
    String statsExportCsv(AntibioticStatsQueryPageDTO query);

    /** 待点评的 I 类切口手术（含围手术期抗菌药物医嘱证据） */
    List<IncisionCandidateVO> incisionCandidates();

    /** 点评记录分页 */
    PageResult<IncisionReviewVO> incisionReviewListPage(IncisionReviewQueryPageDTO query);

    /** 提交/重评点评结论 */
    IncisionReviewVO incisionReviewUpsert(IncisionReviewUpsertDTO dto);
}

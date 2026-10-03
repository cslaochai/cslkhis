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
 *
 * <p>指标全部按"月度快照"落抗菌药物使用监测指标：对外报数与评审取证的数必须能复现，
 * 实时查询会随基础数据补录漂移（sql/161 §5 已写明理由）。
 * 实时试算另给 {@link #previewStats(String)}，只用于"这个月现在大概是多少"，不写库。
 */
public interface AntibioticMonitorService {

    /** 已生成的监测指标分页 */
    PageResult<AntibioticStatsVO> statsListPage(AntibioticStatsQueryPageDTO query);

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

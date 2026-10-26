package com.his.spi.service;

import java.util.Collection;
import java.util.Map;

/**
 * 诊疗科目映射对外端口（依赖倒置）。
 *
 * <p>解决的问题：院内科室编码是本院自编码，而病案首页、卫生统计直报、医保结算清单上报时，
 * 「科室」填的是国家卫健委《医疗机构诊疗科目名录》的诊疗科目代码（如 03.01 呼吸内科专业）。
 * 映射关系维护在平台模块（`sys_dept_subject_map`），住院/收费等模块不能直接查那张表，
 * 一律走本端口。
 *
 * <p>口径约定：
 * <ul>
 *   <li>只做**折算**不做替换——病案首页自身仍存院内 dept_id，上报时才取科目码；</li>
 *   <li>没配映射返回 {@code null}，**不兜默认科目**。上报端宁可留空让人发现有科室漏配，
 *       也不要塞一个错的科目码过去——错码比空值更难查。</li>
 * </ul>
 */
public interface MedSubjectFacadeService {

    /**
     * 取科室的上报主科目代码。
     *
     * @param deptId 科室ID（可空，为空返回 null）
     * @return 未配映射时返回 {@code null}
     */
    String getPrimarySubjectCode(Long deptId);

    /**
     * 批量取科室的上报主科目代码，给病案首页列表 / 统计导出用（避免按行逐条查）。
     *
     * @param deptIds 科室ID集合
     * @return 只包含配了主科目的科室；无匹配返回空 Map（不是 null）
     */
    Map<Long, String> getPrimarySubjectCodes(Collection<Long> deptIds);

    /**
     * 按科目代码取科目名称。上报报文里码和中文名要一起出。
     *
     * @param subjectCode 诊疗科目代码
     * @return 不存在时返回 {@code null}
     */
    String getSubjectName(String subjectCode);
}

package com.his.system.service;

import com.his.system.vo.StaffDemandGapVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 人力需求与缺口（闭环第 ① 步：先有需求，排班才知道要排多少人）。
 */
public interface StaffDemandService {

    /**
     * 缺口清单：某段日期里每个单元每个岗位类别「需求 / 在岗 / 缺多少」。
     *
     * @param startDate 开始日期（必填）
     * @param endDate   结束日期（必填）
     * @param orgType   排班单元类型（1-科室 2-病区 3-全院），空=不限
     * @param orgId     排班单元ID，空=不限（只给 orgId 不给 orgType 是不行的：两种单元
     *                  id 不在同一空间，只给 id 会张冠李戴）
     * @param staffType 岗位类别（1-医生 2-护理 …），空=不限
     */
    List<StaffDemandGapVO> gapList(LocalDate startDate, LocalDate endDate,
                                   Integer orgType, Long orgId, Integer staffType);

    /**
     * 重算某段日期的需求（住院患者派生 + 门诊出诊派生）。
     *
     * <p>覆盖谁、不动谁：只覆盖 demand_source IN (1,2) 的派生行，
     * 护士长手工调过的（source=3）原样保留 —— 系统算的永远不能盖掉人拍板的数。
     *
     * @return 三类派生各写了多少行
     */
    DemandRecalcResult recalc(LocalDate startDate, LocalDate endDate);

    /**
     * 护士长手工调整某单元某天某岗位的需求人数。
     *
     * <p>写进去的是 source=3：从这一刻起这条需求归人负责，重算不再覆盖它。
     * 系统原来算的数会被记进测算依据，将来想退回系统口径有据可查。
     *
     * @param demandDate    需求日期
     * @param orgType       1-科室 2-病区
     * @param orgId         单元ID
     * @param staffType     岗位类别
     * @param requiredCount 需求人数（至少 1）
     */
    void adjust(LocalDate demandDate, Integer orgType, Long orgId, Integer staffType,
                Integer requiredCount, String remark);

    /** 重算结果：三类派生各多少行，界面上要能看出「重算到底动了什么」 */
    class DemandRecalcResult {
        /** 住院护理派生行数 */
        public int inpatient;
        /** 门诊护理派生行数 */
        public int clinicNurse;
        /** 门诊医生派生行数 */
        public int clinicDoctor;

        public int total() {
            return inpatient + clinicNurse + clinicDoctor;
        }
    }
}

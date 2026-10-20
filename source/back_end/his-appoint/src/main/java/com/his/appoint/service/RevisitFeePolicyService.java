package com.his.appoint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.appoint.dto.RevisitFeePolicyPageQueryDTO;
import com.his.appoint.dto.RevisitFeePolicyUpsertDTO;
import com.his.appoint.entity.RevisitFeePolicy;
import com.his.appoint.vo.RevisitFeePolicyVO;
import com.his.common.base.PageResult;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 复诊收费策略服务：既是配置页的 CRUD 出口，也是「复诊号收多少钱」的唯一判定入口。
 */
public interface RevisitFeePolicyService extends IService<RevisitFeePolicy> {

    /**
     * 判定一次复诊挂号该收哪些费。
     *
     * @param context 判定上下文（来源 + 与原就诊的比对结果 + 排班原价）
     * @return 收费决定，含免费原因文案（写进收费单备注，供事后追溯）
     */
    RevisitFeeDecision decide(RevisitFeeContext context);

    /**
     * 配置页：分页查询
     */
    PageResult<RevisitFeePolicyVO> listPage(RevisitFeePolicyPageQueryDTO queryDTO);

    /**
     * 配置页：单条详情（不改名 getById —— 会和 IService 的 getById 撞签名）
     */
    RevisitFeePolicyVO detail(Long id);

    /**
     * 配置页：新增/修改合一
     */
    void upsert(RevisitFeePolicyUpsertDTO upsertDTO);

    /**
     * 配置页：删除（软删）
     */
    void deleteById(Long id);

/**
 * 判定上下文
 */
    @Data
    class RevisitFeeContext {
        /**
         * 复诊来源（1~4）；null 或 0 = 未知，只有「不限来源」的策略能命中
         */
        private Integer revisitSource;
        /**
         * 是否与原就诊同一医生；null = 判不出来（原病历缺医生），带该条件的策略一律不命中
         */
        private Boolean sameDoctor;
        /**
         * 是否与原就诊同一科室；null 同上
         */
        private Boolean sameDept;
        /**
         * 与原就诊日的间隔天数；null = 判不出来，带 withinDays 的策略一律不命中
         */
        private Long daysSinceOrigin;
        /**
         * 排班上的挂号费原价。
         *
         * <p><b>已废止</b>：湘医保发〔2025〕58号（长沙市医保局同文，2026-01-01 执行）废止挂号费，
         * 合并为门诊诊查费。保留字段仅为历史数据兼容，恒为 0，判定逻辑不读它。
         */
        private BigDecimal registFee;
        /**
         * 排班上的门诊诊查费原价（医保编码 011102020010000）
         */
        private BigDecimal diagnosisFee;
        /**
         * 号源职级加收档位（1-普通 2-副主任医师 3-主任医师 4-知名专家）：
         * 决定「主项门诊诊查费」之上要不要出加收子项、出哪一档（011102020010001/2/3）。
         * null = 判不出来，按普通档处理（只出主项）。
         */
        private Integer diagnosisTier;
        /**
         * 患者出生日期：儿童加收 30% 的判定输入（湘医保发〔2025〕58号附件1）。
         * null = 档案缺出生日期 → 不加收（判不出来不猜，见 PediatricSurchargePolicy）。
         */
        private LocalDate birthDate;
        /**
         * 就诊日：儿童年龄按这天算，不按挂号当天（预约可提前数周）。
         */
        private LocalDate visitDate;
    }

/**
 * 收费决定
 */
    @Data
    class RevisitFeeDecision {
        /**
         * 应收挂号费（已按策略减免）。
         *
         * <p><b>已废止</b>：挂号费按湘医保发〔2025〕58号并入门诊诊查费，本轮整改后恒为 0。
         */
        private BigDecimal registFee = BigDecimal.ZERO;
        /**
         * 应收门诊诊查费（已按策略减免）
         */
        private BigDecimal diagnosisFee = BigDecimal.ZERO;
        /**
         * 职级加收档位（1-普通 2-副主任医师 3-主任医师 4-知名专家）：透传给收费层，
         * 决定加收子项的院内码与名称。
         */
        private Integer diagnosisTier;
        /**
         * 主项门诊诊查费金额（普通档全额，如 9；儿童加收后 = 9×1.3）。
         * 与 {@link #surchargeFee} 合计 = {@link #diagnosisFee}（儿童总价），二者口径必须一致。
         */
        private BigDecimal baseFee = BigDecimal.ZERO;
        /**
         * 职级加收项金额（普通档为 0；副主任/主任/知名专家 = 对应加收额，儿童加收后同步 ×1.3）。
         * 0 表示不出加收子项。
         */
        private BigDecimal surchargeFee = BigDecimal.ZERO;
        /**
         * 合计为 0：收费单照样要建，且直接置「已收费」，否则患者签不了到
         */
        private boolean waived;
        /**
         * 命中的收费方式（1-全额 2-原「免挂号费」 3-全免）；未命中策略为 1。
         * 挂号费废止后，2 与 3 实际等价（都免掉门诊唯一费用），2 是历史码值不做废以免改存量数据。
         */
        private Integer chargeMode;
        /**
         * 命中的策略ID（雪花，字符串化由上层处理）
         */
        private Long policyId;
        /**
         * 命中的策略名称
         */
        private String policyName;
        /**
         * 判定说明（写进收费单备注，医保/审计追问时的依据）
         */
        private String reason;
        /**
         * 本次是否适用了儿童加收 30%（前端据此显示「含儿童加收」提示）。
         */
        private boolean childSurcharged;
        /**
         * 儿童加收额（base × 30%）。未加收时为 0。
         */
        private BigDecimal childSurchargeAmount = BigDecimal.ZERO;
        /**
         * 号源原价（加收前）。前端要显示「原价 ¥70 → 含加收 ¥91」时用它，
         * 别让前端拿 diagnosisFee 反推（那是加收后的数，反推不出原价）。
         */
        private BigDecimal baseDiagnosisFee = BigDecimal.ZERO;

        /**
         * 本档应收总额（儿童加收后的门诊诊查费）。免收命中时为 0，收费侧据此建 0 元单并直接置已收费。
         */
        public BigDecimal totalFee() {
            return diagnosisFee;
        }
    }
}

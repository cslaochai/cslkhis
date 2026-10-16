package com.his.appoint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.appoint.dto.RevisitFeePolicyQueryPageDTO;
import com.his.appoint.dto.RevisitFeePolicyUpsertDTO;
import com.his.appoint.entity.BizRevisitFeePolicy;
import com.his.appoint.vo.RevisitFeePolicyVO;
import com.his.common.base.PageResult;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 复诊收费策略服务：既是配置页的 CRUD 出口，也是「复诊号收多少钱」的唯一判定入口。
 */
public interface BizRevisitFeePolicyService extends IService<BizRevisitFeePolicy> {

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
    PageResult<RevisitFeePolicyVO> listPage(RevisitFeePolicyQueryPageDTO queryDTO);

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
         * 排班上的挂号费原价
         */
        private BigDecimal registFee;
        /**
         * 排班上的诊查费原价
         */
        private BigDecimal diagnosisFee;
    }

/**
 * 收费决定
 */
    @Data
    class RevisitFeeDecision {
        /**
         * 应收挂号费（已按策略减免）
         */
        private BigDecimal registFee = BigDecimal.ZERO;
        /**
         * 应收诊查费（已按策略减免）
         */
        private BigDecimal diagnosisFee = BigDecimal.ZERO;
        /**
         * 合计为 0：收费单照样要建，且直接置「已收费」，否则患者签不了到
         */
        private boolean waived;
        /**
         * 命中的收费方式（1-全额 2-免挂号费 3-全免）；未命中策略为 1
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

        public BigDecimal totalFee() {
            return registFee.add(diagnosisFee);
        }
    }
}

package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.DeathCertificateDTO;
import com.his.patient.vo.DeathCertificateVO;

import java.time.LocalDateTime;

/**
 * 居民死亡医学证明（推断）书：填写 → 审核 → 签发 → 打印 → 死因监测上报，错证只能作废重开。
 *
 * <p>死亡事实的唯一来源仍是出院办理（出院记录的死亡标记=1）：本服务不写出院，
 * 但<b>签发</b>这一对外动作必须以「该住院已办死亡离院」为前提，且死亡时间与出院时间是同一时点。
 */
public interface DeathCertificateService {

    /**
     * 证明台账分页（含上报台账，按 reportStatus/overdue 收口）
     */
    PageResult<DeathCertificateVO.Row> listPage(DeathCertificateDTO.QueryPage query);

    /**
     * 待开证榜：已办死亡离院但没有有效证明的住院（欠账榜，不硬拦出院）
     */
    PageResult<DeathCertificateVO.PendingRow> pendingListPage(DeathCertificateDTO.QueryPage query);

    /**
     * 详情＝编辑回显（一般项目明文 + 死因链 + 上报报文）
     */
    DeathCertificateVO.Detail getDetailById(Long id);

    /**
     * 开证底稿：按住院带出死者一般项目快照与死亡离院时间（表单默认值服务端算，不采信前端）
     */
    DeathCertificateVO.PatientSnapshot admissionBase(Long admissionId);

    /**
     * 填写/修改（草稿与已审核可改；已开具禁改，只能作废重开）
     */
    Long upsert(DeathCertificateDTO.Upsert dto);

    /**
     * 审核（1→2）
     */
    void audit(DeathCertificateDTO.Audit dto);

    /**
     * 签发（2→3）：审核通过 + 已办死亡离院 + 死因链与根本死因齐备
     */
    void issue(DeathCertificateDTO.Issue dto);

    /**
     * 作废（1/2/3→4），之后才能重开新证
     */
    void voidCert(DeathCertificateDTO.VoidCert dto);

    /**
     * 重开：按被作废的原证复制一张新草稿，orig_cert_id 指向原证（原证内容永不改）
     */
    Long reissue(Long origCertId);

    /**
     * 四联打印回执：只有已开具的证能打印，每打一次计一次数（法定文书打印留痕）
     */
    void print(DeathCertificateDTO.Print dto);

    /**
     * 死因监测上报（当前不对接外部平台：组装标准报文落库留痕，真实对接时本方法是唯一替换点）
     */
    String report(Long id);

    /**
     * 逾期催报（定时 + 手工补跑双路径，按天幂等），返回发送条数
     */
    int notifyOverdue();

    /**
     * 统计卡：证明四态 + 上报三态 + 待开证/待登记欠账
     */
    DeathCertificateVO.Stats stats();

    /**
     * 出院办理的前置校验（由住院出院流程调用）：
     * 该住院若已有有效死亡证明，出院时间必须等于证明的死亡时间——同一个时点，不许两处编。
     */
    void assertDischargeConsistent(Long admissionId, LocalDateTime dischargeTime);
}

package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.*;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.vo.*;

import java.util.List;

/**
 * 住院病历文书服务（P2）。
 *
 * <p>状态机（沿用病案首页口径，不另造一套）：
 * <pre>
 *   保存 ──→ 1 草稿 ──提交──→ 2 已提交 ──归档──→ 3 已归档（单向门，禁改）
 *                ↑  ↑______________│  (提交后仍可修改，但每次修改逐字段留痕)
 * </pre>
 *
 * <p>两条不可破的业务铁律：
 * <ol>
 *   <li><b>归档后禁止修改</b>（病案封存）：任何写操作对已归档文书一律拒绝，不做"改了但提醒一下"。</li>
 *   <li><b>每次修改逐字段留痕</b>：只在值真的变了时写一行（谁、什么时候、哪个字段、原值、新值）。
 *       飞检问"这句话是谁改的"，日志答不上来等于没有日志。</li>
 * </ol>
 */
public interface InpatientRecordService {

    /**
     * 新增 / 修改病历文书
     *
     * @return 保存后的完整详情（创建类接口必须回传 VO）
     */
    InpatientRecordDetailVO save(InpatientRecordUpsertDTO dto);

    /**
     * 文书详情（含结构化要素逐项明细）
     */
    InpatientRecordDetailVO detail(Long id);

    /**
     * 文书分页（列表行不带长文本）
     */
    IPage<InpatientRecordVO> listPage(InpatientRecordQueryPageDTO query);

    /**
     * 提交（批量；草稿 → 已提交，记录 submitTime 与书写医生）
     *
     * @return 实际提交份数
     */
    int submit(InpatientRecordSubmitDTO dto);

    /**
     * 归档（批量；已提交 → 已归档。归档是单向门，归档说明必填）
     *
     * @return 实际归档份数
     */
    int archive(InpatientRecordArchiveDTO dto);

    /**
     * 修改日志分页（可按单据查，也可按入院查"这次住院所有文书的修改轨迹"）
     */
    IPage<InpatientRecordLogVO> logPage(InpatientRecordLogQueryPageDTO query);

    /**
     * 某份文书的全部修改日志（按时间升序，不分页）
     */
    List<InpatientRecordLogVO> logList(Integer docType, Long recordId);

    /**
     * 结构化率统计（总体 + 分组 + 逐要素 + 逐份文书）
     */
    RecordQualityStatVO qualityStat(Long admissionId);

    /**
     * 病历文书类型下拉
     */
    List<CodeOptionVO> typeOptions();

    /**
     * 文书状态下拉
     */
    List<CodeOptionVO> statusOptions();

    /**
     * 临床闭环（手术、输血等）回写正式文书。正文由发起方组装，本方法只负责病历号取号与落库
     * —— 病历号是病历文书的编号，取号规则归本服务，不许第二个地方再抄一份。
     *
     * @return 落库后的病历（含ID与病历号，供发起单据回指）
     */
    BizInpatientRecord appendClosedLoopRecord(BizInpatientRecord record);
}

package com.his.spi.service;

import com.his.spi.dto.AiClinicalRuleCheckSaveDTO;
import com.his.spi.dto.AiQualityControlSaveDTO;
import com.his.spi.vo.AiMedicalRecordContentVO;
import com.his.spi.vo.AiPrescriptionBriefVO;
import com.his.spi.vo.AiPrescriptionDetailRowVO;
import com.his.spi.vo.AiQualityControlWriteVO;
import com.his.spi.vo.AiRuleCheckWriteVO;

import java.util.List;

/**
 * 病历域供智能辅助能力取数与回写的端口：病历正文、处方与明细的读取，以及规则核查、质控结论的落库。
 *
 * <p>实现方只依赖本模块 mapper 与系统序号服务，是叶子端口：调用方拿到的是窄投影，
 * 病历实体与其列结构不随契约外泄。
 */
public interface AiEmrDataFacadeService {

    /**
     * 按处方ID取处方主档摘要。
     *
     * <p>用于处方合理性审核入口先确认这张方子存在，并取到性别、年龄与诊断作为临床取值依据。
     *
     * @param prescriptionId 处方ID
     * @return 处方摘要；不存在时返回 {@code null}，报错文案由调用方决定
     */
    AiPrescriptionBriefVO findPrescription(Long prescriptionId);

    /**
     * 取一张处方的全部明细行。
     *
     * <p>用于审核前把药品名、单次剂量、频次与途径摊平成比对清单。
     *
     * @param prescriptionId 处方ID
     * @return 明细行；无明细返回空集合
     */
    List<AiPrescriptionDetailRowVO> listPrescriptionDetails(Long prescriptionId);

    /**
     * 按病历ID取正文与体征快照。
     *
     * <p>用于病历草拟、文本结构化抽取、内涵质控与编码推荐读取本次就诊的临床记录内容。
     *
     * @param recordId 病历ID
     * @return 正文快照；不存在时返回 {@code null}，报错文案由调用方决定
     */
    AiMedicalRecordContentVO findMedicalRecord(Long recordId);

    /**
     * 落一条临床规则核查记录。
     *
     * <p>用于处方审核出结论后写进医生工作站已有的校验列表页，核查单号、核查人与核查时间由本端口生成，
     * 调用方只交代结论本身。
     *
     * @param dto 核查结论
     * @return 落库后的记录ID与单号
     */
    AiRuleCheckWriteVO saveRuleCheck(AiClinicalRuleCheckSaveDTO dto);

    /**
     * 落一条病历质控记录。
     *
     * <p>用于内涵质控出结论后写进质控列表页，质控单号、质控人与质控时间由本端口生成。
     *
     * @param dto 质控结论
     * @return 落库后的记录ID与单号
     */
    AiQualityControlWriteVO saveQualityControl(AiQualityControlSaveDTO dto);
}

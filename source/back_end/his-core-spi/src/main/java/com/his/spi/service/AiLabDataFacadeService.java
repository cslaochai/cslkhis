package com.his.spi.service;

import com.his.spi.dto.AiLabConclusionSaveDTO;
import com.his.spi.vo.AiLabRecordVO;
import com.his.spi.vo.AiLabResultRowVO;
import com.his.spi.vo.AiReportVO;
import com.his.spi.vo.JudgeResultVO;

import java.util.List;

/**
 * 医技域供智能辅助能力取数的端口：检验记录与结果的读取（判定结论随结果一并给出）、报告主档读取、结论写回。
 *
 * <p>异常方向、是否未判定、是否危急值都由医技域按自己的规则算完再给出去，
 * 判定口径全仓只有一份；调用方不参与重算。
 */
public interface AiLabDataFacadeService {

    /**
     * 按检验记录ID取记录摘要。
     *
     * <p>用于检验结果解读入口确认记录存在，并取患者与项目信息拼提示词。
     *
     * @param recordId 检验记录ID
     * @return 记录摘要；不存在时返回 {@code null}，报错文案由调用方决定
     */
    AiLabRecordVO findLabRecord(Long recordId);

    /**
     * 取一条检验记录下的全部结果行，按录入顺序排列并带齐判定结论。
     *
     * <p>用于逐项概览、危急值点名与历史趋势的逐记录取数。
     *
     * @param recordId 检验记录ID
     * @return 结果行；无结果返回空集合
     */
    List<AiLabResultRowVO> listLabResults(Long recordId);

    /**
     * 回溯同一患者、同一检验项目的历史记录，按检测时间倒序。
     *
     * <p>用于检验趋势计算，只要真正做过检测的历史（没有检测时间的记录不参与比对）。
     *
     * @param patientId        患者ID
     * @param laboratoryItemId 检验项目ID（为空表示不限项目）
     * @param excludeRecordId  要排除的当前记录ID
     * @param limit            最多回溯条数
     * @return 历史记录；无历史返回空集合
     */
    List<AiLabRecordVO> listHistoryLabRecords(Long patientId, Long laboratoryItemId, Long excludeRecordId, int limit);

    /**
     * 按报告ID取报告主档摘要。
     *
     * <p>用于患者端报告解读与影像解读确认报告归属与发布状态。
     *
     * @param reportId 报告ID
     * @return 报告摘要；不存在时返回 {@code null}，报错文案由调用方决定
     */
    AiReportVO findReport(Long reportId);

    /**
     * 把解读结论与建议写回检验记录。
     *
     * <p>用于医生在检验解读结果里显式勾选「覆盖保存」后落库，属于覆盖写，调用方需自行确认已获授权。
     *
     * @param dto 结论与建议
     */
    void saveConclusion(AiLabConclusionSaveDTO dto);

    /**
     * 对单个原始检验值独立做一次异常判定 / 危急值判断 / 参考范围数值解析。
     *
     * <p>判定口径（异常方向、危急值硬规则、参考区间解析）全仓只有医技域一份，
     * 调用方不复制这些规则，只消费算好的结论。</p>
     *
     * @param laboratoryItemId 检验项目ID（用于取项目编码/名称/单位与参考区间）
     * @param resultValue     原始检验结果值（可为空，空值按未判定处理）
     * @return 判定结果；项目不存在时返回未判定结论，不返回 {@code null}
     */
    JudgeResultVO judgeValue(Long laboratoryItemId, String resultValue);
}

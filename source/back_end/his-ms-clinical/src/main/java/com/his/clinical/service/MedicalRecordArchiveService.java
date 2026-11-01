package com.his.clinical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.clinical.dto.PaperSignVerifyDTO;
import com.his.clinical.entity.BizMedicalRecordArchive;
import com.his.clinical.vo.BizMedicalRecordArchiveVO;
import com.his.clinical.vo.MedicalRecordArchiveCountVO;

/**
 * 病历归档服务接口
 */
public interface MedicalRecordArchiveService extends IService<BizMedicalRecordArchive> {

    /**
     * 查询归档列表
     *
     * @param keyword 病历号/患者姓名模糊（病案借阅申请的病案选择器用，可空）
     */
    PageResult<BizMedicalRecordArchiveVO> selectArchivePage(Long patientId, Integer archiveStatus, String keyword,
                                                            int pageNum, int pageSize);

    /**
     * 获取归档详情
     */
    BizMedicalRecordArchiveVO getArchiveDetail(Long archiveId);

    /**
     * 纸质签名核验登记：医师无电子证书时，由病案管理科核验纸质手写签名后登记。
     * 纸质核验是替代凭证，不回写病历 sign_status；登记后归档按「纸质签名已核验」放行。
     */
    boolean verifyPaperSign(PaperSignVerifyDTO dto);

    /**
     * 归档病历
     */
    boolean archive(Long archiveId);

    /**
     * 封存病历
     */
    boolean seal(Long archiveId);

    /**
     * 三态计数（待归档 / 已归档 / 已封存 + 合计）。
     *
     * <p>计数必须由后端 group by 出，不能让前端拿「当前页 list」去数 ——
     * 那等于只统计了本页，翻页就变。
     *
     * @return total/pending/archived/sealed 四段计数，Integer（归档量级不会溢出）
     */
    MedicalRecordArchiveCountVO statusCount();

    /**
     * 扫描「归档超期」并发站内信提醒（emr-arch 发送方）。
     *
     * <p>门诊病历的归档截止日是「诊疗结束后的首个工作日」（跳过周末），
     * 过了截止日仍未归档（archive_status=1）即给病历医生发提醒；
     * 每份病历每天最多一条，方法可重入（定时任务与手动补跑共用）。
     *
     * @return 本轮实际发送的消息条数
     */
    int notifyOverdueArchives();

    /**
     * 门诊病历定时自动归档（emr-arch 系统动作）。
     *
     * <p>只扫「待归档」且已具备签名证据（电子签名已签 或 纸质签名已核验）的记录，
     * 以系统身份推进为已归档；缺签名证据的留在待归档继续催办，不在这里放行。
     * 单条失败只记日志，不影响其余病案。
     *
     * @return 本轮实际归档的病案数
     */
    int autoArchiveSigned();
}

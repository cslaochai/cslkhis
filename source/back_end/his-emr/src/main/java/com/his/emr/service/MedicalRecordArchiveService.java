package com.his.emr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.emr.entity.BizMedicalRecordArchive;
import com.his.emr.vo.BizMedicalRecordArchiveVO;
import com.his.emr.vo.MedicalRecordArchiveCountVO;

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
     * <p>门诊病历就诊后超过时限仍未归档（archive_status=1）即给病历医生发提醒；
     * 每份病历每天最多一条，方法可重入（定时任务与手动补跑共用）。
     *
     * @return 本轮实际发送的消息条数
     */
    int notifyOverdueArchives();
}

package com.his.medicaltech.service;

import com.his.medicaltech.dto.ExamImageMockImportDTO;
import com.his.medicaltech.dto.ExamImageUploadDTO;
import com.his.medicaltech.vo.ExamImageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 简化 PACS：检查/检验影像帧（检查影像帧，sql/137）
 */
public interface ExamImageService {

    /**
     * 工作站上传一张影像（seq 由服务端 max+1 递增）
     */
    ExamImageVO upload(MultipartFile file, ExamImageUploadDTO uploadDTO);

    /**
     * 模拟 DICOM 导入：后端生成 frameCount 帧灰阶测试图
     */
    List<ExamImageVO> mockImport(ExamImageMockImportDTO importDTO);

    /**
     * 按申请单取全部帧（seq 升序，阅片器直接铺）
     */
    List<ExamImageVO> listByApply(Integer bizType, Long applyId);

    /**
     * 按报告取影像（报告 → 执行记录 → 申请单）。
     *
     * <p>报告出参要求带 images，而影像只认申请单，这条换算必须由后端做：
     * 让前端自己去翻记录等于把「报告挂在哪个申请单上」复制两份，将来必然漂移。
     */
    List<ExamImageVO> listByReportId(Long reportId);

    /**
     * 删帧：磁盘文件 + 数据库行一起物理删，删前把快照写进审计日志（留痕见审计日志，本表无墓碑行）。
     */
    boolean deleteById(Long id, String reason);
}

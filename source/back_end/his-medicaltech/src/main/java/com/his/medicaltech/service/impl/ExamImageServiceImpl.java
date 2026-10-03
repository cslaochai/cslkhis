package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.exception.BusinessException;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.entity.BizLaboratoryApply;
import com.his.emr.mapper.BizInspectionApplyMapper;
import com.his.emr.mapper.BizLaboratoryApplyMapper;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.entity.BizReport;
import com.his.medicaltech.enums.ReportTypeEnum;
import com.his.medicaltech.dto.ExamImageMockImportDTO;
import com.his.medicaltech.dto.ExamImageUploadDTO;
import com.his.medicaltech.entity.BizExamImage;
import com.his.medicaltech.enums.ExamImageSourceEnum;
import com.his.medicaltech.mapper.BizExamImageMapper;
import com.his.medicaltech.service.ExamImageService;
import com.his.medicaltech.support.MockExamImageSource;
import com.his.medicaltech.vo.ExamImageVO;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.medicaltech.mapper.BizLaboratoryRecordMapper;
import com.his.medicaltech.mapper.BizReportMapper;
import com.his.medicaltech.support.SubDictText;
import com.his.security.UserUtils;
import com.his.system.service.SysAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 简化 PACS 影像帧服务（sql/137）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamImageServiceImpl implements ExamImageService {

    /** 学习阶段只收灰阶/彩色位图：真 DICOM 的 .dcm 需要解析器，阅片器也画不出来 */
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png");
    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final int MAX_FRAME_COUNT = 24;
    private static final int DEFAULT_FRAME_COUNT = 6;
    private static final String UPLOAD_ROOT_REL = "uploads/examImage";
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BizExamImageMapper imageMapper;
    private final BizReportMapper reportMapper;
    private final BizInspectionRecordMapper inspectionRecordMapper;
    private final BizLaboratoryRecordMapper laboratoryRecordMapper;
    private final BizInspectionApplyMapper inspectionApplyMapper;
    private final BizLaboratoryApplyMapper laboratoryApplyMapper;
    private final MockExamImageSource mockExamImageSource;
    private final SubDictText subDictText;
    private final SysAuditLogService sysAuditLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamImageVO upload(MultipartFile file, ExamImageUploadDTO uploadDTO) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的影像文件");
        }
        String originalName = StringUtils.hasText(file.getOriginalFilename())
                ? Paths.get(file.getOriginalFilename()).getFileName().toString() : "image";
        String ext = StringUtils.getFilenameExtension(originalName);
        ext = ext == null ? "" : ext.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("影像文件只支持 jpg/png（当前：" + ext + "）");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("单帧影像不能超过 10MB");
        }
        Anchor anchor = resolveAnchor(uploadDTO.getBizType(), uploadDTO.getApplyId());

        String relativeUrl = saveBytes(buildFileName(ext), readQuietly(file));
        BizExamImage row = newAnchorRow(anchor, uploadDTO.getBizType(), uploadDTO.getApplyId(),
                uploadDTO.getModality());
        row.setSeq(nextSeq(uploadDTO.getBizType(), uploadDTO.getApplyId()));
        row.setFileName(cut(originalName, 255));
        row.setFileUrl(relativeUrl);
        row.setFileSize(file.getSize());
        row.setMimeType(cut(file.getContentType(), 64));
        row.setSource(ExamImageSourceEnum.UPLOAD.getCode());
        imageMapper.insert(row);
        return toVO(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ExamImageVO> mockImport(ExamImageMockImportDTO importDTO) {
        Anchor anchor = resolveAnchor(importDTO.getBizType(), importDTO.getApplyId());
        int frames = importDTO.getFrameCount() == null || importDTO.getFrameCount() <= 0
                ? DEFAULT_FRAME_COUNT : Math.min(importDTO.getFrameCount(), MAX_FRAME_COUNT);
        int startSeq = nextSeq(importDTO.getBizType(), importDTO.getApplyId());
        String modalityText = importDTO.getModality() == null ? null
                : subDictText.text("his_exam_device_type", importDTO.getModality());

        List<ExamImageVO> created = new ArrayList<>();
        try {
            for (int i = 0; i < frames; i++) {
                int seq = startSeq + i;
                byte[] png = mockExamImageSource.frame(anchor.applyNo, seq, startSeq + frames - 1,
                        modalityText, anchor.itemName, anchor.patientName);
                BizExamImage row = newAnchorRow(anchor, importDTO.getBizType(), importDTO.getApplyId(),
                        importDTO.getModality());
                row.setSeq(seq);
                row.setFileName("sim_" + anchor.applyNo + "_" + seq + ".png");
                row.setFileUrl(saveBytes("sim_" + seq + "_" + UUID.randomUUID().toString().replace("-", "") + ".png", png));
                row.setFileSize((long) png.length);
                row.setMimeType("image/png");
                row.setSource(ExamImageSourceEnum.MOCK_DICOM.getCode());
                row.setRemark(cut("模拟 DICOM 导入（非真实影像）", 500));
                imageMapper.insert(row);
                created.add(toVO(row));
            }
        } catch (IOException e) {
            throw new BusinessException("生成模拟影像失败：" + e.getMessage());
        }
        return created;
    }

    @Override
    public List<ExamImageVO> listByApply(Integer bizType, Long applyId) {
        if (applyId == null || bizType == null) {
            // 老数据里存在没有 apply_id 的执行记录（迁移前直接建的），返回空而不是拼出 apply_id is null 的查询
            return new ArrayList<>();
        }
        return toVOList(imageMapper.selectList(new LambdaQueryWrapper<BizExamImage>()
                .eq(BizExamImage::getBizType, bizType)
                .eq(BizExamImage::getApplyId, applyId)
                .orderByAsc(BizExamImage::getSeq)));
    }

    @Override
    public List<ExamImageVO> listByReportId(Long reportId) {
        BizReport report = reportId == null ? null : reportMapper.selectById(reportId);
        if (report == null || report.getRecordId() == null) {
            return new ArrayList<>();
        }
        Long applyId = resolveApplyIdByRecord(report.getReportType(), report.getRecordId());
        return applyId == null ? new ArrayList<>() : listByApply(report.getReportType(), applyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id, String reason) {
        BizExamImage row = id == null ? null : imageMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("影像帧不存在或已被删除");
        }
        // 留痕必须在删之前拿得到内容：物理删之后这行就没了，审计是唯一的历史
        sysAuditLogService.record(UserUtils.getCurrentEmployeeId(), UserUtils.getCurrentEmployeeName(),
                "检查影像", "删除影像帧", "biz_exam_image", row.getId(),
                cut("apply=" + row.getBizType() + "#" + row.getApplyId() + " seq=" + row.getSeq()
                        + " file=" + row.getFileName() + " url=" + row.getFileUrl()
                        + " 原因=" + (StringUtils.hasText(reason) ? reason : "未填写"), 2000),
                true, null);
        deleteFileQuietly(row.getFileUrl());
        return imageMapper.purgeById(id) > 0;
    }

    // 内部

    /** 申请单上的影像锚点快照（一次解析，避免每帧查一次库） */
    private record Anchor(Long applyId, String applyNo, Long patientId, String patientName,
                          String itemName, String bodyPart) {
    }

    private Anchor resolveAnchor(Integer bizType, Long applyId) {
        ReportTypeEnum type = ReportTypeEnum.getByCode(bizType);
        if (type == null) {
            throw new BusinessException("影像单据类型只能是 1-检查 或 2-检验");
        }
        // C类：内部锚点解析的入参是主键参数而非请求 DTO，Bean Validation 够不到，保留
        if (applyId == null) {
            throw new BusinessException("申请单不能为空");
        }
        if (type == ReportTypeEnum.INSPECTION) {
            BizInspectionApply apply = inspectionApplyMapper.selectById(applyId);
            if (apply == null) {
                throw new BusinessException("检查申请单不存在");
            }
            return new Anchor(applyId, apply.getApplyNo(), apply.getPatientId(), apply.getPatientName(),
                    apply.getInspectionItemName(), apply.getBodyPart());
        }
        BizLaboratoryApply apply = laboratoryApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("检验申请单不存在");
        }
        return new Anchor(applyId, apply.getApplyNo(), apply.getPatientId(), apply.getPatientName(),
                apply.getLaboratoryItemName(), apply.getSpecimenType());
    }

    private Long resolveApplyIdByRecord(Integer reportType, Long recordId) {
        if (ReportTypeEnum.INSPECTION.getCode().equals(reportType)) {
            BizInspectionRecord record = inspectionRecordMapper.selectById(recordId);
            return record == null ? null : record.getApplyId();
        }
        BizLaboratoryRecord record = laboratoryRecordMapper.selectById(recordId);
        return record == null ? null : record.getApplyId();
    }

    private BizExamImage newAnchorRow(Anchor anchor, Integer bizType, Long applyId, Integer modality) {
        BizExamImage row = new BizExamImage();
        row.setBizType(bizType);
        row.setApplyId(applyId);
        row.setApplyNo(cut(anchor.applyNo, 64));
        row.setPatientId(anchor.patientId);
        row.setPatientName(cut(anchor.patientName, 64));
        row.setItemName(cut(anchor.itemName, 200));
        row.setBodyPart(cut(anchor.bodyPart, 100));
        row.setModality(modality);
        return row;
    }

    /**
     * 本申请单内下一帧序号。
     *
     * <p>不建 uk(apply_id, seq)（sql/137 第三条），所以这里必须由服务端算准，
     * 否则两帧同为 seq=1，阅片器翻页会看到重复帧。
     */
    private int nextSeq(Integer bizType, Long applyId) {
        List<BizExamImage> exists = imageMapper.selectList(new LambdaQueryWrapper<BizExamImage>()
                .eq(BizExamImage::getBizType, bizType)
                .eq(BizExamImage::getApplyId, applyId)
                .orderByDesc(BizExamImage::getSeq)
                .last("LIMIT 1"));
        return exists.isEmpty() || exists.get(0).getSeq() == null ? 1 : exists.get(0).getSeq() + 1;
    }

    private String buildFileName(String ext) {
        return LocalDate.now().format(DAY) + "_" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
    }

    /**
     * 落盘并返回相对访问路径（uploads/examImage/yyyyMMdd/xxx.png）。
     *
     * <p>文件名一律服务端生成：用户传什么名字只作为展示用的 file_name 存着，
     * 参与拼路径就等于把 "../../application.yml" 交给请求方（AGENTS 的目录穿越口子）。
     */
    private String saveBytes(String fileName, byte[] bytes) {
        String dateDir = LocalDate.now().format(DAY);
        String relativeDir = UPLOAD_ROOT_REL + "/" + dateDir;
        File dir = new File(System.getProperty("user.dir"), relativeDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("影像存储目录创建失败：" + dir.getAbsolutePath());
        }
        Path target = new File(dir, fileName).toPath();
        try {
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new BusinessException("影像写入失败：" + e.getMessage());
        }
        return relativeDir + "/" + fileName;
    }

    private byte[] readQuietly(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("读取上传流失败：" + e.getMessage());
        }
    }

    /** 只允许删本模块自己目录下的文件，防止 file_url 被改成任意路径后借删除接口删服务器文件 */
    private void deleteFileQuietly(String fileUrl) {
        if (!StringUtils.hasText(fileUrl) || !fileUrl.startsWith(UPLOAD_ROOT_REL + "/")) {
            log.warn("[影像删除] file_url 不在影像目录内，跳过磁盘删除：{}", fileUrl);
            return;
        }
        try {
            File f = new File(System.getProperty("user.dir"), fileUrl);
            if (f.exists() && !f.delete()) {
                log.warn("[影像删除] 磁盘文件删除失败（行已删，孤儿文件待清理）：{}", f.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("[影像删除] 磁盘文件删除异常 fileUrl={} err={}", fileUrl, e.getMessage());
        }
    }

    private List<ExamImageVO> toVOList(List<BizExamImage> rows) {
        List<ExamImageVO> list = new ArrayList<>();
        for (BizExamImage row : rows) {
            list.add(toVO(row));
        }
        return list;
    }

    private ExamImageVO toVO(BizExamImage row) {
        ExamImageVO vo = new ExamImageVO();
        BeanUtils.copyProperties(row, vo);
        vo.setModalityText(row.getModality() == null ? null
                : subDictText.text("his_exam_device_type", row.getModality()));
        vo.setSourceText(ExamImageSourceEnum.textOf(row.getSource()));
        return vo;
    }

    private static String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}

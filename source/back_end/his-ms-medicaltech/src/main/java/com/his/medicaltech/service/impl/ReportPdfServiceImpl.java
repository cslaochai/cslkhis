package com.his.medicaltech.service.impl;

import com.his.common.config.UploadProperties;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.LabAbnormalFlagEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.utils.UserUtils;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.medicaltech.dto.ReportPdfFileVO;
import com.his.medicaltech.dto.ReportPdfVerifyVO;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.service.ReportPdfService;
import com.his.spi.service.SignCertFacadeService;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.DeptBriefVO;
import com.his.spi.vo.EmployeeBriefVO;
import com.his.spi.vo.SignCertBriefVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 报告签名 PDF 实现：openpdf 渲染（与引导单同栈，simsun 中文字体），
 * 文件级数字签名 = 对 PDF 字节 SHA-256 后用审核医师证书（SHA256withRSA）签名，签名元数据落报告记录表。
 *
 * <p>患者面口径：报告单落款同时展示报告 / 审核人员的签名图样与打印姓名（未预维护笔迹时仅打印姓名），
 * 不出现「电子签名 / 数字签名 / 验签」等后台证据链字样；CA 签名与文件级哈希属于后台法律效力留存，
 * 由签名记录与验签接口承载，不上患者面。
 *
 * <p>落款称谓按岗位口径区分：影像检查报告由执业医师出具，落款「报告医师 / 审核医师」；
 * 检验报告由检验技师完成、审核者复核，落款「检验者 / 审核者」。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportPdfServiceImpl implements ReportPdfService {

    private final SignCertFacadeService signCertFacadeService;
    private final SystemFacadeService systemFacadeService;
    private final UploadProperties uploadProperties;

    @Override
    public ReportPdfFileVO generateInspectionPdf(BizInspectionRecord record, String pin) {
        if (record == null || !TextUtil.hasText(record.getRecordNo())) {
            return null;
        }
        try {
            byte[] pdfBytes = buildInspectionPdf(record);
            return signAndStore(record.getRecordNo(), pdfBytes, pin);
        } catch (Exception e) {
            log.error("检查报告 PDF 生成失败 recordNo={}", record.getRecordNo(), e);
            return null;
        }
    }

    @Override
    public ReportPdfFileVO generateLaboratoryPdf(BizLaboratoryRecord record, List<BizLabResult> results, String pin) {
        if (record == null || !TextUtil.hasText(record.getRecordNo())) {
            return null;
        }
        try {
            byte[] pdfBytes = buildLaboratoryPdf(record, results == null ? List.of() : results);
            return signAndStore(record.getRecordNo(), pdfBytes, pin);
        } catch (Exception e) {
            log.error("检验报告 PDF 生成失败 recordNo={}", record.getRecordNo(), e);
            return null;
        }
    }

    @Override
    public ReportPdfVerifyVO verifyPdf(String relativePath, String expectedHash, String signValue, Long certId) {
        ReportPdfVerifyVO vo = new ReportPdfVerifyVO();
        if (!TextUtil.hasText(relativePath) || !TextUtil.hasText(expectedHash)
                || !TextUtil.hasText(signValue) || certId == null) {
            vo.setValid(false);
            vo.setMessage("缺少验签要素（文件/哈希/签名/证书）");
            return vo;
        }
        try {
            String absolutePath = uploadProperties.file(relativePath).getAbsolutePath();
            byte[] pdfBytes = Files.readAllBytes(Paths.get(absolutePath));
            String actualHash = sha256Hex(pdfBytes);
            SignCertBriefVO cert = signCertFacadeService.getCertById(certId);
            if (cert == null) {
                vo.setValid(false);
                vo.setMessage("签名证书不存在或已吊销，无法验签");
                return vo;
            }
            boolean hashOk = actualHash.equalsIgnoreCase(expectedHash.trim());
            boolean signOk = hashOk && signCertFacadeService.verifySignValue(
                    cert.getPublicKey(), "SHA-256:" + actualHash, signValue);
            if (signOk) {
                vo.setValid(true);
                vo.setMessage("验签通过：文件未被篡改，数字签名有效（" + signCertFacadeService.signAlgo() + "）");
            } else {
                vo.setValid(false);
                vo.setMessage("验签失败：文件内容与签名值不匹配（文件可能被修改）");
            }
            return vo;
        } catch (Exception e) {
            log.warn("报告 PDF 验签异常 relativePath={}", relativePath, e);
            vo.setValid(false);
            vo.setMessage("验签失败：" + e.getMessage());
            return vo;
        }
    }

    // ==================== PDF 渲染 ====================

    private byte[] buildInspectionPdf(BizInspectionRecord record) throws Exception {
        com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
        document.open();

        com.lowagie.text.Font titleFont = titleFont();
        com.lowagie.text.Font headerFont = headerFont();
        com.lowagie.text.Font normalFont = normalFont();

        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("医学影像检查报告", titleFont);
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(12);
        document.add(title);

        List<String[]> pairs = new ArrayList<>();
        pairs.add(new String[]{"患者姓名", nullToDash(record.getPatientName())});
        pairs.add(new String[]{"病历号", nullToDash(record.getRecordNo())});
        pairs.add(new String[]{"性别", nullToDash(SysGenderEnum.getLabel(record.getGender()))});
        pairs.add(new String[]{"年龄", record.getAge() != null ? record.getAge() + "岁" : "-"});
        pairs.add(new String[]{"检查项目", nullToDash(record.getInspectionItemName())});
        pairs.add(new String[]{"检查科室", deptName(record.getInspectionDeptId(), record.getInspectionDeptName())});
        pairs.add(new String[]{"检查部位", nullToDash(record.getBodyPart())});
        pairs.add(new String[]{"检查时间", fmtTime(record.getExecuteTime())});
        addInfoGrid(document, pairs, normalFont);

        document.add(paragraph(" ", normalFont));
        document.add(paragraph("【检查所见】", headerFont));
        document.add(paragraph(nullToDash(record.getResultDescription()), normalFont));
        document.add(paragraph(" ", normalFont));

        document.add(paragraph("【影像诊断】", headerFont));
        document.add(paragraph(nullToDash(record.getResultConclusion()), normalFont));
        document.add(paragraph(" ", normalFont));

        String reporterName = TextUtil.hasText(record.getReportBy()) ? record.getReportBy() : record.getExecuteBy();
        addSignArea(document, normalFont,
                "报告医师（签名）", reporterName, signImageByName(reporterName),
                "审核医师（签名）", record.getAuditBy(), auditorSignImage(record.getAuditBy()));

        document.close();
        return baos.toByteArray();
    }

    private byte[] buildLaboratoryPdf(BizLaboratoryRecord record, List<BizLabResult> results) throws Exception {
        com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
        document.open();

        com.lowagie.text.Font titleFont = titleFont();
        com.lowagie.text.Font headerFont = headerFont();
        com.lowagie.text.Font normalFont = normalFont();

        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("检 验 报 告", titleFont);
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(12);
        document.add(title);

        List<String[]> pairs = new ArrayList<>();
        pairs.add(new String[]{"患者姓名", nullToDash(record.getPatientName())});
        pairs.add(new String[]{"病历号", nullToDash(record.getRecordNo())});
        pairs.add(new String[]{"性别", nullToDash(SysGenderEnum.getLabel(record.getGender()))});
        pairs.add(new String[]{"年龄", record.getAge() != null ? record.getAge() + "岁" : "-"});
        pairs.add(new String[]{"检验项目", nullToDash(record.getLaboratoryItemName())});
        pairs.add(new String[]{"检验科室", deptName(record.getLaboratoryDeptId(), record.getLaboratoryDeptName())});
        pairs.add(new String[]{"标本", nullToDash(record.getSpecimenType())});
        pairs.add(new String[]{"检验时间", fmtTime(record.getExecuteTime())});
        addInfoGrid(document, pairs, normalFont);

        document.add(paragraph(" ", normalFont));
        document.add(paragraph("【检验结果】", headerFont));
        if (results.isEmpty()) {
            document.add(paragraph("（无检验结果明细）", normalFont));
        } else {
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.4f, 1.8f, 1.6f, 2.8f, 1.2f});
            addCell(table, "项目名称", headerFont);
            addCell(table, "结果", headerFont);
            addCell(table, "单位", headerFont);
            addCell(table, "参考区间", headerFont);
            addCell(table, "标志", headerFont);
            for (BizLabResult r : results) {
                addCell(table, nullToDash(r.getLaboratoryItemName()), normalFont);
                addCell(table, nullToDash(r.getResultValue()), normalFont);
                addCell(table, nullToDash(r.getResultUnit()), normalFont);
                addCell(table, nullToDash(r.getReferenceRange()), normalFont);
                addCell(table, abnormalLabel(r.getAbnormalFlag()), normalFont);
            }
            document.add(table);
        }
        document.add(paragraph(" ", normalFont));

        document.add(paragraph("【诊断意见】", headerFont));
        document.add(paragraph(nullToDash(record.getDiagnosis()), normalFont));
        document.add(paragraph(" ", normalFont));

        String reporterName = TextUtil.hasText(record.getReportBy()) ? record.getReportBy() : record.getExecuteBy();
        addSignArea(document, normalFont,
                "检验者（签名）", reporterName, signImageByName(reporterName),
                "审核者（签名）", record.getAuditBy(), auditorSignImage(record.getAuditBy()));

        document.close();
        return baos.toByteArray();
    }

    /**
     * 基本信息网格：一行三对「标签 + 值」，无边框，紧凑排版。
     */
    private void addInfoGrid(com.lowagie.text.Document document, List<String[]> pairs,
                             com.lowagie.text.Font normalFont) throws Exception {
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 2.2f, 1.2f, 2.2f, 1.2f, 2.2f});
        int size = pairs.size();
        int rows = (size + 2) / 3;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 3; col++) {
                int idx = row * 3 + col;
                String label = idx < size ? pairs.get(idx)[0] : "";
                String value = idx < size ? pairs.get(idx)[1] : "";
                addGridCell(table, TextUtil.hasText(label) ? label + "：" : "", normalFont);
                addGridCell(table, value, normalFont);
            }
        }
        document.add(table);
    }

    /**
     * 落款签名区：左右两列，称谓按报告类型传入（影像：报告医师/审核医师；检验：检验者/审核者）。
     * 同一行左右排布：打印姓名在前，签名图样紧跟其后；未预维护笔迹时仅打印姓名。
     */
    private void addSignArea(com.lowagie.text.Document document, com.lowagie.text.Font normalFont,
                             String reporterLabel, String reporterName, String reporterImage,
                             String auditorLabel, String auditorName, String auditorImage) throws Exception {
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{5.0f, 5.0f});

        com.lowagie.text.pdf.PdfPCell reporterCell = new com.lowagie.text.pdf.PdfPCell();
        fillSignCell(reporterCell, reporterLabel, reporterName, reporterImage, normalFont);
        com.lowagie.text.pdf.PdfPCell auditorCell = new com.lowagie.text.pdf.PdfPCell();
        fillSignCell(auditorCell, auditorLabel, auditorName, auditorImage, normalFont);
        table.addCell(reporterCell);
        table.addCell(auditorCell);
        document.add(table);
    }

    private void fillSignCell(com.lowagie.text.pdf.PdfPCell cell, String label, String name,
                              String image, com.lowagie.text.Font normalFont) throws Exception {
        cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        cell.setPadding(6);
        com.lowagie.text.Paragraph p = new com.lowagie.text.Paragraph();
        p.add(new com.lowagie.text.Phrase(label + "：", normalFont));
        p.add(new com.lowagie.text.Phrase(nullToDash(name), nameFont()));
        if (TextUtil.hasText(image)) {
            try {
                com.lowagie.text.Image signImg = imageOf(image, 44, 13);
                p.add(new com.lowagie.text.Chunk(signImg, 6, -2));
            } catch (Exception e) {
                log.warn("医师笔迹渲染失败，仅保留姓名落款", e);
            }
        }
        cell.addElement(p);
    }

    /**
     * 审核医师笔迹：PDF 在审核通过时刻生成，取当前登录人（即审核医师）员工档案笔迹；
     * 取不到登录态时退回按姓名回捞员工档案。
     */
    private String auditorSignImage(String auditByName) {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user != null && user.getEmployeeId() != null) {
                String image = systemFacadeService.getEmployeeSignImage(user.getEmployeeId());
                if (TextUtil.hasText(image)) {
                    return image;
                }
            }
        } catch (Exception e) {
            log.warn("取当前登录医师档案笔迹失败，退回按姓名回捞", e);
        }
        return signImageByName(auditByName);
    }

    /**
     * 按姓名回捞员工档案手写笔迹（报告医师在结果录入时签署，PDF 生成时刻其登录态已不在）。
     */
    private String signImageByName(String empName) {
        if (!TextUtil.hasText(empName)) {
            return null;
        }
        try {
            List<EmployeeBriefVO> employees = systemFacadeService.listEmployeesByName(empName.trim());
            if (employees == null || employees.isEmpty()) {
                return null;
            }
            return systemFacadeService.getEmployeeSignImage(employees.get(0).getId());
        } catch (Exception e) {
            log.warn("按姓名 {} 取档案笔迹失败", empName, e);
            return null;
        }
    }

    /**
     * 科室名快照为空时按科室 ID 回查主数据兜底（历史申请单只落了科室 ID 的情形）。
     */
    private String deptName(Long deptId, String snapshotName) {
        if (TextUtil.hasText(snapshotName)) {
            return snapshotName;
        }
        if (deptId == null) {
            return "-";
        }
        try {
            DeptBriefVO dept = systemFacadeService.findDepartment(deptId);
            return dept != null && TextUtil.hasText(dept.getDeptName()) ? dept.getDeptName() : "-";
        } catch (Exception e) {
            log.warn("按科室 ID {} 回查科室名失败", deptId, e);
            return "-";
        }
    }

    // ==================== 文件签名与落盘 ====================

    /**
     * 对 PDF 字节做 SHA-256，用审核医师（当前登录人）证书签名，落盘 uploads/reportPdf/yyyyMMdd/{recordNo}.pdf。
     */
    private ReportPdfFileVO signAndStore(String recordNo, byte[] pdfBytes, String pin) throws Exception {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getEmployeeId() == null) {
            throw new com.his.common.exception.BusinessException("报告PDF签名失败：取不到当前登录用户，无法确定签名人");
        }
        SignCertBriefVO cert = signCertFacadeService.ensureActiveCert(
                user.getEmployeeId(), user.getRealName(), user.getDeptId(), user.getDeptName());

        String hash = sha256Hex(pdfBytes);
        // 软签：PIN 校验与私钥使用都在签名域内完成，这里只拿签名值
        String signValue = signCertFacadeService.signWithCert(cert.getId(), "SHA-256:" + hash, pin);

        String dateDir = LocalDate.now().format(com.his.common.util.DateFormats.COMPACT_DATE);
        String fileName = recordNo + ".pdf";
        String relativePath = "uploads/reportPdf/" + dateDir + "/" + fileName;
        File dir = uploadProperties.mkdirsOf("uploads/reportPdf/" + dateDir);
        Files.write(new File(dir, fileName).toPath(), pdfBytes);

        signCertFacadeService.markCertUsed(cert.getId(), LocalDateTime.now());

        ReportPdfFileVO vo = new ReportPdfFileVO();
        vo.setRelativePath(relativePath);
        vo.setFileHash(hash);
        vo.setSignValue(signValue);
        vo.setCertId(cert.getId());
        vo.setSignedTime(LocalDateTime.now());
        return vo;
    }

    private String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest(bytes);
        StringBuilder sb = new StringBuilder(hashed.length * 2);
        for (byte b : hashed) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    // ==================== openpdf 工具 ====================

    private com.lowagie.text.Font titleFont() throws Exception {
        return new com.lowagie.text.Font(baseFont(), 16, com.lowagie.text.Font.BOLD);
    }

    private com.lowagie.text.Font headerFont() throws Exception {
        return new com.lowagie.text.Font(baseFont(), 10.5f, com.lowagie.text.Font.BOLD);
    }

    private com.lowagie.text.Font normalFont() throws Exception {
        return new com.lowagie.text.Font(baseFont(), 9, com.lowagie.text.Font.NORMAL);
    }

    private com.lowagie.text.Font nameFont() throws Exception {
        return new com.lowagie.text.Font(baseFont(), 8, com.lowagie.text.Font.NORMAL);
    }

    private com.lowagie.text.pdf.BaseFont baseFont() throws Exception {
        return com.lowagie.text.pdf.BaseFont.createFont(
                "C:/Windows/Fonts/simsun.ttc,0", "Identity-H", com.lowagie.text.pdf.BaseFont.EMBEDDED);
    }

    private com.lowagie.text.Paragraph paragraph(String text, com.lowagie.text.Font font) {
        com.lowagie.text.Paragraph p = new com.lowagie.text.Paragraph(
                text == null ? "" : text, font);
        p.setSpacingAfter(4);
        return p;
    }

    private void addCell(com.lowagie.text.pdf.PdfPTable table, String text, com.lowagie.text.Font font) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(
                text == null ? "" : text, font));
        cell.setPadding(3);
        table.addCell(cell);
    }

    private void addGridCell(com.lowagie.text.pdf.PdfPTable table, String text,
                             com.lowagie.text.Font font) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(
                text == null ? "" : text, font));
        cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        cell.setPadding(3);
        table.addCell(cell);
    }

    /**
     * 把 base64 图片（dataURL 或纯 base64）转成 openpdf Image。
     */
    private com.lowagie.text.Image imageOf(String dataUrl, int width, int height) throws Exception {
        String base64 = dataUrl;
        int idx = dataUrl.indexOf("base64,");
        if (idx >= 0) {
            base64 = dataUrl.substring(idx + "base64,".length());
        }
        byte[] bytes = java.util.Base64.getDecoder().decode(base64);
        com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(bytes);
        image.scaleToFit(width, height);
        return image;
    }

    private String nullToDash(String value) {
        return TextUtil.hasText(value) ? value : "-";
    }

    private String fmtTime(LocalDateTime time) {
        return time != null ? time.format(DateFormats.DATETIME) : "-";
    }

    private String abnormalLabel(Integer flag) {
        if (flag == null) {
            return "-";
        }
        String label = LabAbnormalFlagEnum.getLabel(flag);
        return TextUtil.hasText(label) ? label : "-";
    }
}

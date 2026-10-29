package com.his.medicaltech.service.impl;

import com.his.common.entity.CurrentUser;
import com.his.common.enums.LabAbnormalFlagEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.utils.UserUtils;
import com.his.common.util.TextUtil;
import com.his.medicaltech.dto.ReportPdfFileVO;
import com.his.medicaltech.dto.ReportPdfVerifyVO;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.service.ReportPdfService;
import com.his.spi.service.SignCertFacadeService;
import com.his.spi.service.SystemFacadeService;
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
import java.util.List;

/**
 * 报告签名 PDF 实现：openpdf 渲染（与引导单同栈，simsun 中文字体），
 * 文件级数字签名 = 对 PDF 字节 SHA-256 后用审核医师证书（SHA256withRSA）签名，签名元数据落报告记录表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportPdfServiceImpl implements ReportPdfService {

    private final SignCertFacadeService signCertFacadeService;
    private final SystemFacadeService systemFacadeService;

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
            String absolutePath = System.getProperty("user.dir") + "/" + relativePath;
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
        com.lowagie.text.Font smallFont = smallFont();

        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("医学影像检查报告", titleFont);
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(16);
        document.add(title);

        document.add(paragraph("患者姓名：" + nullToDash(record.getPatientName()), normalFont));
        document.add(paragraph("病历号：" + nullToDash(record.getRecordNo()), normalFont));
        document.add(paragraph("性别：" + SysGenderEnum.getLabel(record.getGender()), normalFont));
        document.add(paragraph("年龄：" + (record.getAge() != null ? record.getAge() + "岁" : "-"), normalFont));
        document.add(paragraph("检查项目：" + nullToDash(record.getInspectionItemName())
                + (TextUtil.hasText(record.getBodyPart()) ? "（部位：" + record.getBodyPart() + "）" : ""), normalFont));
        document.add(paragraph("检查科室：" + nullToDash(record.getInspectionDeptName()), normalFont));
        document.add(paragraph("检查时间：" + (record.getExecuteTime() != null ? record.getExecuteTime() : "-"), normalFont));
        document.add(paragraph(" ", normalFont));

        document.add(paragraph("【检查所见】", headerFont));
        document.add(paragraph(nullToDash(record.getResultDescription()), normalFont));
        document.add(paragraph(" ", normalFont));

        document.add(paragraph("【影像诊断】", headerFont));
        document.add(paragraph(nullToDash(record.getResultConclusion()), normalFont));
        document.add(paragraph(" ", normalFont));

        addSignArea(document, headerFont, normalFont, smallFont,
                "报告医师：" + nullToDash(record.getReportBy()),
                record.getReportSignedTime() != null ? "（电子签名 · " + record.getReportSignedTime() + "）" : "",
                null,
                "审核医师：" + nullToDash(record.getAuditBy()),
                record.getAuditSignedTime() != null ? "（电子签名 · " + record.getAuditSignedTime() + "）" : "",
                currentAuditSignImage());

        document.add(paragraph(" ", normalFont));
        document.add(paragraph("本报告已加盖审核医师数字签名（文件级），可扫码验签。", smallFont));

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
        com.lowagie.text.Font smallFont = smallFont();

        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("检 验 报 告", titleFont);
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(16);
        document.add(title);

        document.add(paragraph("患者姓名：" + nullToDash(record.getPatientName()), normalFont));
        document.add(paragraph("病历号：" + nullToDash(record.getRecordNo()), normalFont));
        document.add(paragraph("性别：" + SysGenderEnum.getLabel(record.getGender()), normalFont));
        document.add(paragraph("年龄：" + (record.getAge() != null ? record.getAge() + "岁" : "-"), normalFont));
        document.add(paragraph("检验项目：" + nullToDash(record.getLaboratoryItemName()), normalFont));
        document.add(paragraph("检验科室：" + nullToDash(record.getLaboratoryDeptName()), normalFont));
        document.add(paragraph("标本：" + nullToDash(record.getSpecimenType()), normalFont));
        document.add(paragraph("检验时间：" + (record.getExecuteTime() != null ? record.getExecuteTime() : "-"), normalFont));
        document.add(paragraph(" ", normalFont));

        document.add(paragraph("【检验结果】", headerFont));
        if (results.isEmpty()) {
            document.add(paragraph("（无检验结果明细）", normalFont));
        } else {
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.8f, 1.6f, 1.2f, 2.2f, 1.0f, 2.0f});
            addCell(table, "项目名称", headerFont);
            addCell(table, "结果", headerFont);
            addCell(table, "单位", headerFont);
            addCell(table, "参考区间", headerFont);
            addCell(table, "标志", headerFont);
            addCell(table, "备注", headerFont);
            for (BizLabResult r : results) {
                addCell(table, nullToDash(r.getLaboratoryItemName()), normalFont);
                addCell(table, nullToDash(r.getResultValue()), normalFont);
                addCell(table, nullToDash(r.getResultUnit()), normalFont);
                addCell(table, nullToDash(r.getReferenceRange()), normalFont);
                addCell(table, abnormalLabel(r.getAbnormalFlag()), normalFont);
                addCell(table, nullToDash(r.getAbnormalDesc()), normalFont);
            }
            document.add(table);
        }
        document.add(paragraph(" ", normalFont));

        document.add(paragraph("【诊断意见】", headerFont));
        document.add(paragraph(nullToDash(record.getDiagnosis()), normalFont));
        document.add(paragraph(" ", normalFont));

        addSignArea(document, headerFont, normalFont, smallFont,
                "报告医师：" + nullToDash(record.getReportBy()),
                record.getReportSignedTime() != null ? "（电子签名 · " + record.getReportSignedTime() + "）" : "",
                null,
                "审核医师：" + nullToDash(record.getAuditBy()),
                record.getAuditSignedTime() != null ? "（电子签名 · " + record.getAuditSignedTime() + "）" : "",
                currentAuditSignImage());

        document.add(paragraph(" ", normalFont));
        document.add(paragraph("本报告已加盖审核医师数字签名（文件级），可扫码验签。", smallFont));

        document.close();
        return baos.toByteArray();
    }

    /**
     * 签名区：报告医师（姓名 + 电子签名时刻）+ 审核医师（姓名 + 电子签名时刻 + 手写笔迹）。
     */
    private void addSignArea(com.lowagie.text.Document document, com.lowagie.text.Font headerFont,
                             com.lowagie.text.Font normalFont, com.lowagie.text.Font smallFont,
                             String reporterLine, String reporterSignSuffix, String reporterImage,
                             String auditorLine, String auditorSignSuffix, String auditorImage) {
        document.add(paragraph("【医师签名】", headerFont));
        document.add(paragraph(reporterLine + reporterSignSuffix, normalFont));
        document.add(paragraph(auditorLine + auditorSignSuffix, normalFont));
        if (TextUtil.hasText(auditorImage)) {
            try {
                com.lowagie.text.Image signImg = imageOf(auditorImage, 160, 60);
                if (signImg != null) {
                    signImg.setSpacingBefore(6);
                    document.add(signImg);
                }
            } catch (Exception e) {
                log.warn("审核医师笔迹渲染失败，仅保留文字签名", e);
            }
        }
    }

    /**
     * 审核医师手写笔迹：引用登录人（生成时刻的审核人）员工档案预维护笔迹。
     */
    private String currentAuditSignImage() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user == null || user.getEmployeeId() == null) {
                return null;
            }
            return systemFacadeService.getEmployeeSignImage(user.getEmployeeId());
        } catch (Exception e) {
            log.warn("取审核医师档案笔迹失败，PDF 仅保留文字签名", e);
            return null;
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
        String relativePath = "uploads/reportPdf/" + dateDir + "/" + recordNo + ".pdf";
        String uploadBaseDir = System.getProperty("user.dir");
        File dir = new File(uploadBaseDir + "/uploads/reportPdf/" + dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Files.write(Paths.get(uploadBaseDir + "/" + relativePath), pdfBytes);

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
        return new com.lowagie.text.Font(baseFont(), 18, com.lowagie.text.Font.BOLD);
    }

    private com.lowagie.text.Font headerFont() throws Exception {
        return new com.lowagie.text.Font(baseFont(), 12, com.lowagie.text.Font.BOLD);
    }

    private com.lowagie.text.Font normalFont() throws Exception {
        return new com.lowagie.text.Font(baseFont(), 10, com.lowagie.text.Font.NORMAL);
    }

    private com.lowagie.text.Font smallFont() throws Exception {
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
        cell.setPadding(4);
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
        image.scaleAbsolute(width, height);
        return image;
    }

    private String nullToDash(String value) {
        return TextUtil.hasText(value) ? value : "-";
    }

    private String abnormalLabel(Integer flag) {
        if (flag == null) {
            return "-";
        }
        String label = LabAbnormalFlagEnum.getLabel(flag);
        return TextUtil.hasText(label) ? label : "-";
    }
}

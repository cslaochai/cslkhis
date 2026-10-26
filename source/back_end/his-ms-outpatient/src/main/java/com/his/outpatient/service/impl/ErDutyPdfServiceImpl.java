package com.his.outpatient.service.impl;

import com.his.common.util.TextUtil;
import com.his.outpatient.service.StaffScheduleService;
import com.his.outpatient.service.ErDutyPdfService;
import com.his.outpatient.vo.ErDutyPdfVO;
import com.his.outpatient.vo.StaffScheduleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 急诊值班表 PDF：openpdf 渲染（与报告单同栈，simsun 中文字体），
 * 按日期落盘 uploads/erDutyPdf/yyyyMMdd/erDuty_{date}.pdf，浏览器可经 /api/uploads/** 直链打开/打印。
 * 已存在则复用（用户要求：有就直接打印，没有才生成再打印），不重复落盘。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErDutyPdfServiceImpl implements ErDutyPdfService {

    private final StaffScheduleService staffScheduleService;

    private static final DateTimeFormatter COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public ErDutyPdfVO generateOrGet(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        String dateDir = date.format(COMPACT);
        String fileName = "erDuty_" + date + ".pdf";
        String relativePath = "uploads/erDutyPdf/" + dateDir + "/" + fileName;
        String uploadBaseDir = System.getProperty("user.dir");
        java.nio.file.Path file = Paths.get(uploadBaseDir + "/" + relativePath);

        boolean existed = Files.exists(file);
        if (!existed) {
            try {
                List<StaffScheduleVO> rows = staffScheduleService.erDutyPublic(date);
                byte[] pdf = buildPdf(date, rows == null ? List.of() : rows);
                File dir = file.getParent().toFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                Files.write(file, pdf);
            } catch (Exception e) {
                log.error("急诊值班表 PDF 生成失败 date={}", date, e);
                throw new com.his.common.exception.BusinessException("急诊值班表 PDF 生成失败：" + e.getMessage());
            }
        }

        ErDutyPdfVO vo = new ErDutyPdfVO();
        vo.setDate(date.toString());
        vo.setRelativePath(relativePath);
        vo.setUrl("/api/" + relativePath);
        vo.setExisted(existed);
        return vo;
    }

    // ==================== openpdf 渲染 ====================

    private byte[] buildPdf(LocalDate date, List<StaffScheduleVO> rows) throws Exception {
        com.lowagie.text.Document document = new com.lowagie.text.Document(
                com.lowagie.text.PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
        document.open();

        com.lowagie.text.Font titleFont = titleFont();
        com.lowagie.text.Font headerFont = headerFont();
        com.lowagie.text.Font normalFont = normalFont();
        com.lowagie.text.Font smallFont = smallFont();

        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("急诊值班表", titleFont);
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(6);
        document.add(title);

        com.lowagie.text.Paragraph sub = new com.lowagie.text.Paragraph(
                "急诊内科 / 外科 / 儿科 · 已发布医生值班（全院公开）    值班日期：" + date, smallFont);
        sub.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        sub.setSpacingAfter(16);
        document.add(sub);

        if (rows.isEmpty()) {
            document.add(paragraph("当日暂无已发布的急诊值班排班。", normalFont));
            document.close();
            return baos.toByteArray();
        }

        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.4f, 2.0f, 2.6f, 2.0f, 1.6f});
        addCell(table, "科室", headerFont);
        addCell(table, "班次", headerFont);
        addCell(table, "起止时间", headerFont);
        addCell(table, "值班医生", headerFont);
        addCell(table, "响应形态", headerFont);
        for (StaffScheduleVO r : rows) {
            addCell(table, dash(r.getDeptName()) + dash(r.getOrgName()), normalFont);
            addCell(table, dash(r.getShiftName()), normalFont);
            addCell(table, (r.getStartTime() == null ? "-" : r.getStartTime())
                    + " – " + (r.getEndTime() == null ? "-" : r.getEndTime()), normalFont);
            addCell(table, dash(r.getEmployeeName()), normalFont);
            addCell(table, dash(r.getAttendModeText()), normalFont);
        }
        document.add(table);

        document.add(paragraph(" ", normalFont));
        document.add(paragraph(
                "本表为只读公开视图，排班/审核请在「岗位出勤」与「排班审批中心」操作。", smallFont));

        document.close();
        return baos.toByteArray();
    }

    private com.lowagie.text.Font titleFont() throws Exception {
        return new com.lowagie.text.Font(baseFont(), 18, com.lowagie.text.Font.BOLD);
    }

    private com.lowagie.text.Font headerFont() throws Exception {
        return new com.lowagie.text.Font(baseFont(), 11, com.lowagie.text.Font.BOLD);
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
        com.lowagie.text.Paragraph p = new com.lowagie.text.Paragraph(text == null ? "" : text, font);
        p.setSpacingAfter(4);
        return p;
    }

    private void addCell(com.lowagie.text.pdf.PdfPTable table, String text, com.lowagie.text.Font font) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                new com.lowagie.text.Phrase(text == null ? "" : text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String dash(String value) {
        return TextUtil.hasText(value) ? value : "-";
    }
}

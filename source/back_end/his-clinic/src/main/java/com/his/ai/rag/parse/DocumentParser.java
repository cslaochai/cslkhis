package com.his.ai.rag.parse;

import com.his.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 知识库文件解析：按扩展名提取纯文本（PDF / Word docx / txt / md）。
 * 扫描件 PDF（图片型）提取不到文本，直接抛业务异常，避免把空文档灌进知识库。
 */
@Slf4j
@Component
public class DocumentParser {

    /** 提取文本低于该长度视为解析无效（扫描件 / 空白文档），拒绝入库 */
    private static final int MIN_TEXT_LENGTH = 20;

    /**
     * 解析上传文件为纯文本。
     *
     * @param filename 原始文件名（决定解析方式与默认标题）
     * @param bytes    文件字节
     * @return 提取后的纯文本（已 trim）
     */
    public String parse(String filename, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new BusinessException("文件内容为空");
        }
        String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        String text;
        if (lower.endsWith(".pdf")) {
            text = parsePdf(bytes);
        } else if (lower.endsWith(".docx")) {
            text = parseDocx(bytes);
        } else if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".markdown")) {
            text = new String(bytes, StandardCharsets.UTF_8);
        } else {
            throw new BusinessException("不支持的文件类型，仅支持 pdf / docx / txt / md");
        }
        if (text == null || text.trim().length() < MIN_TEXT_LENGTH) {
            throw new BusinessException("未能从文件中提取到有效文本（可能是扫描件 PDF 或空白文档）");
        }
        return text.trim();
    }

    private String parsePdf(byte[] bytes) {
        try (PDDocument document = PDDocument.load(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException ex) {
            throw new BusinessException("PDF 解析失败：" + ex.getMessage());
        }
    }

    private String parseDocx(byte[] bytes) {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : document.getParagraphs()) {
                String t = p.getText();
                if (t != null && !t.isEmpty()) {
                    sb.append(t).append('\n');
                }
            }
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String t = cell.getText();
                        if (t != null && !t.isEmpty()) {
                            sb.append(t).append(' ');
                        }
                    }
                    sb.append('\n');
                }
            }
            return sb.toString();
        } catch (IOException ex) {
            throw new BusinessException("Word 文档解析失败：" + ex.getMessage());
        }
    }
}

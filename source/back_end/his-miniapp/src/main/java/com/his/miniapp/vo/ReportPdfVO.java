package com.his.miniapp.vo;

import lombok.Data;

/**
 * 报告 PDF 出参。
 */
@Data
public class ReportPdfVO {

    private Integer denyStatus;

    private String fileName;

    private byte[] content;

    public static ReportPdfVO deny(int status) {
        ReportPdfVO vo = new ReportPdfVO();
        vo.setDenyStatus(status);
        return vo;
    }

    public static ReportPdfVO of(String fileName, byte[] content) {
        ReportPdfVO vo = new ReportPdfVO();
        vo.setFileName(fileName);
        vo.setContent(content);
        return vo;
    }
}

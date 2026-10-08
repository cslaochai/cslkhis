package com.his.miniapp.vo;

import lombok.Data;

/**
 * 报告 PDF 出参。
 */
@Data
public class MiniReportPdfVO {

    private Integer denyStatus;

    private String fileName;

    private byte[] content;

    public static MiniReportPdfVO deny(int status) {
        MiniReportPdfVO vo = new MiniReportPdfVO();
        vo.setDenyStatus(status);
        return vo;
    }

    public static MiniReportPdfVO of(String fileName, byte[] content) {
        MiniReportPdfVO vo = new MiniReportPdfVO();
        vo.setFileName(fileName);
        vo.setContent(content);
        return vo;
    }
}

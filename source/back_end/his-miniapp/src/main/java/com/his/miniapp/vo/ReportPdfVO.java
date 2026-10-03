package com.his.miniapp.vo;

import lombok.Data;

/**
 * 报告 PDF 出参。
 *
 * <p>denyStatus 非空表示这次取不到文件（越权 / 尚未发布），值就是应当回给前端的 HTTP 状态：
 * 小程序按 downloadFile 的 statusCode 判成败，拒绝一律兜成 200 会让它把「无权限」当成下载成功。
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

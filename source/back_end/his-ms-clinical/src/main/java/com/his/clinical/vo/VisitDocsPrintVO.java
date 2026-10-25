package com.his.clinical.vo;

import lombok.Data;

/**
 * 医生点「打印就诊文书」时返回的三份 PDF 相对路径。
 *
 * <p>结诊时不自动生成 PDF，医生在医生站点按钮时才现场画三份：
 * <ul>
 *   <li>引导单（含缴费二维码 + 费用 + 检查指引）；</li>
 *   <li>普通处方签（西药+中成药）；</li>
 *   <li>中药饮片处方签。</li>
 * </ul>
 * 对应项没有内容（如无处方、无费用）时为空串，前端不打开对应打印窗口。
 */
@Data
public class VisitDocsPrintVO {

    /** 就诊引导单 PDF（uploads/guide/yyyyMMdd/{recordNo}.pdf） */
    private String guidePdfPath;

    /** 普通处方签（西药+中成药）PDF */
    private String rxNormalPdfPath;

    /** 中药饮片处方签 PDF */
    private String rxHerbalPdfPath;
}

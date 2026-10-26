package com.his.outpatient.service;

import com.his.outpatient.vo.ErDutyPdfVO;

import java.time.LocalDate;

/**
 * 急诊值班表 PDF：按日期生成并落盘，已存在则复用。
 */
public interface ErDutyPdfService {

    /**
     * 取某日急诊值班表 PDF 的访问信息。
     * 文件已存在则直接返回（existed=true），否则先渲染落盘再返回。
     *
     * @param date 值班日期，null 视为今天
     * @return 相对路径 / 可直链 URL / 是否复用已有文件
     */
    ErDutyPdfVO generateOrGet(LocalDate date);
}

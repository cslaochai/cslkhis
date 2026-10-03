package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.ExamFilmQueryPageDTO;
import com.his.medicaltech.dto.ExamFilmSpecUpsertDTO;
import com.his.medicaltech.dto.ExamFilmUpsertDTO;
import com.his.medicaltech.vo.ExamFilmVO;
import com.his.medicaltech.vo.FilmSpecSelectListVO;

import java.util.List;

/**
 * 检查胶片量方与发放（sql/138）。
 *
 * <p>状态机（检查胶片用量.film_status）：
 * <pre>
 *   1 已登记 —— 技师录了「这次打了 3 张 14×17」，还没收钱也没给患者
 *   2 已打印 —— 打印机出片了
 *   3 已发放 —— 交到患者/病区手上，这一刻胶片才算交付完成
 *   4 已作废 —— 登记错了；**已记账的不能作废**，那笔钱要走收费红冲
 * </pre>
 */
public interface ExamFilmService {

    /** 分页列表 */
    PageResult<ExamFilmVO> listPage(ExamFilmQueryPageDTO query);

    /** 按检查记录取该次检查的全部胶片 */
    List<ExamFilmVO> listByRecordId(Long recordId);

    /** 某段时间内的汇总（张数 / 金额 / 已记账金额） */
    ExamFilmVO.FilmStats stats(String startDate, String endDate);

    /** 登记用量（新增/改张数）：金额服务端现算 */
    ExamFilmVO upsert(ExamFilmUpsertDTO dto);

    /** 记账：调收费模块生成待结算记账行（模块缺席则降级为不记账并明确提示） */
    ExamFilmVO charge(Long filmId);

    /** 标记已打印 */
    ExamFilmVO markPrinted(Long filmId);

    /** 标记已发放（交给患者/病区） */
    ExamFilmVO deliver(Long filmId);

    /** 作废：只能作废「未记账」的胶片行 */
    boolean deleteById(Long filmId, String reason);

    // 规格价目

    List<FilmSpecSelectListVO> specSelectList();

    FilmSpecSelectListVO upsertSpec(ExamFilmSpecUpsertDTO dto);

    boolean deleteSpec(Long id);
}

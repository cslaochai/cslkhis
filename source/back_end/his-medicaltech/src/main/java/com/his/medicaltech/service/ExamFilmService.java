package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.ExamFilmPageQueryDTO;
import com.his.medicaltech.dto.ExamFilmSpecUpsertDTO;
import com.his.medicaltech.dto.ExamFilmUpsertDTO;
import com.his.medicaltech.vo.ExamFilmVO;
import com.his.medicaltech.vo.FilmSpecSelectListVO;

import java.util.List;

/**
 * 检查胶片量方与发放（sql/138）。
 */
public interface ExamFilmService {

    /**
     * 分页列表
     */
    PageResult<ExamFilmVO> listPage(ExamFilmPageQueryDTO query);

    /**
     * 按检查记录取该次检查的全部胶片
     */
    List<ExamFilmVO> listByRecordId(Long recordId);

    /**
     * 某段时间内的汇总（张数 / 金额 / 已记账金额）
     */
    ExamFilmVO.FilmStats stats(String startDate, String endDate);

    /**
     * 登记用量（新增/改张数）：金额服务端现算
     */
    ExamFilmVO upsert(ExamFilmUpsertDTO dto);

    /**
     * 记账：调收费模块生成待结算记账行（模块缺席则降级为不记账并明确提示）
     */
    ExamFilmVO charge(Long filmId);

    /**
     * 标记已打印
     */
    ExamFilmVO markPrinted(Long filmId);

    /**
     * 标记已发放（交给患者/病区）
     */
    ExamFilmVO deliver(Long filmId);

    /**
     * 作废：只能作废「未记账」的胶片行
     */
    boolean deleteById(Long filmId, String reason);

    // 规格价目

    List<FilmSpecSelectListVO> specSelectList();

    /**
     * 新增或修改胶片规格价目，单价是登记用量时算金额的依据。
     * 用于胶片量方与发放页的规格价目弹框保存。
     */
    FilmSpecSelectListVO upsertSpec(ExamFilmSpecUpsertDTO dto);

    /**
     * 删除胶片规格；已被用量行引用过的规格不许删，只能停用，返回 false 表示规格不存在或已删。
     * 用于规格价目弹框清理错录的规格。
     */
    boolean deleteSpec(Long id);
}

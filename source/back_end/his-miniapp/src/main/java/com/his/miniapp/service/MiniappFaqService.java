package com.his.miniapp.service;

import com.his.common.base.PageResult;
import com.his.miniapp.dto.FaqPageQueryDTO;
import com.his.miniapp.dto.FaqUpsertDTO;
import com.his.miniapp.vo.FaqCategoryListVO;
import com.his.miniapp.vo.FaqListVO;

import java.util.List;

/**
 * 患者端常见问题。
 */
public interface MiniappFaqService {

    /** 分类（带条数） */
    List<FaqCategoryListVO> categories();

    /** 检索（关键词切词匹配 + 分类过滤，分页） */
    PageResult<FaqListVO> search(FaqPageQueryDTO dto);

    /** 单条详情（并累计查看次数） */
    FaqListVO getById(Long faqId);

    /** 热门问题（热门标记优先，其次查看次数） */
    List<FaqListVO> hotList(Integer limit);

    /**
     * 有用反馈（helpful=1 有帮助，0 没帮助）。
     * <p>这两个计数是 FAQ 语料的唯一体检指标：一条问题被点了 20 次「没帮助」，
     * 就该改答案，而不是继续摆在首页。
     */
    int feedback(Long faqId, Integer helpful);

    // ===== 后台维护 =====

    /** 后台列表（含停用的，供运营维护） */
    PageResult<FaqListVO> adminPage(FaqPageQueryDTO dto);

    /** 后台详情 */
    FaqListVO adminGetById(Long faqId);

    /** 新增或修改，返回主键 */
    String adminUpsert(FaqUpsertDTO dto);

    /**
     * 删除（物理删）。
     * <p>{@code sys_faq.faq_no} 的唯一键<b>不含 del_flag</b>，软删的行仍然占着这个键 ——
     * 删掉再新增同一编号必然 Duplicate entry。这类表删除只能物理删。
     */
    void adminDelete(Long faqId);
}

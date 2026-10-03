package com.his.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.AnesthesiaFollowupQueryPageDTO;
import com.his.operation.dto.AnesthesiaFollowupUpsertDTO;
import com.his.operation.vo.AnesthesiaFollowupVO;
import com.his.operation.vo.OperationApplyVO;

import java.util.List;

/**
 * 麻醉术后随访服务（P134.3）。
 *
 * <p>状态机只有两态：<b>草稿(0) ──完成──▶ 已完成(1)</b>，完成即锁死（不可改不可删），
 * 草稿可改可删。随访挂在<b>麻醉记录</b>上（一台麻醉可有多轮随访：术后即刻/24h/48h/追加）。
 *
 * <p>三条前提：
 * <ul>
 *   <li><b>麻醉记录未提交不可随访</b>：麻醉过程还没定稿，评"恢复得怎么样"评的是不存在的基准；</li>
 *   <li><b>随访时间不得早于麻醉结束</b>：麻醉还没结束就"术后随访"，是时间轴上的自相矛盾；</li>
 *   <li><b>勾选任何并发症，就必须写经过与处理</b>："发现了却没处理记录"是随访里最糟的一条链。</li>
 * </ul>
 */
public interface AnesthesiaFollowupService {

    /** 随访分页 */
    IPage<AnesthesiaFollowupVO> listPage(AnesthesiaFollowupQueryPageDTO query);

    /** 随访详情（含并发症字典） */
    AnesthesiaFollowupVO getDetailById(Long id);

    /** 某条麻醉记录的全部随访（按轮次升序 = 这条链的发生顺序） */
    List<AnesthesiaFollowupVO> listByRecord(Long recordId);

    /** 新增/修改草稿（返回随访单号；轮次服务端定） */
    String save(AnesthesiaFollowupUpsertDTO dto);

    /** 完成随访（草稿 → 已完成；疼痛/恢复必填，并发症三件套齐） */
    void finish(Long id);

    /** 删除（仅草稿） */
    void deleteById(Long id);

    /** 随访欠账数（工作台角标） */
    long countOverduePending();

    /** 并发症要点字典 */
    List<OperationApplyVO.CheckItem> adverseItems();
}

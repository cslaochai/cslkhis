package com.his.clinical.service;

import com.his.common.base.PageResult;
import com.his.clinical.dto.ArchiveBorrowApplyDTO;
import com.his.clinical.dto.ArchiveBorrowAuditDTO;
import com.his.clinical.dto.ArchiveBorrowPageQueryDTO;
import com.his.clinical.vo.ArchiveBorrowStatsVO;
import com.his.clinical.vo.ArchiveBorrowVO;

/**
 * 病案借阅/复印服务
 */
public interface ArchiveBorrowService {

    /**
     * 分页查询
     */
    PageResult<ArchiveBorrowVO> page(ArchiveBorrowPageQueryDTO query);

    /**
     * 详情
     */
    ArchiveBorrowVO getDetailById(Long id);

    /**
     * 工作台统计（待审核/已借出/超期未还/已归还）
     */
    ArchiveBorrowStatsVO stats();

    /**
     * 申请借阅/复印（服务端取当前员工作为申请人）
     */
    Long apply(ArchiveBorrowApplyDTO dto);

    /**
     * 审核（借阅通过 → 已借出；复印通过 → 已复印；拒绝 → 已拒绝）
     */
    void audit(ArchiveBorrowAuditDTO dto);

    /**
     * 归还（仅借阅单，2 → 3）
     */
    void giveBack(Long id);

    /**
     * 删除待审核单（仅申请人本人）
     */
    void deleteById(Long id);

    /**
     * 借阅超期提醒：超期未还的每日提醒申请人一条，返回本轮发送条数（可重入）
     */
    int notifyOverdue();
}

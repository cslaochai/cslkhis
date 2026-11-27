package com.his.clinical.service;

import com.his.clinical.dto.EmrHandoverAssignDTO;
import com.his.clinical.dto.EmrHandoverPageQueryDTO;
import com.his.clinical.entity.BizEmrDraftHandover;
import com.his.clinical.vo.EmrHandoverVO;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 病历草稿移交代补服务。
 *
 * <p>离岗/调离医生名下未提交草稿的承接闭环：移交留痕 → 承接人待补列表 → 代补提交闭环。
 * 病历经治医生归属按就诊事实快照固化，不因移交改变。
 */
public interface EmrHandoverService {

    /**
     * 承接人视角：本人名下待代补的草稿移交记录。
     *
     * @return 待代补状态、承接人=当前登录员工的记录，按就诊日期倒序
     */
    List<EmrHandoverVO> listMyReceiving();

    /**
     * 管理侧移交台账分页（科室上级/质控/医务查看全部移交记录）。
     */
    PageResult<EmrHandoverVO> pageManaged(EmrHandoverPageQueryDTO queryDTO);

    /**
     * 某员工作为承接人、仍处于待代补状态的病历ID集合。
     *
     * <p>供「我的草稿」列表与续写权限闸使用：本人经治 ∪ 本人承接代补。
     */
    List<Long> listPendingReceivingRecordIds(Long employeeId);

    /**
     * 按病历ID集合批量取待代补移交单（草稿列表打「代补」标记用）。
     */
    List<BizEmrDraftHandover> listPendingByRecordIds(List<Long> recordIds);

    /**
     * 批量移交草稿给承接人。
     *
     * <p>闸门：病历存在且为草稿态；承接人在职；同一病历不存在待代补移交单。
     */
    void assign(EmrHandoverAssignDTO dto);

    /**
     * 撤销一份待代补移交单（仅待代补态可撤），并关闭承接人待办。
     */
    void revoke(Long id);

    /**
     * 病历代补提交后闭环：待代补单置为已完成，记录完成人/时间，关闭待办。
     *
     * <p>由结诊收尾调用；无待代补单时静默返回。
     */
    void markCompleted(Long recordId, Long operatorId, String operatorName);

    /**
     * 取某病历当前待代补状态的移交单；没有或已完成/撤销时返回 {@code null}。
     *
     * <p>续写/保存权限闸用：经治医生本人 ∪ 待代补承接人可继续编辑。
     */
    BizEmrDraftHandover findPendingByRecord(Long recordId);
}

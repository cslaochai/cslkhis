package com.his.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.OperationApplyQueryPageDTO;
import com.his.operation.dto.OperationApplyUpsertDTO;
import com.his.operation.dto.OperationCancelDTO;
import com.his.operation.dto.OperationFinishDTO;
import com.his.operation.dto.OperationPreopCheckDTO;
import com.his.operation.dto.OperationScheduleDTO;
import com.his.operation.vo.OperationApplyVO;
import com.his.operation.vo.OperationScheduleMatrixVO;

import java.util.List;

/**
 * 住院手术闭环服务（P4.3）。
 *
 * <p>状态机（每一步都有明确的"谁能做、前提是什么"）：
 * <pre>
 *   0-待排期 ──排台──▶ 1-已排期 ──术前核对──▶ 2-术前核对完成 ──完成──▶ 3-已完成
 *       │                 │
 *       └──────取消───────┴──▶ 4-已取消
 * </pre>
 *
 * <p>三条不可破的前置条件：
 * <ul>
 *   <li><b>未排期不可核对</b>：没有手术间/时段/主刀，核对的是一个不存在的手术；</li>
 *   <li><b>未核对不可完成</b>：术后补一条核对记录是伪造（同"医嘱未校对不可执行"）；</li>
 *   <li><b>核对完成后不可取消</b>：患者已经进了手术区流程，停台是另一件事，
 *       不能用"取消"把整条申请抹成从未发生。</li>
 * </ul>
 *
 * <p><b>完成时会回写两份正式文书</b>：病案首页手术明细（病案首页手术明细）
 * 与住院病历（record_type=5 手术记录），并把两个 ID 回填到申请单。
 * 状态是"已完成"而 {@code operationId}/{@code recordId} 为空 = 链断了，属于必须拦住的假数据。
 */
public interface OperationApplyService {

    /** 手术申请分页 */
    IPage<OperationApplyVO> listPage(OperationApplyQueryPageDTO query);

    /** 手术申请详情 */
    OperationApplyVO getDetailById(Long applyId);

    /** 某次住院的全部手术申请（按申请时间升序 = 这条链的发生顺序） */
    List<OperationApplyVO> listByAdmission(Long admissionId);

    /** 发起 / 修改手术申请（返回手术申请单号；修改仅允许「待排期」） */
    String save(OperationApplyUpsertDTO dto);

    /** 排台（待排期 → 已排期；已排期可改期） */
    void schedule(OperationScheduleDTO dto);

    /** 术前核对（已排期 → 术前核对完成；必核项缺失直接拒绝） */
    void preopCheck(OperationPreopCheckDTO dto);

    /** 完成（术前核对完成 → 已完成；回写病案首页手术明细 + 手术记录病历） */
    void finish(OperationFinishDTO dto);

    /** 取消（仅待排期 / 已排期 → 已取消） */
    void cancel(OperationCancelDTO dto);

    /** 未完成手术数（工作台角标） */
    long countUnfinished(Long admissionId);

    /** 排台总表：某天 × 启用手术间的矩阵（含待排期暂存区与未登记手术间兜底桶） */
    OperationScheduleMatrixVO scheduleMatrix(String date);

    /** 已用过的手术间（下拉候选） */
    List<String> roomList();

    /** 术前核对要点字典 */
    List<OperationApplyVO.CheckItem> checkItems();
}

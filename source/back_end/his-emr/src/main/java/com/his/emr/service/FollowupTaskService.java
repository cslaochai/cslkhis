package com.his.emr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.emr.dto.FollowupQueryDTO;
import com.his.emr.dto.FollowupTaskDTO;
import com.his.emr.entity.BizFollowupTask;
import com.his.emr.vo.BizFollowupTaskVO;
import com.his.emr.vo.FollowupStatVO;

import java.util.List;

/**
 * 随访任务服务接口
 */
public interface FollowupTaskService extends IService<BizFollowupTask> {

    /**
     * 分页（患者/姓名/类型/状态/科室/逾期；科室按登录岗位强制收口）
     */
    PageResult<BizFollowupTaskVO> listPage(FollowupQueryDTO dto);

    /**
     * 出院随访任务看板（服务端聚合；科室范围同分页口径）
     */
    FollowupStatVO stat();

    /**
     * 新建 / 修改随访任务（G20：修改仅「待随访」可改；患者号/姓名/电话自动快照）
     */
    BizFollowupTaskVO upsertTask(FollowupTaskDTO.Upsert dto);

    /**
     * 按出院记录一键生成随访计划（G20：同一出院记录幂等，重复调用返回已生成任务）
     */
    BizFollowupTask createFromDischarge(FollowupTaskDTO.FromDischarge dto);

    /**
     * 出参口径的按出院记录生成：手机号按编辑回显给明文
     */
    BizFollowupTaskVO createTaskFromDischarge(FollowupTaskDTO.FromDischarge dto);

    /**
     * 扫描「已出院还没有随访计划」的患者并补建，返回本次新建条数。
     *
     * <p><b>为什么由本域扫描而不是出院办理时同步建单</b>：依赖方向是随访→出院，
     * 出院方在本模块之下，反向调用会成环；而「每个存活出院都该有一条随访计划」
     * 是随访域的规则，事实的写入方自己兑现，不需要上游来通知。
     *
     * <p>逐条独立处理：某一条建失败只留痕，不把整批回滚，下一轮扫描还会再补上。
     */
    int autoCreateFromDischarge(int limit);

    /**
     * 获取随访任务详情（明文手机号只服务编辑回显，列表一律脱敏）
     */
    BizFollowupTaskVO getFollowupTaskDetail(Long taskId);

    /**
     * 开始随访
     */
    boolean startFollowup(Long taskId, Long executorId, String executorName);

    /**
     * 完成随访
     */
    boolean completeFollowup(Long taskId, String result);

    /**
     * 取消随访
     */
    boolean cancelFollowup(Long taskId, String reason);

    /**
     * 由随访任务生成复诊号（复诊来源 4）。
     *
     * <p>建号动作本身归挂号域（{@code AppointService#addAppoint}），这里只负责
     * 「带任务上下文去挂号 + 把结果回写到任务上」。同一个任务重复点第二次会被拒，
     * 除非上一次那张号已经作废（退号/爽约/过号）—— 那种情况下患者确实还需要复诊。
     */
    BizFollowupTaskVO createRevisitAppoint(FollowupTaskDTO.CreateRevisit dto);

    /**
     * 患者端「我的随访」：只查患者本人名下的任务，不走科室收口（患者没有科室岗位）。
     */
    List<BizFollowupTaskVO> listForPatient(Long patientId);

    /**
     * 患者提交反馈（小程序回写 patient_reply）。
     * 归属两道闸：任务必须属于该患者；任务未被取消才允许反馈。
     */
    boolean replyFromPatient(Long taskId, Long patientId, String replyText);

    /**
     * 登记电话外呼：通道闸后置「待外呼」并返回任务（明文电话供护士拨号）。
     * 已取消/已完成拒呼；已在待外呼中拒重复登记。
     */
    BizFollowupTaskVO registerCall(Long taskId);

    /**
     * 回填外呼结果：仅待外呼可回填；接通且任务还在待随访时转随访中。
     */
    boolean recordCallResult(com.his.emr.dto.FollowupCallResultDTO dto);
}

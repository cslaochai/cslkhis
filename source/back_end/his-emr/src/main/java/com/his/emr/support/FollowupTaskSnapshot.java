package com.his.emr.support;

import com.his.emr.entity.BizFollowupTask;
import lombok.Data;

import java.io.Serializable;

/**
 * 随访任务的最小快照（发放问卷时由随访侧构造后传入）。
 *
 * <p><b>为什么不用 SurveyService 直接查随访任务</b>：随访完成后要自动发问卷（FollowupTaskService → SurveyService），
 * 若评价侧再反向依赖 FollowupTaskService 取任务，两个 ServiceImpl 就构成 Bean 循环依赖，
 * 而 Spring 默认禁止循环引用，现象是应用起不来。传快照把方向定死成单向。
 *
 * <p>只带「发一张问卷需要的东西」：谁、怎么联系、哪个科室。多带字段等于给评价侧开了读随访业务的口子。
 */
@Data
public class FollowupTaskSnapshot implements Serializable {

    private Long taskId;

    private Long patientId;

    private String patientNo;

    private String patientName;

    /** 明文手机号（评价侧入库供外呼拨号，出参一律脱敏） */
    private String phone;

    private Long deptId;

    private String deptName;

    public static FollowupTaskSnapshot of(BizFollowupTask task) {
        FollowupTaskSnapshot snapshot = new FollowupTaskSnapshot();
        if (task != null) {
            snapshot.setTaskId(task.getId());
            snapshot.setPatientId(task.getPatientId());
            snapshot.setPatientNo(task.getPatientNo());
            snapshot.setPatientName(task.getPatientName());
            snapshot.setPhone(task.getPhone());
            snapshot.setDeptId(task.getDeptId());
            snapshot.setDeptName(task.getDeptName());
        }
        return snapshot;
    }
}

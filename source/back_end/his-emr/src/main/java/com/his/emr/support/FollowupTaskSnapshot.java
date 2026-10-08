package com.his.emr.support;

import com.his.emr.entity.BizFollowupTask;
import lombok.Data;

import java.io.Serializable;

/**
 * 随访任务的最小快照（发放问卷时由随访侧构造后传入）。
 */
@Data
public class FollowupTaskSnapshot implements Serializable {

    private Long taskId;

    private Long patientId;

    private String patientNo;

    private String patientName;

    /**
     * 明文手机号（评价侧入库供外呼拨号，出参一律脱敏）
     */
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

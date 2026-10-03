package com.his.patient.service;

import com.his.patient.dto.GuardianUpsertDTO;
import com.his.patient.dto.GuardianBindDTO;
import com.his.patient.dto.GuardianSendAddCodeDTO;
import com.his.patient.dto.GuardianSendBindCodeDTO;
import com.his.patient.vo.GuardianPatientVO;
import com.his.patient.vo.SmsSendVO;
import com.his.security.CurrentUser;

import java.util.List;

/**
 * 就诊人绑定服务（患者端小程序「多就诊人」基座）。
 *
 * <p>口径：一个 user_type=3 的登录账号可绑多个患者基本信息；
 * 注册时落的用户的患者ID 视为「本人」绑定（relation=1），
 * 两者共同构成该账号的<b>可访问患者集合</b> —— 患者端所有带 patientId 的
 * 读写接口都必须先过 {@link #accessiblePatientIds} 校验。
 */
public interface PatientGuardianService {

    /**
     * 当前登录账号可访问的患者ID集合（绑定表 + 用户的患者ID 兜底并入）。
     * 非患者账号（员工/管理员）返回空集合 —— 调用方据此拒绝。
     */
    List<Long> accessiblePatientIds(CurrentUser user);

    /**
     * 当前登录账号能否访问该就诊人。患者端所有带 patientId / 能从记录反查出 patientId
     * 的读写接口都走这里收口，不要各自写一遍，也不要信前端自报的归属。
     */
    boolean canAccessPatient(Long patientId);

    /**
     * 院内（医生/护士/工作站）接口上的越权判定：员工账号一律放行，
     * 患者账号（user_type=3）只有访问自己绑定的就诊人才放行。
     *
     * <p>用于「同一套接口既给工作站用、又可能被患者 token 调到」的场景，
     * 这类接口历史上只认前端传的 patientId，患者改个数字就能读别人的病历。
     */
    boolean patientScopeViolated(Long patientId);

    /** 我的就诊人列表（含关系文案、默认标记） */
    List<GuardianPatientVO> myPatients();

    /** 绑定场景发码：按姓名+身份证定位档案，验证码发往建档预留手机号 */
    SmsSendVO sendBindCode(GuardianSendBindCodeDTO dto);

    /** 绑定已有档案：姓名 + 身份证 + 建档预留手机号 + 短信码四因子，匹配不上不建任何数据 */
    GuardianPatientVO bindPatient(GuardianBindDTO dto);

    /** 新增建档场景发码：验证码发往 dto.phone（操作人手机） */
    SmsSendVO sendAddCode(GuardianSendAddCodeDTO dto);

    /** 新建档并自动绑定（需短信验证；身份证已建档时提示改用绑定） */
    GuardianPatientVO addPatient(GuardianUpsertDTO dto);

    /** 解绑（软删）。账号本人（用户的患者ID）不可解绑 */
    void unbindPatient(Long patientId);

    /** 设为默认就诊人 */
    void setDefault(Long patientId);

    /** 绑定当前账号的微信 openid（订阅消息发送用，一账号一个，重复绑定以最新为准） */
    void bindOpenid(String openid);

    /** 通道自测：给当前账号发一条测试订阅消息，返回发送结果文案 */
    String testNotify();
}

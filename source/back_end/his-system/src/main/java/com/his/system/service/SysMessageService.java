package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.system.dto.MessageQueryPageDTO;
import com.his.system.entity.SysMessage;
import com.his.system.vo.MessageTypeCountVO;
import com.his.system.vo.SysMessageVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * 消息通知服务接口
 */
public interface SysMessageService extends IService<SysMessage> {

    /**
     * 当前登录人的收件箱分页（收件人由实现内取当前登录人，调用方不传身份）。
     */
    PageResult<SysMessageVO> queryMessagePage(MessageQueryPageDTO queryDTO);

    /**
     * 为当前登录人建立实时推送连接。
     */
    SseEmitter subscribe();

    /**
     * 当前登录人的未读数。
     */
    int countUnreadOfCurrentUser();

    /**
     * 当前登录人按业务类型分组的消息数。
     */
    List<MessageTypeCountVO> typeCountsOfCurrentUser();

    /**
     * 标记单条消息已读（未读才更新，已读时不重复刷时间）
     */
    void markRead(Long messageId);

    /**
     * 当前登录人全部未读消息置为已读。
     */
    void markAllRead();

    /**
     * 当前登录人的收件人标识：优先取员工身份，院外用户没有员工档案时退回用户身份。
     *
     * <p>站内信的投递方（检验报告、危急值提醒）写入的收件人是医师的员工身份，
     * 读取侧若按用户身份匹配会出现「未读数恒为 0、列表恒为空」且无任何报错的静默失灵。
     */
    Long currentReceiverId();

    /**
     * 发送站内信（通知型默认值：severity=info，无 payload，无 handle_status）。
     * 待办型消息请用 {@link #sendSystemMessage(Long, String, String, String, String, Long, String, String, Integer)}
     * 显式带上紧急度与处理状态。
     */
    boolean sendSystemMessage(Long receiverId, String receiverName, String title, String content,
                              String bizType, Long bizId);

    /**
     * 发送站内信（完整参数）。
     *
     * @param severity     紧急度：info/warning/urgent（urgent 参与列表 SQL 置顶排序，见 sql/70）
     * @param payload      结构化负载 JSON 字符串（patientName/prescriptionNo/bedNo/opinion 等），可为 null
     * @param handleStatus 处理状态：0-待处理（待办型）；null-通知型（用 read_status 闭环）
     */
    boolean sendSystemMessage(Long receiverId, String receiverName, String title, String content,
                              String bizType, Long bizId, String severity, String payload, Integer handleStatus);

    /**
     * 发送微信订阅消息（患者端小程序出站通知）。
     *
     * <p>与站内信的区别：收件人是患者账号（receiver_id = 用户的ID），openid 由实现内
     * 按 receiverUserId 现查用户（调用方不传身份）；发送结果直接体现在 send_status
     * （1 成功 / 2 失败，原因落 error_msg），通道未启用、未绑 openid 都算「发送失败」留痕，不静默吞掉。
     *
     * @param scene    业务场景码（映射 yml {@code wechat.miniapp.templates}）
     * @param page     点击跳转页面路径，可空
     * @param data     模板字段（字段名 → 文本值）
     * @return 是否发送成功
     */
    boolean sendWechatMessage(Long receiverUserId, String receiverName, String scene, String page,
                              Map<String, String> data, String title, String content,
                              String bizType, Long bizId);

    /**
     * 发送微信订阅消息给患者（按 patient_id 反查患者账号用户，业务侧免查用户表）。
     *
     * <p>与 {@link #sendWechatMessage} 的区别：入参是患者主档 ID 而非用户 ID ——
     * 报告发布/叫号这类触发点手里只有 patientId。患者未注册账号（无用户行）
     * 时不落留痕直接返回 false（没有收件人谈不上送达）。
     */
    boolean sendWechatToPatient(Long patientId, String scene, String page, Map<String, String> data,
                                String title, String content, String bizType, Long bizId);

    /**
     * 当前收件人的未读消息数（send_status=1，口径与 listPage 一致）。
     */
    int countUnread(Long receiverId);

    /**
     * 按业务类型分组统计当前收件人的消息数（抽屉 Tab / 分组徽标用）。
     *
     * @return 每个业务类型一条，含总数与未读数
     */
    List<MessageTypeCountVO> typeCounts(Long receiverId);

    /**
     * 查某条业务对象当前发过的全部消息收件人（员工ID，去重）。
     * 危急值超时升级等「追发通知」场景用它找原收件人 —— 收件箱里可能有
     * 开单医生也可能有兜底接收人的消息，升级时一个都不能漏。
     */
    List<Long> receiverIdsOfBiz(String bizType, Long bizId);
}

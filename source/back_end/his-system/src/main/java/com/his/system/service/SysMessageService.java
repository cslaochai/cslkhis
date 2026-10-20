package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.system.dto.MessagePageQueryDTO;
import com.his.system.entity.SysMessage;
import com.his.system.vo.MessageTypeCountVO;
import com.his.system.vo.SysMessageVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知服务接口
 */
public interface SysMessageService extends IService<SysMessage> {

    /**
     * 当前登录人的收件箱分页（收件人由实现内取当前登录人，调用方不传身份）。
     */
    PageResult<SysMessageVO> queryMessagePage(MessagePageQueryDTO queryDTO);

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

    /**
     * 把某业务对象名下「待处理」的待办消息批量置为目标处理状态。
     *
     * <p>业务侧动作闭环收件箱用（会诊取消、危急值处置完成），
     * 联动失败不回滚业务单据，由调用方按日志处理。
     *
     * @param targetStatus 目标处理状态（1-已处理 2-已关闭）
     */
    void closeTodoByBiz(String bizType, Long bizId, Integer targetStatus);

    /**
     * 同一业务单对同一收件人在时窗内是否发过指定紧急度的消息。
     *
     * <p>催办类动作用它判重：永久判重会让「催过一次就永远不再催」，
     * 而挂住的等床/转诊单恰恰会越挂越久。
     *
     * @param severity 紧急度（催办传 urgent，只跟催办类判重，别把通知型算进来）
     * @param hours    时窗（小时）
     */
    boolean sentWithinHours(String bizType, Long bizId, Long receiverId, String severity, int hours);

    /**
     * 某业务对象自指定时刻起已发的消息条数。
     *
     * <p>定时与手动补跑共用同一个催办入口时用它保证「每张单每天最多催一次」的可重入。
     */
    long countSentSince(String bizType, Long bizId, LocalDateTime since);
}

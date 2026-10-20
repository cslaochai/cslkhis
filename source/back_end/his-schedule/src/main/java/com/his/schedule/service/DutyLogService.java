package com.his.schedule.service;

import com.his.common.base.PageResult;
import com.his.schedule.dto.DutyLogHandoverDTO;
import com.his.schedule.dto.DutyLogPageQueryDTO;
import com.his.schedule.dto.DutyLogUpsertDTO;
import com.his.schedule.vo.DutyLogVO;

import java.util.List;

/**
 * 总值班日志服务：值班事项登记与交班、签收闭环的写入收口。
 */
public interface DutyLogService {

    /**
     * 值班日志分页：不传日期时默认只给近 7 天（交班本是流水账，翻几个月前没意义）。
     * 用于「总值班日志（交班本）」页的日志表格与查询卡。
     */
    PageResult<DutyLogVO> listPage(DutyLogPageQueryDTO q);

    /**
     * 待当前登录人签收的遗留事项（交班对象是自己、状态已交班且未签收）。
     * 交班本页顶部的待签收提醒条用它——接班人一进页面就该看到压在自己头上的事。
     */
    List<DutyLogVO> pendingMine();

    /**
     * 登记或修改值班事项：新增时值班人留空即记在当前总值班头上，状态只能取待处理/已处理，
     * 已交班或已签收的记录不再允许改内容。
     * 交班本的「登记事项」弹框保存时调用；代记、补记须显式选值班人，记录人会单独留痕。
     */
    Long upsert(DutyLogUpsertDTO dto);

    /**
     * 删除一条值班日志，已签收的拒绝（交接完成后删除等于抹掉交接凭据）。
     * 交班本列表行的删除动作。
     */
    void deleteById(Long id);

    /**
     * 交班：把本班未处理完的事项交给下一班，重复交班拒绝。
     * 交班本「交班」弹框提交时调用；接班人留空则由排班推算下一班总值班（白班交同日夜班，夜班交次日白班）。
     */
    void handover(DutyLogHandoverDTO dto);

    /**
     * 签收：确认收到交接事项，只有交班对象本人可签，且必须是已交班状态。
     * 交班本待签收提醒条与列表行的签收按钮，签收完这条交接才算闭环。
     */
    void ack(Long id);
}

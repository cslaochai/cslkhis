package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.InfusionRoomDTO;
import com.his.medicaltech.vo.InfusionRoomVO;

import java.util.List;

/**
 * 门诊输液室服务：入座、皮试、输注、巡视、结束这条输液护理过程链的业务收口。
 */
public interface InfusionRoomService {

    /**
     * 输液室全部座位及占用它们的输液单摘要。
     * 用于门诊输液站入座环节挑选空位时取座位清单。
     */
    List<InfusionRoomVO.Seat> seats();

    /**
     * 新增或修改输液座位，已被输液单占用的座位不允许改动。
     * 用于门诊输液站页的座位维护。
     */
    InfusionRoomVO.Seat seatUpsert(InfusionRoomDTO.SeatUpsertDTO dto);

    /**
     * 患者入座：建输液单并占用座位，需皮试时先落在待皮试。
     * 用于门诊输液站接收治疗执行单入科登记。
     */
    InfusionRoomVO.Infusion admit(InfusionRoomDTO.AdmitDTO dto);

    /**
     * 为待皮试的输液单执行皮试，从登记时刻起算观察窗。
     * 用于门诊输液站页待皮试患者的打皮试动作。
     */
    InfusionRoomVO.Infusion skinTest(InfusionRoomDTO.SkinTestCreate dto);

    /**
     * 判读皮试结果：观察不足十五分钟拒绝，阳性自动取消输液单并释放座位。
     * 用于门诊输液站页打皮试后的结果判读。
     */
    InfusionRoomVO.Infusion skinTestResult(InfusionRoomDTO.SkinTestResult dto);

    /**
     * 开始输注：待输注且皮试阴性方可执行，同时记录起始滴速。
     * 用于门诊输液站页座位上的开始输注。
     */
    InfusionRoomVO.Infusion start(InfusionRoomDTO.Start dto);

    /**
     * 记一次输液巡视：滴速、余量与巡视备注。
     * 用于门诊输液站页输液中患者的巡视登记。
     */
    InfusionRoomVO.Round round(InfusionRoomDTO.Round dto);

    /**
     * 结束输注并释放座位，标记不良反应时描述必填。
     * 用于门诊输液站页拔针收尾的完成登记。
     */
    InfusionRoomVO.Infusion finish(InfusionRoomDTO.Finish dto);

    /**
     * 取消输液单：非终态方可取消且必填原因，同时释放座位。
     * 用于门诊输液站页患者停液或转走时的作废。
     */
    InfusionRoomVO.Infusion cancel(InfusionRoomDTO.Cancel dto);

    /**
     * 今日输液单分页查询。
     * 用于门诊输液站页输液单列表的查询卡。
     */
    PageResult<InfusionRoomVO.Infusion> listPage(InfusionRoomDTO.InfusionQuery query);

    /**
     * 取某张输液单的全部巡视记录，按时间升序。
     * 用于门诊输液站页展开单条输液单的巡视轨迹。
     */
    List<InfusionRoomVO.Round> rounds(Long infusionId);

    /**
     * 今日输液看板：座位图与各状态计数一次取回。
     * 用于门诊输液站页顶部的统计卡与座位图。
     */
    InfusionRoomVO.Board board();
}

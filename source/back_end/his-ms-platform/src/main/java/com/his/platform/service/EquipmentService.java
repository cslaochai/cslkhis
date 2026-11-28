package com.his.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.platform.dto.*;
import com.his.platform.vo.EquipmentVO;import com.his.platform.vo.MaintainVO;
import com.his.platform.vo.MeteringVO;

/**
 * 设备档案服务：设备台账与其维保、计量登记记录的查询与写入收口。
 */
public interface EquipmentService {

    /**
     * 设备台账分页：可按关键字、类别、状态筛选，行上带最近维保日期与计量有效期。
     * 用于资产管理设备台账页的查询卡表格。
     */
    IPage<EquipmentVO> listPage(EquipmentPageQueryDTO q);

    /**
     * 单台设备详情：档案字段加最近一批维保、计量记录。
     * 用于设备台账点开一行后的记录详情弹框。
     */
    EquipmentVO getDetailById(Long equipmentId);

    /**
     * 一台设备的维保记录分页：按维保类型筛选，维保日期倒序。
     * 供按设备核查维保历史的取数节点，入参必须指定设备。
     */
    IPage<MaintainVO> maintainListPage(MaintainPageQueryDTO q);

    /**
     * 维保登记：落一条维保记录并回写设备档案的最近维保日期。
     * 用于设备台账「登记维保」弹框保存。
     */
    MaintainVO maintainCreate(MaintainCreateDTO dto);

    /**
     * 删除维保记录：只处理录错的登记，删后按剩余记录重算档案最近维保日期。
     * 用于设备详情里维保记录行的删除动作。
     */
    void maintainDelete(Long id);

    /**
     * 一台设备的计量记录分页：按计量类型筛选，计量日期倒序。
     * 供按设备核查强检/校准历史的取数节点，入参必须指定设备。
     */
    IPage<MeteringVO> meteringListPage(MeteringPageQueryDTO q);

    /**
     * 计量登记：落一条强检/校准记录，计量有效期与是否过期由该记录的有效期现算。
     * 用于设备台账「登记计量」弹框保存。
     */
    MeteringVO meteringCreate(MeteringCreateDTO dto);

    /**
     * 删除计量记录：只处理录错的登记，档案不落冗余列故无需重算。
     * 用于设备详情里计量记录行的删除动作。
     */
    void meteringDelete(Long id);

    /**
     * 维护设备是否进入共享池。只改 rentable 标志，不改台账状态。
     */
    void updateRentable(EquipmentRentableDTO dto);

    /**
     * 设备建档/修改：id 为空走建档，否则按 id 更新。
     * 设备编码是资产编号，建档后不可改；8-物流设备（机器人）强制不进共享池，并同步物联网接入台账。
     * 用于设备管理页「新增设备 / 编辑」弹框保存。
     */
    void equipmentUpsert(EquipmentUpsertDTO dto);

    /**
     * 设备状态变更（停用/启用/维修中/报废）；物流设备同步接入台账启停，停用的车不再参与派单。
     * 用于设备管理页操作列的「停用 / 启用」动作。
     */
    void updateStatus(EquipmentStatusDTO dto);
}

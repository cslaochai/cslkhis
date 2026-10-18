package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.system.dto.ClinicRoomQueryDTO;
import com.his.system.dto.ClinicRoomUpsertDTO;
import com.his.system.entity.SysClinicRoom;
import com.his.system.vo.ClinicRoomVO;

import java.util.List;

/**
 * 诊室档案服务：科室下属诊室的名称、编码、排队前缀与启停状态的业务收口。
 */
public interface SysClinicRoomService extends IService<SysClinicRoom> {

    /**
     * 诊室分页：按名称、所属科室、状态筛选，编码升序。
     * 用于「诊室管理」页的查询卡与表格。
     */
    PageResult<ClinicRoomVO> listPage(ClinicRoomQueryDTO queryDTO);

    /**
     * 诊室全量列表（不分页）：与分页查询同一套筛选条件。
     * 用于排班登记、修改与排班模板里按科室挑诊室的下拉候选。
     */
    List<ClinicRoomVO> listAll(ClinicRoomQueryDTO queryDTO);

    /**
     * 单条诊室详情：按主键取全量字段。
     * 用于诊室新增/修改弹框回显已有配置（编辑入口必须拿到明文原值，不能拿列表行残缺的数据回填）。
     */
    ClinicRoomVO getInfo(Long roomId);

    /**
     * 新增/修改诊室：带 id 走修改，不带则新建一条归属指定科室的诊室。
     * 用于诊室管理页的新增/修改弹框保存。
     *
     * @return 面向用户的操作结果文案（新增/修改两条路文案不同）
     */
    String upsert(ClinicRoomUpsertDTO upsertDTO);

    /**
     * 删除诊室。
     * 用于「诊室管理」页操作列的删除，删前由页面二次确认。
     */
    void delete(Long roomId);
}

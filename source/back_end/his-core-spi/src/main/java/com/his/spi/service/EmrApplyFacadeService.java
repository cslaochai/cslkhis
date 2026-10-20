package com.his.spi.service;

import com.his.common.base.PageResult;
import com.his.spi.dto.InspectionApplyPageQueryDTO;
import com.his.spi.vo.InspectionApplyBriefVO;
import com.his.spi.vo.LaboratoryApplyBriefVO;

/**
 * 申请单事实端口：检查申请单与检验申请单的跨域读取口，供医技域开单建记录、预约与影像登记取数。
 *
 * <p>申请单的「开单—提交—取消」流转与签名状态由病历域自己写，本端口只承诺读；
 * 医技侧推进申请单状态的动作另有各自的写入口，不经这里。
 */
public interface EmrApplyFacadeService {

    /**
     * 按主键取一张检查申请单。
     *
     * <p>用于检查预约占号前核对申请单状态与项目、缴费后生成检查执行记录时带出病案与项目维度、
     * 胶片记账时取就诊标识、影像上传时取患者与部位锚点。
     *
     * @param applyId 检查申请单ID（可空，空则返回 null）
     * @return 申请单摘要，不存在或已删除返回 null
     */
    InspectionApplyBriefVO getInspectionApplyById(Long applyId);

    /**
     * 按主键取一张检验申请单。
     *
     * <p>用于缴费后生成检验执行记录时带出病案与项目维度，以及检验影像登记时取患者与标本锚点。
     *
     * @param applyId 检验申请单ID（可空，空则返回 null）
     * @return 申请单摘要，不存在或已删除返回 null
     */
    LaboratoryApplyBriefVO getLaboratoryApplyById(Long applyId);

    /**
     * 检查申请单分页：急诊单置顶、按申请落库顺序给出，用于检查预约中心「待预约申请」查询卡表格。
     *
     * @param query 筛选条件与分页参数
     * @return 分页的申请单摘要
     */
    PageResult<InspectionApplyBriefVO> pageInspectionApply(InspectionApplyPageQueryDTO query);

    /**
     * 检查申请单条数：与分页同一套筛选条件，用于预约工作台的状态卡计数。
     *
     * @param query 只取筛选条件，分页参数不参与计数
     * @return 命中的申请单条数
     */
    long countInspectionApply(InspectionApplyPageQueryDTO query);
}

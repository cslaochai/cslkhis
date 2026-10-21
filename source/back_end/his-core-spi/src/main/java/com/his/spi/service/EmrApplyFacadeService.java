package com.his.spi.service;

import com.his.common.base.PageResult;
import com.his.spi.dto.InspectionApplyPageQueryDTO;
import com.his.spi.vo.InspectionApplyBriefVO;
import com.his.spi.vo.LaboratoryApplyBriefVO;

import java.time.LocalDateTime;

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

    /**
     * 占号成功：检查申请单推进到 3-已预约，并回写预约时刻。
     *
     * <p>由检查预约侧占号后调用，只翻状态与预约时刻，不动项目/缴费等其他字段；
     * 是否可预约的前置状态校验（2-已缴费、急诊绿色通道 1-已提交，以及「残留已预约但无
     * 在办预约单」的重约恢复）由调用方在编排侧完成，本写端口按主键命中推进；
     * 返回实际命中行数，0 表示未命中（单不存在/已删除/已被他人处理），调用方据此回滚。
     *
     * @param applyId         检查申请单ID
     * @param appointmentTime 预约时刻（预约网格起点）
     * @param operator        操作人姓名（写更新人审计字段）
     * @return 命中行数（1 成功，0 未推进）
     */
    int markInspectionBooked(Long applyId, LocalDateTime appointmentTime, String operator);

    /**
     * 到检签到：仅当申请单仍停在 3-已预约 时推进到 4-检查中。
     *
     * <p>状态守卫下沉到 WHERE（apply_status = 3），并发重复签到只允许一方推进，
     * 不覆盖已进入执行链（检查中/已出报告）或已回退的状态；返回命中行数，0 表示未停在已预约。
     *
     * @param applyId  检查申请单ID
     * @param operator 操作人姓名（写更新人审计字段）
     * @return 命中行数（1 成功，0 未推进）
     */
    int markInspectionArrived(Long applyId, String operator);

    /**
     * 取消/爽约收尾：仅当申请单仍停在 3-已预约 时，按预约前状态精确回退并清空预约时刻。
     *
     * <p>状态守卫下沉到 WHERE（apply_status = 3），回退目标状态由调用方按该单预约前
     * 记录（2-已缴费 / 急诊 1-已提交）传入；已进入执行链的单不被回退。
     * 返回命中行数，0 表示未停在已预约（可能已进入执行链），调用方记录日志即可，不抛错。
     *
     * @param applyId    检查申请单ID
     * @param prevStatus 预约前状态（回退目标码值）
     * @param operator   操作人姓名（写更新人审计字段）
     * @return 命中行数（1 成功，0 未推进）
     */
    int revertInspectionBooked(Long applyId, Integer prevStatus, String operator);
}

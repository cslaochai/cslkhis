package com.his.iot.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 新建配送任务单入参。
 * <p>
 * 只收三个站点 id + 品类 + 箱数：站点名称、所属楼栋楼层、任务来源、本单总重全部由后端从站点表与品类枚举推导，
 * 下单人不再手填楼层和重量（手填必然与实际楼层、载重口径漂移）。
 */
@Data
public class RobotDispatchCreateDTO implements Serializable {

    /** 发起站点：谁发起的这单（决定任务来源，如静配→PIVAS） */
    @NotNull(message = "发起站点不能为空")
    private Long applyStationId;

    /** 取货站点：机器人去哪装货 */
    @NotNull(message = "取货站点不能为空")
    private Long fromStationId;

    /** 收货站点：送到哪 */
    @NotNull(message = "收货站点不能为空")
    private Long toStationId;

    /** 品类：药品/标本/耗材/器械包/被服（单箱基准重量见 RobotGoodsTypeEnum） */
    @NotBlank(message = "物品品类不能为空")
    private String goodsType;

    /** 物品明细 */
    @NotBlank(message = "物品明细不能为空")
    private String goodsName;

    /** 优先级：1-常规 2-批量补货 3-急送 */
    @NotNull(message = "优先级不能为空")
    @AllowedValues(values = {"1", "2", "3"}, message = "优先级取值不合法")
    private Integer priority;

    /** 箱数：总重 = 品类单箱基准重量 × 箱数，上限按单车额定载重校验 */
    @NotNull(message = "箱数不能为空")
    @Min(value = 1, message = "箱数至少为 1")
    private Integer boxCount;

    /** 是否创建后立即派车；false 仅落待调度单 */
    private Boolean autoAssign;

    /** 手工指定机器人编号；为空由调度自动选空闲车 */
    private String robotNo;

    private String remark;
}

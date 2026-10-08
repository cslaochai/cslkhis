package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 膳食方案（sql/168 §2，膳食医嘱的执行侧）。
 *
 * <p>三条铁律：
 * <ul>
 *   <li><b>order_id 唯一</b>：一条 orderClass=10 的住院医嘱只派生一个方案，重复校对/重放不会多出第二条；
 *       该唯一键不含 del_flag，所以删除走物理删（Mapper.purgeById）。</li>
 *   <li><b>confirm_status 才是"执行"</b>：医生开了膳食医嘱而营养科没接收，患者就是没吃上治疗饮食。
 *       膳食医嘱执行率的分子只数 1-已接收，2-已退回必须填原因并让医师重开。</li>
 *   <li><b>只有 route=1-口服才进订餐</b>：管饲制剂由营养科发放、肠外营养走静配，
 *       给肠外营养的患者订一份食堂餐是错的。</li>
 * </ul>
 */
@Data
@TableName("biz_diet_plan")
public class BizDietPlan {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 膳食方案编号（DP+yyyyMMdd+4位序号）
     */
    private String dietNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 患者编号
     */
    private String patientNo;
    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 科室名称
     */
    private String deptName;
    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;
    /**
     * 病区名称
     */
    private String wardName;
    /**
     * 床号
     */
    private String bedNo;

    /**
     * 来源医嘱ID（orderClass=10）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    /**
     * 来源医嘱号
     */
    private String orderNo;
    /**
     * 来源（1-医嘱校对派生 2-营养师手工登记）
     */
    private Integer source;

    /**
     * 饮食类型码（字典 his_diet_type）
     */
    private String dietCode;
    /**
     * 饮食类别（1-基本饮食 2-治疗饮食 3-诊断试验饮食 4-营养支持）
     */
    private Integer dietCategory;
    /**
     * 饮食名称
     */
    private String dietName;
    /**
     * 给食途径：1-口服 2-管饲 3-静脉
     */
    private Integer route;
    /**
     * 管饲/输注方式说明
     */
    private String feedWay;

    /**
     * 每日热量目标 kcal
     */
    private Integer calorieTarget;
    /**
     * 每日蛋白目标 g
     */
    private Integer proteinTarget;
    /**
     * 每日液体量 ml
     */
    private Integer fluidTarget;
    /**
     * 供应餐次（逗号分隔，订餐据此拆行）
     */
    private String mealTypes;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /**
     * 停止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stopTime;

    /**
     * 方案状态（1-执行中 2-已停止 3-已作废）
     */
    private Integer planStatus;
    /**
     * 营养科接收状态（0-待接收 1-已接收 2-已退回）
     */
    private Integer confirmStatus;
    /**
     * 接收/退回时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;
    /**
     * 接收人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long confirmerId;
    /**
     * 接收人姓名
     */
    private String confirmerName;
    /**
     * 退回原因（confirm_status=2 必填）
     */
    private String rejectReason;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableLogic
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}

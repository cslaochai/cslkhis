package com.his.system.vo;

import com.his.system.service.MessageMetricSupport;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 工作台卡片 {@code myTodo} 的出参（我当前未办结的站内信）。
 *
 * <p>对应 {@code MyTodoMetricProviderImpl#summary}：未办结总数 + 加急数 + 前 8 条摘要。
 * 摘要项复用 {@link MessageMetricSupport.Item}（雪花 ID 已转字符串、时间已格式化）。
 *
 * <p><b>类本身是有类型的，但 SPI 边界上仍要转回 Map</b>：
 * {@code WorkbenchMetricProvider#summary} 的返回类型由父接口钉死为
 * {@code Map<String, Object>}（his-medicaltech 等模块有 5 个子接口实现它，改签名会连带
 * 打穿那些模块），而 {@code WorkbenchServiceImpl} 把返回值原样塞进
 * {@code WorkbenchDataVO.data}，前端按 {@code data.xxx} 取键。所以这里是
 * 「先用 VO 算清字段、再用字段名做键拍平」，而不是让 VO 取代 SPI 契约。
 */
@Data
public class WorkbenchMyTodoVO implements Serializable {

    /**
     * 未办结总数
     */
    private Long total;

    /**
     * 其中加急（severity=urgent）条数
     */
    private Long urgentTotal;

    /**
     * 前 8 条摘要（按 加急 → 警告 → 普通，再按发送时间倒序）
     */
    private List<MessageMetricSupport.Item> items;
}

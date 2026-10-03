package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代煎台账分页查询入参（sql/139）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TcmDecoctQueryPageDTO extends PageParam {

    /** 状态（1-待煎 2-已煎 3-已取 9-已作废），空=全部 */
    private Integer decoctStatus;

    /** 模糊词：代煎单号 / 处方号 / 患者姓名 */
    private String keyword;
}

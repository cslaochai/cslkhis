package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 临床侧「我的器械包」分页查询：按当前岗位科室收口，只给本科室看自己申的包。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CssdPackMyPageQueryDTO extends PageParam implements Serializable {

    /** 包状态（0-待回收 6-待签收 7-已签收，不传=全部） */
    private Integer status;

    /** 关键字：条码/包名模糊 */
    private String keyword;
}

package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

/** 代煎状态计数：中药代煎列表各代煎状态的单据数出参。 */
@Data
public class TcmDecoctCountVO implements Serializable {

    /** 待煎数（代煎状态 1-待煎） */
    private Long pending;

    /** 已煎数（代煎状态 2-已煎） */
    private Long decocted;

    /** 已取数（代煎状态 3-已取） */
    private Long picked;

    /** 已作废数（代煎状态 9-已作废） */
    private Long cancelled;
}

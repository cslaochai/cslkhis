package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 诊室摘要：跨模块换取名呼与队列前缀的出参。
 */
@Data
@NoArgsConstructor
public class ClinicRoomBriefVO {

    /**
     * 诊室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 诊室名称
     */
    private String name;

    /**
     * 叫号队列前缀
     */
    private String queuePrefix;
}

package com.mis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 异地就医备案新增/修改
 */
@Data
public class FilingUpsertDTO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;
    private String idCardMask;
    private String insuranceArea;
    private String hospital;
    private String reason;
    private String fromArea;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validStart;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validEnd;
}

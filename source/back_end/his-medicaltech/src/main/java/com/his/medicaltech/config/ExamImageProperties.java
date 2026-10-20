package com.his.medicaltech.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.Set;

/**
 * 简化 PACS 影像帧服务配置（sql/137）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.medicaltech.exam-image")
public class ExamImageProperties {

    /**
     * 允许上传的影像扩展名（小写，不含点）。非白名单类型一律拒收。
     */
    private Set<String> allowedExt = Set.of("jpg", "jpeg", "png");

    /**
     * 单帧影像大小上限。超过直接拒收（spring.servlet.multipart 还有更外层的总大小闸）。
     */
    private DataSize maxBytes = DataSize.ofMegabytes(10);

    /**
     * 模拟 DICOM 导入时单次最多生成的帧数（UI 传 frameCount 会被夹到该值以内）。
     */
    private int maxFrameCount = 24;

    /**
     * 模拟 DICOM 导入时 UI 未传 frameCount 的默认帧数。
     */
    private int defaultFrameCount = 6;

    /**
     * 仅允许删除本目录下的文件
     */
    private String uploadRootRel = "uploads/examImage";
}

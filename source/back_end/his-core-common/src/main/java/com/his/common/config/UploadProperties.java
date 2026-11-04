package com.his.common.config;

import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 上传产物落盘配置（his.upload.*）：影像帧、导诊单/处方/报告/值班表 PDF 的写入根。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.upload")
public class UploadProperties {

    /**
     * 落盘基准目录（绝对路径）：库里存的相对访问路径 uploads/xxx 相对它解析。随环境变，只在 profile 文件声明。
     */
    private String baseDir;

    @PostConstruct
    public void assertConfigured() {
        if (!TextUtil.hasText(baseDir)) {
            throw new IllegalStateException("未配置 his.upload.base-dir，上传产物没有落盘基准目录（dev 与 pro 各需声明一份）");
        }
    }

    /**
     * 相对访问路径 → 磁盘文件（不保证存在，读取方自行判在不在）。
     */
    public File file(String relativePath) {
        return new File(baseDir, relativePath);
    }

    /**
     * 相对访问路径的所在目录 → 磁盘目录，不存在则建出来（写入前调用）。
     */
    public File mkdirsOf(String relativeDir) {
        File dir = new File(baseDir, relativeDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("上传目录创建失败：" + dir.getAbsolutePath());
        }
        return dir;
    }
}

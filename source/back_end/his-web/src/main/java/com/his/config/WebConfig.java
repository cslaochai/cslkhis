package com.his.config;

import com.his.common.config.UploadProperties;
import com.his.platform.interceptor.OperLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * MVC 层配置：上传产物的静态访问映射 + 全局操作日志拦截器。
 *
 * <p>与 SecurityConfig 的分工：本类只决定「URL 怎么映射到磁盘文件、请求要不要留痕」，
 * 谁能访问由 SecurityConfig 的白名单决定（/uploads/** 在那里匿名放行，故上传产物不校验 token）。
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final OperLogInterceptor operLogInterceptor;
    private final UploadProperties uploadProperties;

    /**
     * 把 his.upload.base-dir 下的 uploads/ 目录挂到 /uploads/** 供前端直接读取
     * （影像帧、导诊单/处方/报告 PDF）。
     *
     * <p>必须给绝对路径并以分隔符结尾：相对写法 Spring 会按 classpath 根解析，
     * 映射不到文件且不报错（静默 404）。目录不存在时也照挂，读取时自然 404。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射目录：必须给绝对路径并以分隔符结尾，相对写法 Spring 按 classpath 根解析，会静默映射不到文件
        File uploadsDir = uploadProperties.file("uploads");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsDir.getAbsolutePath() + File.separator);
    }

    /**
     * 操作日志拦截器：/** 全路径拦截，请求结束把留痕行落 sys_oper_log
     * （请求体由 OperLogCachingFilter 包装后缓存，拦截器本身读不到原始流）。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operLogInterceptor).addPathPatterns("/**");
    }
}

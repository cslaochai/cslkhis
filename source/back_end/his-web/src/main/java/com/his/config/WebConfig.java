package com.his.config;

import com.his.common.config.UploadProperties;
import com.his.platform.interceptor.OperLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final OperLogInterceptor operLogInterceptor;
    private final UploadProperties uploadProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射目录：必须给绝对路径并以分隔符结尾，相对写法 Spring 按 classpath 根解析，会静默映射不到文件
        File uploadsDir = uploadProperties.file("uploads");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsDir.getAbsolutePath() + File.separator);
    }

    /**
     * 操作日志拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operLogInterceptor).addPathPatterns("/**");
    }
}

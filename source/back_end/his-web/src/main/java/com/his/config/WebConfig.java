package com.his.config;

import com.his.system.support.OperLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final OperLogInterceptor operLogInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 uploads 目录为静态资源（使用绝对路径）
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    /**
     * 操作日志拦截器（sql/158）：只记写动作，读接口在拦截器内部写死的名单里挡掉。
     * 注册在 web 模块而不是 his-system —— MVC 配置属于应用装配层，库模块不管 URL 映射。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operLogInterceptor).addPathPatterns("/**");
    }
}

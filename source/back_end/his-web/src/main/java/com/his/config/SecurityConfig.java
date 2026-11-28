package com.his.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.base.Result;
import com.his.platform.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置：无状态 JWT 鉴权（不建 Session，每次请求由 JwtAuthenticationFilter 解析 token）。
 *
 * <p>本类同时是三件事的唯一落点，改动前先看全：
 * <ul>
 *   <li>匿名白名单分两组，前提不同不许混：{@link #LOCAL_WHITE_LIST} 是自有客户端的本地调用入口
 *       （匿名前提 = 接口不敏感），{@link #EXTERNAL_CALLBACK_WHITE_LIST} 是第三方服务器推送的回调
 *       （匿名前提 = 端点内验签）。新加入口先判断自己属于哪一组，两组都不是就别加；</li>
 *   <li>认证失败（401）与越权（403）统一以 {@link Result} 结构写回，HTTP 状态码与 body.code 一致，
 *       前端按这两条分流：401 清 token 回登录，403 只提示、绝不清 token；</li>
 *   <li>@EnableMethodSecurity 打开方法级鉴权，@PreAuthorize 一律标方法、禁标类（类级注解会静默
 *       罩住该类下所有没写自己注解的方法）。</li>
 * </ul>
 *
 * <p>csrf 关闭 + STATELESS：纯 Authorization 头认证，不走 Cookie 会话，CSRF 无从附着。
 * frameOptions 关闭是为前端用 iframe 预览 PDF（导诊单、处方签），默认 DENY 会把预览拦成白屏。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 本地调用入口：调用方是本系统自己的前端/小程序，只是此刻还没有登录态。
     *
     * <p>匿名前提只有一个——「接口本身不返回敏感数据」，不依赖任何签名校验。
     * 所以这组严格自持：新增一条就等于在登录墙上开一个洞，非必要不加，能先登录就绝不挂这里。
     */
    private static final String[] LOCAL_WHITE_LIST = {
            // 登录、注册
            "/auth/login",
            // 登录口令加密公钥（SM2）：登录前必须先匿名拿到它才能把口令加密，
            // 不放行的话登录页拿不到公钥，反而只能退回明文 —— 等于没做。
            "/auth/publicKey",
            "/auth/register",
            // 小程序端患者自助注册（建档并开通账号，匿名可访问，否则新患者无法注册）
            "/patient/register",
            // 注册验证码下发（同上，注册前无 token）
            "/patient/sms/sendCode",
            // 患者端微信一键登录（小程序一期口子；openid 换取后才可能有问题，code 本身匿名可带）
            "/miniapp/auth/wxLogin",

            // 接口文档（仅 Swagger 相关，生产环境按 profile 关掉）
            "/doc.html",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v2/api-docs/**",
            "/webjars/**",

            // 静态资源与上传产物（上传产物路径由 WebConfig 挂载，内容不敏感）
            "/favicon.ico",
            "/static/**",
            "/public/**",
            "/resources/**",
            "/uploads/**",

            // 健康检查
            "/actuator/**",
            "/health",
            "/ping",

            // 错误页（否则异常转发到 /error 会被二次鉴权成 401，掩盖真实错误）
            "/error"
    };

    /**
     * 外部回调入口：调用方是渠道/认证中心服务器，永远不可能持有我方 token。
     *
     * <p>身份由端点内的签名校验证明，不由本白名单证明——放行 ≠ 信任报文，
     * 每个端点仍必须自行验签、查单、金额比对后才能落库。
     * <ul>
     *   <li>{@code /pay/notify/**}：支付宝 RSA2 / 微信 V3 平台证书验签，
     *       伪造通知需要渠道私钥，落库前比对与查单见 MiniPayServiceImpl#confirmByAlipayNotify；</li>
     *   <li>{@code /emr/signCert/caCallback}：先 SignCaCallbackRateLimitFilter 按来源 IP 限次（超出 429），
     *       再 SignCaCallbackSignatureFilter 校验 HMAC-SHA256 + 时间戳 + nonce 去重。
     *       顺序固定为「先计数再验签」，否则验签失败的伪造请求不进分钟桶，限流形同虚设。</li>
     *   <li>{@code /medicaltech/sim/**}：仿真平台结果回调，端点内校验 HMAC-SHA256 签名头
     *       （X-Sim-Timestamp / X-Sim-Nonce / X-Sim-Signature）+ 时间戳偏差 + nonce 去重，密钥取 his.simlab.secret。</li>
     * </ul>
     *
     * <p>本组只允许「第三方服务器主动推送」的端点；需要登录态的业务接口、内部服务间调用一律不许放进来。
     * 写的是去掉 context-path（/api）后的内部路径，写成 /api/pay/notify/** 等于没放行。
     */
    private static final String[] EXTERNAL_CALLBACK_WHITE_LIST = {
            "/pay/notify/**",
            "/emr/signCert/caCallback",
            "/medicaltech/sim/**",
    };
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper;

    /**
     * 过滤器链：两段白名单分别放行，其余一律要求已认证。
     *
     * <p>两段刻意分开：本地匿名入口靠「接口不敏感」兜底，外部回调靠端点内验签兜底，混在一个数组里
     * 会让后来加路径的人分不清自己加的是哪一类前提，也没法按类整体收紧（例如生产环境一次性关掉文档）。
     *
     * <p>JwtAuthenticationFilter 插在 UsernamePasswordAuthenticationFilter 之前（表单登录过滤器
     * 在这里是空位，只用来占位排序）。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(LOCAL_WHITE_LIST).permitAll()
                        .requestMatchers(EXTERNAL_CALLBACK_WHITE_LIST).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJson(response, HttpStatus.UNAUTHORIZED, Result.error(401, "未登录或登录已过期")))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJson(response, HttpStatus.FORBIDDEN, Result.error(403, "没有权限访问")))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable()) // 全局关闭
                );
        return http.build();
    }

    /**
     * 以统一 Result 结构写回错误响应（HTTP 状态码与 body.code 保持一致，前端拦截器按此判断）。
     */
    private void writeJson(HttpServletResponse response, HttpStatus status, Result<?> body) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    /**
     * 口令散列器：全应用「写入口令」和「比对口令」共用的唯一算法。
     *
     * <p>谁在用（两处，缺一不可）：
     * <ul>
     *   <li>业务侧 {@code SysUserServiceImpl} 注入它，新建账号 / 改密时把明文散列后存库；
     *       库里存的是 {@code $2a$10$...} 形式的密文，不是明文也不是 MD5；</li>
     *   <li>鉴权侧 DaoAuthenticationProvider 注入它，登录时把用户输入散列后与库里的比。
     *       它拿的就是这个 Bean —— 没有这个 Bean，登录链根本组不起来。</li>
     * </ul>
     *
     * <p>为什么必须是 BCrypt：自带随机盐（同一个人两次改密得到的密文也不同，彩虹表/批量撞库失效）、
     * 计算刻意慢（默认强度 10，约几十毫秒，把离线爆破成本抬到不划算）。纯 MD5/SHA 无盐且极快，
     * 泄露即等于全部账号被破。
     *
     * <p>禁止在业务代码里自己 {@code new BCryptPasswordEncoder()}：那会让散列与比对分属两个实例，
     * 将来调强度/换算法只需改动一处就会漂移，现象是「密码明明输对了却提示用户名或密码错误」，且极难定位。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器：登录时「校验账号口令」的唯一入口，供业务层主动触发认证。
     *
     * <p>调用方是 {@code AuthServiceImpl}（登录方法里
     * {@code authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password))}），
     * 认证成功返回的 principal 就是 {@link com.his.common.entity.CurrentUser}，后续签发 JWT 用它的身份信息。
     *
     * <p>为什么是「从 config 里取」而不是自己 new：真正干活的是 DaoAuthenticationProvider
     * （用 {@code UserDetailsServiceImpl} 查用户、用上面的 PasswordEncoder 比对散列）。
     * 这层装配由 Spring Security 按容器里已有的 UserDetailsService + PasswordEncoder
     * 自动完成，本方法只是把装好的对象暴露成 Bean。自己 new ProviderManager 就得手工把 Provider 装齐，
     * 漏装一个的表现正是「账号密码都对但登不上」，且没有任何提示。
     *
     * <p>声明 throws：AuthenticationConfiguration 在首次获取时才会初始化内部对象，初始化失败会抛
     * 受检异常（属启动期装配问题，不是业务异常），由 Spring 在创建 Bean 时直接暴露出来。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

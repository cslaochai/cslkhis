package com.his.spi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.spi.dto.TsaStatusUpsertDTO;
import com.his.spi.dto.TsaTimeSourceDTO;
import com.his.spi.dto.TsaTokenPageQueryDTO;
import com.his.spi.dto.TsaTokenVerifyDTO;
import com.his.spi.vo.TsaStampVO;
import com.his.spi.vo.TsaStatusVO;
import com.his.spi.vo.TsaTokenVerifyVO;
import com.his.spi.vo.TsaTokenVO;

import java.time.LocalDateTime;

/**
 * 可信时间戳端口：签名授时与令牌验真的唯一出口，时间来源事实由签名域持有。
 */
public interface TsaStampFacadeService {

    /**
     * 对内容摘要盖一枚可信时间戳并落台账，序列号冲突自动重试。
     *
     * <p>用于病历签名保存时确定签名时刻的来源；返回 null 表示没有可用时间戳服务，调用方降级本机时钟。
     */
    TsaStampVO stamp(String digestHex);

    /**
     * 校验一枚令牌是否由本通道签发，且与摘要、授时时刻逐字对得上。
     *
     * <p>用于签名记录的验签环节与时间戳台账复验；异常一律按校验不通过处理。
     */
    boolean verifyToken(String serial, String digestHex, LocalDateTime tsaTime, String tokenValue);

    /**
     * 时间戳通道是否可用（服务已配置且启用、签发密钥可解出）。
     */
    boolean available();

    /**
     * 通道就绪时的展示名称；未就绪返回 null（页面按「未接入」展示）。
     */
    String readyName();

    /**
     * 最终生效的签名时间来源：配置值与通道在线状态共同决定，全院唯一口径。
     *
     * <p>签名落库的 time_source 与签名概览说明都读这里。
     */
    int effectiveTimeSource();

    /**
     * 时间戳台账总条数。
     */
    long countTokens();

    // ---------- TSA 运维（状态聚合 / 令牌台账 / 启停 / 时间来源 / 复验） ----------

    /**
     * TSA 状态聚合：适配器在线、配置与生效时间来源、令牌台账计数。
     */
    TsaStatusVO status();

    /**
     * 令牌台账分页（只增不改的签发流水）。
     */
    IPage<TsaTokenVO> listTokenPage(TsaTokenPageQueryDTO query);

    /**
     * 启停本地 TSA 服务：停用 = 不再签发新令牌，签名侧立即降级本机时钟；历史令牌凭公钥仍可验证。
     */
    void updateStatus(TsaStatusUpsertDTO dto);

    /**
     * 切换签名时间来源：只允许 1（本机时钟）/ 3（可信时间戳）；2（院内授时）未实现，拒绝。
     */
    void updateTimeSource(TsaTimeSourceDTO dto);

    /**
     * 按台账ID复验一枚令牌：签名值 / 摘要 / 时刻三要素重新对一遍，只读不落库。
     */
    TsaTokenVerifyVO verifyTokenById(TsaTokenVerifyDTO dto);
}

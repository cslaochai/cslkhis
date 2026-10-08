package com.his.common.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.dto.TsaTokenQueryPageDTO;
import com.his.common.vo.TsaStatusVO;
import com.his.common.vo.TsaTokenVO;
import com.his.common.vo.TsaTokenVerifyVO;

/**
 * 可信时间戳（TSA）运维：状态、令牌台账与运维操作（G6b）。
 */
public interface TsaService {

    /**
     * TSA 服务状态（适配器在线情况 / 配置与生效的时间来源 / 台账计数）
     */
    TsaStatusVO status();

    /**
     * 令牌台账分页（只增不改的签发流水）
     */
    IPage<TsaTokenVO> listPage(TsaTokenQueryPageDTO query);

    /**
     * 启停本地 TSA 服务（G6b）。
     * 停用 = 不再签发新令牌，签名侧立即降级本机时钟；历史令牌凭公钥仍可验证。
     *
     * @return 操作后的最新状态（available 立即反映）
     */
    TsaStatusVO updateStatus(Integer tsaStatus);

    /**
     * 切换签名时间来源 sign.time_source（G6b）。
     * 只允许 1（本机时钟）/ 3（可信时间戳）；2（院内授时）未实现，拒绝。
     * 配 3 而适配器不在线时**允许写入**——生效值会自动降为 1 并在 trustNote 说明，
     * 这是既有的「宁可承认不可信」语义，不在接口层另设闸门。
     *
     * @return 操作后的最新状态（configTimeSource / effectiveTimeSource 同屏）
     */
    TsaStatusVO updateTimeSource(Integer timeSource);

    /**
     * 复验台账中的一枚令牌（G6b）：签名值 / 摘要 / 时刻三要素重新对一遍。
     * 只读操作，不产生任何留痕（台账只增不改，复验结果不落库）。
     */
    TsaTokenVerifyVO verifyToken(Long id);
}

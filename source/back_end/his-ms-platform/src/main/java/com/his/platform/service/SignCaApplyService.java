package com.his.platform.service;

import com.his.platform.entity.SysSignCaApply;
import com.his.platform.entity.SysSignCert;
import com.his.platform.support.SignCaApiClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 签名证书 CA 申请流水服务：一次向 CA 发起的申请记一行，提交、审核、签发、回填各阶段的时刻都留痕。
 */
public interface SignCaApplyService {

    /**
     * 登记一次 CA 申请发起：送单前先落库，之后无论 CA 回执还是连不上都回写这一行。
     *
     * <p>用于证书办理页提交在线申请与失败重试两条路径——CA 不可达也要留下「什么时候试过、为什么没成」，
     * 否则线下回填时无从判断该等还是该转人工。
     *
     * @param cert              本次申请对应的证书行
     * @param externalRequestId HIS 侧幂等请求号（CA 按它去重，重提时带时间戳后缀）
     * @param templateCode      本次使用的 CA 证书模板编码
     * @param subjectDn         送 CA 的证书主体 DN
     * @return 新建的申请流水行
     */
    SysSignCaApply start(SysSignCert cert, String externalRequestId, String templateCode, String subjectDn);

    /**
     * 记 CA 回执：申请单号、申请状态与 CA 侧的审核、签发时刻。
     *
     * <p>用于提交与刷新两条路径拿到申请单详情后回写流水。
     *
     * @param caMessage 本次回执对外的说明，列表与详情的「CA 说明」都读这一列
     */
    void recordCaResult(SysSignCaApply apply, SignCaApiClient.CaApplication app, String caMessage);

    /**
     * 记本次申请终结于提交失败：CA 不可达、凭证不符、模板不匹配都在这里落原因。
     */
    void recordFailure(SysSignCaApply apply, String errorMessage);

    /**
     * 记证书回填生效：这一单的流程走完的时刻与签发该证书的 CA 名称。
     *
     * <p>在线签发自动下载与管理员线下回填两条路都走这里，回填时刻以本系统落库为准。
     */
    void markBound(SysSignCaApply apply, String caIssuer);

    /**
     * 按证书取最近一次申请：证书台账的「CA 申请」列、回填证书都按这一条对上号。
     *
     * <p>申请单号 / 状态 / 回执说明的唯一事实在流水行上，证书行不再留副本。
     *
     * @return 最近一次申请流水；该证书从未发起过在线申请时返回 null
     */
    SysSignCaApply latestOf(Long certId);

    /**
     * CA 推送回调定位流水：优先申请单号，找不到再按 HIS 幂等请求号反查。
     */
    SysSignCaApply findByApplyNoOrExternal(String applyNo, String externalRequestId);

    /**
     * 就地更新流水（状态推进、审核/签发时刻与说明）。只允许从推送入口推进状态。
     */
    void update(SysSignCaApply apply);

    /**
     * 批量取各证书最近一次申请（证书ID → 最新流水行）：台账分页每行要展示申请列，
     * 逐行 latestOf 会放大成 N+1，这里一次查询按证书分组取 id 最大行。
     */
    Map<Long, SysSignCaApply> latestOf(Collection<Long> certIds);

    /**
     * 就地更新回执说明：CA 已签发但回填成功 / 失败这类「不改变申请状态」的后续进展，
     * 补记在这一单的说明里，列表与详情读到的还是同一条流水。
     */
    void updateMessage(SysSignCaApply apply, String caMessage);

    /**
     * 按证书取全部申请流水（最近一次在前）：证书详情的「CA 申请记录」时间线。
     * 用于签名证书详情里看这张证书签过几次申请、卡在哪一步。
     */
    List<SysSignCaApply> listByCertId(Long certId);
}

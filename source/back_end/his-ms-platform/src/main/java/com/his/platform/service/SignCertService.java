package com.his.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.platform.dto.SignCertPageQueryDTO;
import com.his.spi.dto.SignCertBindDTO;
import com.his.spi.dto.SignCertKeygenDTO;
import com.his.spi.dto.SignCertRevokeDTO;
import com.his.platform.entity.SysSignCert;
import com.his.platform.vo.SignCertVO;
import com.his.spi.vo.SignCaApplyVO;

import java.util.List;

/**
 * 员工签名证书管理。
 */
public interface SignCertService {

    /**
     * 取员工当前可用证书；没有则（在允许自动签发时）按需签发一张。
     *
     * <p>这是"签名不阻断业务"的关键：如果把"没证书"当成签名失败，
     * 那么每次上新员工都会出现"病历提交不了、医嘱校对不了"。
     */
    SysSignCert ensureActiveCert(Long empId, String empName, Long deptId, String deptName);

    /**
     * 生成签名密钥对并返回「CA 签发申请单」（CA 线下签发第一步）。
     *
     * <p>私钥本地生成并加密托管（不出 HIS），申请单带 Subject DN 与公钥 PEM，供管理员线下送 CA 人工签发。
     */
    SignCaApplyVO keygen(SignCertKeygenDTO dto, Long operatorId, String operatorName);

    /**
     * 回填 CA 线下签发的证书（CA 线下签发第二步）：校验证书公钥与本地密钥对一致后转为有效状态。
     */
    SignCertVO bind(SignCertBindDTO dto, Long operatorId, String operatorName);

    /**
     * 吊销证书：必须写明理由，只改状态不删行——历史签名仍要靠它的公钥验通。
     * 用于「签名证书」签表格行上的吊销动作。
     */
    SignCertVO revoke(SignCertRevokeDTO dto, Long operatorId, String operatorName);

    /**
     * 单张证书详情：含公钥 PEM 与指纹，可核对持有人、算法与有效期。
     * 用于「签名证书」签点开一张证书后的详情弹框。
     */
    SignCertVO getById(Long id);

    /**
     * 按员工取当前有效证书：员工管理「签名设置」弹框只读展示用；无有效证书返回 null。
     */
    SignCertVO getActiveCertVO(Long empId);

    /**
     * 证书台账分页：按持有人、证书状态、算法与有效期等条件筛选。
     * 用于签名证书页签的查询卡表格。
     */
    IPage<SignCertVO> listPage(SignCertPageQueryDTO query);

    /**
     * 下拉：按员工姓名/证书号模糊查有效证书
     */
    List<SignCertVO> selectList(String keyword);

    /**
     * 证书私钥（解密后 PEM）。**仅供签名服务内部使用**，不对外暴露端点
     */
    String privatePemOf(SysSignCert cert);

    /**
     * 软签签名：校验签名人 PIN → 解密私钥 → 签名。PIN 错误一律拒绝，不解密私钥。
     */
    String signWithPin(SysSignCert cert, String content, String pin);

    /**
     * 有效证书总数 / 自动签发数等统计给概览用
     */
    long countByStatus(Integer certStatus);

    /**
     * 自动签发的证书张数：即签名时按需补发的那一批，非人工签发。
     * 用于签名概览卡片区，衡量证书是人工铺底还是靠按需签发兜住。
     */
    long countAutoIssued();

    /**
     * 已有有效证书的员工数
     */
    long countActiveEmployees();
}

package com.his.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.spi.dto.SignCertIssueDTO;
import com.his.platform.dto.SignCertPageQueryDTO;
import com.his.spi.dto.SignCertRevokeDTO;
import com.his.platform.entity.SysSignCert;
import com.his.platform.vo.SignCertVO;

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
     * 人工签发（已有有效证书时拒绝，必须先吊销）
     */
    SignCertVO issue(SignCertIssueDTO dto, Long operatorId, String operatorName);

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
     * 证书台账分页：按持有人、证书状态、算法与有效期等条件筛选。
     * 用于签名中心「签名证书」签的查询卡表格。
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

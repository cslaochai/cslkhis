package com.his.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.system.dto.SignCertIssueDTO;
import com.his.system.dto.SignCertQueryPageDTO;
import com.his.system.dto.SignCertRevokeDTO;
import com.his.system.entity.SysSignCert;
import com.his.system.vo.SignCertVO;

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

    SignCertVO revoke(SignCertRevokeDTO dto, Long operatorId, String operatorName);

    SignCertVO getById(Long id);

    IPage<SignCertVO> listPage(SignCertQueryPageDTO query);

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

    long countAutoIssued();

    /**
     * 已有有效证书的员工数
     */
    long countActiveEmployees();
}

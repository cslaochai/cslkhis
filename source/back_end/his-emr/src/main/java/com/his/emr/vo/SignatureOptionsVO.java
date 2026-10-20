package com.his.emr.vo;

import lombok.Data;

import java.util.List;
import com.his.sign.vo.SignOptionVO;

/**
 * 签名中心的下拉选项（全部来自枚举，不查库）。
 */
@Data
public class SignatureOptionsVO {

    /** 业务类型下拉项：签名可挂的业务对象（病历/医嘱等） */
    private List<SignOptionVO> bizTypes;
    /** 签名场景下拉项：医生签名/审核签名/患者知情等 */
    private List<SignOptionVO> scenes;
    /** 签名状态下拉项：签名记录自身的状态筛选条件 */
    private List<SignOptionVO> signStatuses;
    /** 验签结果下拉项：最近一次校验的结论筛选条件 */
    private List<SignOptionVO> verifyStatuses;
    /** 时间来源下拉项：签名时刻由本机时钟/授时服务器/TSA 出具 */
    private List<SignOptionVO> timeSources;
    /** 证书状态下拉项：数字证书有效与吊销情况 */
    private List<SignOptionVO> certStatuses;
    /** 签发方式下拉项：证书由人工签发还是系统自动签发 */
    private List<SignOptionVO> issuedModes;
}

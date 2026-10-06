package com.his.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 签名中心的下拉选项（全部来自枚举，不查库）。
 */
@Data
public class SignatureOptionsVO {

    private List<SignOptionVO> bizTypes;
    private List<SignOptionVO> scenes;
    private List<SignOptionVO> signStatuses;
    private List<SignOptionVO> verifyStatuses;
    private List<SignOptionVO> timeSources;
    private List<SignOptionVO> certStatuses;
    private List<SignOptionVO> issuedModes;
}

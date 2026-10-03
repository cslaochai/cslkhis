package com.his.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 签名中心的下拉选项（全部来自枚举，不查库）。
 *
 * <p>用对象而不是 {@code Map<String, Object>}：Map 出参没有字段契约，
 * 前端拼错 key 不会报错、只会静默拿到 undefined（这个项目里踩过一次）。
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

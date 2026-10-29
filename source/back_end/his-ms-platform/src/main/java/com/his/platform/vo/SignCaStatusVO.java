package com.his.platform.vo;

import lombok.Data;

import java.io.Serializable;

/** 签名 CA 通道状态：证书签发走演示模式还是远程 CA（csssl）的实时自检回参。 */
@Data
public class SignCaStatusVO implements Serializable {

    /** CA 交互方式（offline-线下人工：HIS 生成密钥对、申请单线下送 CA、回填证书） */
    private String caMode;

    /** 是否可用：线下人工模式下恒为 true（不依赖任何 CA 接口连通性） */
    private Boolean available;

    /** 当前签发行为提示文案（信任根与线下流程口径） */
    private String hint;
}

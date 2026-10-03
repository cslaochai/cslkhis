package com.his.emr.vo;

import com.his.common.vo.SignCertVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 证书下拉出参，字段口径同 {@link SignCertVO}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SignCertSelectListVO extends SignCertVO {
}

package com.his.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色下拉出参，字段口径同 {@link RoleVO}（角色字典只有编码/名称/状态在用，不单独裁剪）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleSelectListVO extends RoleVO {
}

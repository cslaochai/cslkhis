package com.his.common.support;

public interface ValidateParams {

    /**
     * 如果采用spring验证方法都兜不住的逻辑，请将逻辑写到这个里面，特别是有很多参数需要嵌套判断的
     * 例如当只有满足 字段a =1 的时候才判断b是否为空的判断
     */
    void validateParams();
}

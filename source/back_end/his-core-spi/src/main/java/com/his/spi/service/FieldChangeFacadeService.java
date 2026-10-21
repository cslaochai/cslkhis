package com.his.spi.service;

import com.his.common.support.FieldSpec;

import java.util.List;

/**
 * 字段级修改日志窄端口：跨模块对主档等对象做变更前后快照 diff 落库。
 * 数据宿主为系统域（字段变更日志表 sql/159），调用方只声明要记哪些字段。
 */
public interface FieldChangeFacadeService {

    /**
     * 比对两个对象快照并落库。
     *
     * @param bizType 业务类型（PATIENT / EMPLOYEE …）
     * @param bizId   业务主键
     * @param bizNo   业务单号（可空）
     * @param bizName 业务名称（可空）
     * @param oldObj  变更前快照（建档传 null）
     * @param newObj  变更后快照（从库里重新查出来的真实值，不是 DTO）
     * @param specs   要记哪些字段，由调用方声明
     * @return 批次号；没有任何字段发生变化时返回 null
     */
    String record(String bizType, Object bizId, String bizNo, String bizName,
                  Object oldObj, Object newObj, List<FieldSpec> specs);
}

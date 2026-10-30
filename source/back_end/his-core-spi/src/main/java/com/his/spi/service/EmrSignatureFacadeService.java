package com.his.spi.service;

import com.his.spi.dto.SignCommandDTO;
import com.his.spi.vo.SignatureVO;

/**
 * 电子签名端口：文书落定后的「签」与「作废」两个动作，供临床各域在写完自己的业务行后调用。
 *
 * <p>签名事实（谁签的、签了什么内容快照、用了哪张证书）由病历域持有，本端口只承诺发起签名与作废，
 * 不承诺签名记录的查询与统计——那些是签名验签自己的页面口径。
 */
public interface EmrSignatureFacadeService {

    /**
     * 对一份业务对象发起签名。
     *
     * <p>用于检查/检验报告发布、住院医嘱签发、病危通知与交接班闭环等「写完即签」的场景，
     * 签名内容与被签对象类型由 {@link SignCommandDTO} 描述。
     *
     * @param command 签名指令
     * @return 生成的签名行
     */
    SignatureVO sign(SignCommandDTO command);

    /**
     * 作废一条签名：不改历史行，只追加作废信息与理由。
     *
     * <p>用于住院医嘱修订/撤回时把原签名标废，保留「谁在何时撤了谁的签」的证据链。
     *
     * @param signId       待作废的签名ID
     * @param reason       作废理由
     * @param operatorId   操作人员工ID
     * @param operatorName 操作人姓名
     * @return 作废后的签名行
     */
    SignatureVO invalidate(Long signId, String reason, Long operatorId, String operatorName);
}

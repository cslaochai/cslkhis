package com.his.platform.service;

import com.his.platform.vo.EmployeePostVO;
import com.his.platform.vo.SignQualReviewVO;

import java.util.List;

/**
 * 签名资格复核：人事异动后判断证书该保留还是吊销，把决定权收在医务科。
 */
public interface SignQualReviewService {

    /**
     * 岗位整体替换后调用：仅当「是否需要电子签名」发生翻转时才生成复核单/通知。
     *
     * @param before 异动前仍在生效的岗位
     * @param after  异动后仍在生效的岗位
     */
    void handlePostsChanged(Long employeeId, String empName, List<EmployeePostVO> before, List<EmployeePostVO> after);

    /**
     * 待复核单列表（按创建时间倒序）。
     */
    List<SignQualReviewVO> listPending();

    /**
     * 处理复核单：1-保留证书（不动）；2-吊销证书（走业务吊销，自动通知信息科重办）。
     */
    void handle(Long reviewId, Integer action, String remark);
}

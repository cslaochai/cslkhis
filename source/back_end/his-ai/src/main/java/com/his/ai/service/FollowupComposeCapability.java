package com.his.ai.service;

import com.his.ai.dto.FollowupComposeDTO;
import com.his.ai.vo.FollowupComposeVO;

/**
 * 随访话术草拟能力（G-06）
 */
public interface FollowupComposeCapability {

    FollowupComposeVO execute(FollowupComposeDTO dto);
}

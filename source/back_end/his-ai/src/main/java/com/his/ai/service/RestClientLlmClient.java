package com.his.ai.service;

import com.his.ai.dto.AiChatRequestDTO;
import com.his.ai.dto.LlmResultDTO;

public interface RestClientLlmClient extends LlmClient {

    LlmResultDTO complete(AiChatRequestDTO request, int timeoutMs);
}

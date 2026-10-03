package com.his.common.service;

import java.time.LocalDateTime;

public interface TsaChannelService {

    public static final String TSA_CODE = "LOCAL";

    /** 一次盖章的结果（三列缺一不可：签名行 time_source=3 时都要落库） */
    public record Stamp(String serial, LocalDateTime tsaTime, String tokenValue) {
    }

    String name();

    boolean available();

    String readyName();

    Stamp stamp(String digestHex);

    boolean verifyToken(String serial, String digestHex, LocalDateTime tsaTime, String tokenValue);

    void invalidate();
}

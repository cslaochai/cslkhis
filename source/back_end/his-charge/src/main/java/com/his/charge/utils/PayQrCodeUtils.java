package com.his.charge.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.Base64;

/**
 * 支付二维码工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PayQrCodeUtils {

    private static final String DATA_URI_PREFIX = "data:image/png;base64,";

    /**
     * 生成支付二维码（Base64 DataURI，可直接用于 &lt;img src&gt;）
     *
     * @param content 二维码内容
     * @param size    边长（像素）
     * @return data:image/png;base64,... ；生成失败返回 null
     */
    public static String generateDataUri(String content, int size) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            QrConfig config = new QrConfig(size, size);
            config.setMargin(1);
            BufferedImage image = QrCodeUtil.generate(content, config);
            return DATA_URI_PREFIX + Base64.getEncoder().encodeToString(ImgUtil.toBytes(image, ImgUtil.IMAGE_TYPE_PNG));
        } catch (Exception e) {
            log.error("生成支付二维码失败, content={}", content, e);
            return null;
        }
    }

    /**
     * 组装支付二维码内容（小程序支付入口）
     */
    public static String buildMiniappPayUrl(Long billId) {
        return "http://localhost:5173/miniapp/pay?billId=" + (billId == null ? "" : billId);
    }
}

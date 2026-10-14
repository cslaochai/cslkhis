package com.mis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 医保结算经办平台业务参数（application.yml 中 jinb.* ）
 */
@Data
@ConfigurationProperties(prefix = "jinb")
public class MisProperties {

    /** 准入的定点医疗机构 */
    private Medins medins = new Medins();

    /** 月度智能审核规则 */
    private Audit audit = new Audit();

    /** 2304 即时核定政策 */
    private Policy policy = new Policy();

    @Data
    public static class Medins {
        private String code;
        private String name;
        private String appSecret;
    }

    @Data
    public static class Audit {
        /** 质量保证金预留比例（占申报统筹） */
        private BigDecimal reserveRatio = new BigDecimal("0.05");
        /** 结算单号末位命中即判违规的数字 */
        private List<Integer> refuseTailDigits = new ArrayList<>();
        /** 命中单据的扣款比例（占该单统筹，1.0=全额拒付） */
        private BigDecimal refuseRatio = BigDecimal.ONE;
    }

    @Data
    public static class Policy {
        private Scheme employee = new Scheme();
        private Scheme resident = new Scheme();
    }

    @Data
    public static class Scheme {
        private Rate outpatient = new Rate();
        private Rate inpatient = new Rate();
    }

    @Data
    public static class Rate {
        /** 起付线 */
        private BigDecimal deductible = BigDecimal.ZERO;
        /** 报销比例 */
        private BigDecimal ratio = BigDecimal.ZERO;
    }
}

package com.his.common.support;

import com.his.common.enums.ClinicDiagnosisTierEnum;

import java.math.BigDecimal;

/**
 * 员工职称 → 门诊诊查费职级加收档位的映射口径。
 *
 * <p><b>为什么不能只看字典文案</b>：前端老逻辑 {@code scheduleTitle.js} 用
 * {@code label.includes('副主任')} / {@code label.startsWith('主任')} 判断是否专家号，
 * 结果「副主任医师」和「主任医师」<b>都</b>落进同一个专家档。dev 库里 63 名副主任医师
 * 和 57 名主任医师的号源全部按 70 元挂，与湘医保发〔2025〕58号的四档价格表不符
 * （副主任应 22 元、主任应 31 元）。
 *
 * <p><b>本类的口径是「职称码白名单」，不是文案猜测</b>：
 * <ul>
 *   <li>{@code sys_employee.title} 取值来自字典 {@code sys_hospital_title}，码值是稳定契约
 *       （101医士 / 102医师 / 201主治（主管）医师 / 301副主任医师 / 401主任医师 …）；
 *       文案可能因字典维护而改，码值不会。</li>
 *   <li>只有<b>医生序列</b>才有诊查费加收。药师（1xx/2xx/3xx/4xx 里的药师/护师/技师）
 *       虽然也是高级职称，但挂的是药事/护理/医技号源，不出「门诊诊查费」这一行，
 *       套诊查费加收就是错账。</li>
 * </ul>
 *
 * <p><b>关于「知名专家」</b>：它<b>不是职称</b>，是医院对主任医师的技术认定（每期名单制，
 * 会动态调整），字典里没有这一码。所以知名专家档只能由排班人员在号源上手工指定，
 * 本类不从职称自动推导。这是有意的：让系统按职称猜「谁是知名专家」，
 * 等于把定价权交给一份永远过期的名单。
 */
public final class TitleTierMapping {

    /** 主治（主管）医师及以上的主治医师档位码 */
    public static final String TITLE_ATTENDING = "201";
    /** 副主任医师 */
    public static final String TITLE_DEPUTY_CHIEF = "301";
    /** 主任医师 */
    public static final String TITLE_CHIEF = "401";

    private TitleTierMapping() {
    }

    /**
     * 按员工职称推导默认的诊查费档位。
     *
     * <p>🔑 <b>只对医生序列返回非普通档</b>：副主任药师(302)、副主任护师(303)、副主任技师(304)
     * 这些码在字典里排布上紧邻副主任医师，<b>不能</b>用 {@code startsWith("3")} 之类的前缀匹配——
     * 那会把药事、护理、医技人员全部算成「副主任医师加收」。
     *
     * @param employeeTitle {@code sys_employee.title}，可为空
     * @return 301→副主任档、401→主任档；其余（含 null）→普通档
     */
    public static ClinicDiagnosisTierEnum tierOf(String employeeTitle) {
        if (TITLE_DEPUTY_CHIEF.equals(employeeTitle)) {
            return ClinicDiagnosisTierEnum.DEPUTY_CHIEF;
        }
        if (TITLE_CHIEF.equals(employeeTitle)) {
            return ClinicDiagnosisTierEnum.CHIEF;
        }
        return ClinicDiagnosisTierEnum.GENERAL;
    }

    /**
     * 该职称是否<b>可能</b>适用专家档（知名专家）。
     *
     * <p>只作提示用：前端在主任医师档时把「知名专家」这个选项放出来，
     * 但不自动勾选——知名专家是名单制认定，不能由职称推导。
     */
    public static boolean mayBeExpert(String employeeTitle) {
        return TITLE_CHIEF.equals(employeeTitle);
    }

    /**
     * 该档位的全额价格（普通 9 / 副主任 22 / 主任 31 / 知名专家 70）。
     *
     * <p>给号源新增/编辑页做默认值用。<b>仅为默认值，最终价仍落在号源的 {@code diagnosisFee}</b>：
     * 医生升职称后已排出的号源价格不变（价格是号源快照，不是实时推导）。
     */
    public static BigDecimal defaultAmountOf(String employeeTitle) {
        return new BigDecimal(tierOf(employeeTitle).getAmount());
    }
}

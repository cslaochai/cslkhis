package com.his.iot.support;

/**
 * 机器人调度口径常量（技术阈值，不是码值，不枚举化）。
 */
public final class RobotDispatchConst {

    /** 单车额定载重(kg)：仿真 4 箱 × 3.6kg ≈ 14.4kg，留 15kg 余量。 */
    public static final double NOMINAL_PAYLOAD_KG = 15.0d;

    private RobotDispatchConst() {
    }
}

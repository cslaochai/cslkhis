package com.his.iot.support;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备类型 → 时序库超级表 + 字段映射。整条链路唯一的「协议翻译表」。
 * <p>
 * MQTT 报文是厂商/模拟器的 camelCase 字段，TDengine 列是 snake_case，两边对齐只在这一处维护；
 * 换设备厂商时改这张表，不动订阅、落库、查询逻辑。
 * <p>
 * 字段同时带上中文名与单位：查询接口直接把它们拼进出参，前端不用再维护第二份字典。
 */
public final class DeviceTypeSchema {

    /**
     * 列类型。只区分落库时的转义方式，与查询无关
     */
    public enum Kind { DOUBLE, INT, BOOL, STRING }

    /**
     * 一个指标列：时序列名、报文键名、类型、中文名、单位
     */
    public record Field(String column, String key, Kind kind, String label, String unit) {
    }

    /**
     * 一类设备：超级表名 + 指标列清单
     */
    public record Schema(String deviceType, String label, String stable, List<Field> fields) {
    }

    private static final Map<String, Schema> REGISTRY = new LinkedHashMap<>();

    private static void reg(String type, String label, String stable, Field... fields) {
        REGISTRY.put(type, new Schema(type, label, stable, List.of(fields)));
    }

    private static Field d(String col, String key, String label, String unit) {
        return new Field(col, key, Kind.DOUBLE, label, unit);
    }

    private static Field i(String col, String key, String label, String unit) {
        return new Field(col, key, Kind.INT, label, unit);
    }

    private static Field b(String col, String key, String label, String unit) {
        return new Field(col, key, Kind.BOOL, label, unit);
    }

    private static Field s(String col, String key, String label, String unit) {
        return new Field(col, key, Kind.STRING, label, unit);
    }

    static {
        reg("coldchain", "冷链冰箱", "st_coldchain",
                d("temp", "temp", "温度", "℃"), d("humidity", "humidity", "湿度", "%"),
                b("door", "door", "门磁", ""), b("power", "power", "供电", ""));
        reg("infusion", "输液泵", "st_infusion",
                d("rate", "rate", "滴速", "滴/分"), d("remaining_ml", "remainingMl", "余量", "ml"),
                i("battery", "battery", "电量", "%"), b("occluded", "occluded", "管路阻塞", ""));
        reg("asset", "资产定位标签", "st_asset",
                s("area", "area", "所在区域", ""), i("rssi", "rssi", "信号强度", "dBm"),
                i("battery", "battery", "电量", "%"));
        reg("env", "环境监测", "st_env",
                d("temp", "temp", "温度", "℃"), d("humidity", "humidity", "湿度", "%"),
                d("pressure_pa", "pressurePa", "压差", "Pa"), b("door", "door", "门磁", ""),
                d("air_velocity", "airVelocity", "风速", "m/s"));
        reg("gas", "医用气体", "st_gas",
                d("o2_kpa", "o2Kpa", "氧气压力", "kPa"), d("vacuum_kpa", "vacuumKpa", "负压", "kPa"),
                d("air_kpa", "airKpa", "压缩空气", "kPa"), d("flow_lpm", "flowLpm", "流量", "L/min"));
        reg("bed", "病床监测", "st_bed",
                b("in_bed", "inBed", "在床", ""), i("heart_rate", "heartRate", "心率", "bpm"),
                i("resp_rate", "respRate", "呼吸", "次/分"), i("turns", "turns", "翻身次数", "次"),
                d("body_movement", "bodyMovement", "体动", ""));
        reg("monitor", "床旁监护仪", "st_monitor",
                i("heart_rate", "heartRate", "心率", "bpm"),
                i("spo2", "spo2", "血氧", "%"),
                i("sbp", "sbp", "收缩压", "mmHg"),
                i("dbp", "dbp", "舒张压", "mmHg"),
                i("resp_rate", "respRate", "呼吸", "次/分"),
                d("temp_c", "tempC", "体温", "℃"),
                s("rhythm", "rhythm", "心律判读", ""),
                s("rhythm_label", "rhythmLabel", "心律", ""),
                s("resp_pattern", "respPattern", "呼吸模式", ""));
        reg("dialysis", "透析机", "st_dialysis",
                i("blood_flow_ml_min", "bloodFlowMlMin", "血流量", "ml/min"),
                d("dialysate_temp_c", "dialysateTempC", "透析液温度", "℃"),
                d("conductivity_ms_cm", "conductivityMsCm", "电导率", "mS/cm"),
                i("tmp_mmhg", "tmpMmHg", "跨膜压", "mmHg"),
                i("elapsed_min", "elapsedMin", "已运行", "min"),
                i("remaining_min", "remainingMin", "剩余", "min"),
                i("ultrafiltration_ml", "ultrafiltrationMl", "超滤量", "ml"));
        reg("cabinet", "智能药柜", "st_cabinet",
                d("temp", "temp", "温度", "℃"), d("humidity", "humidity", "湿度", "%"),
                b("door", "door", "柜门", ""), i("stock_pct", "stockPct", "库存率", "%"));
        reg("uv", "紫外线消毒", "st_uv",
                b("running", "running", "运行中", ""), d("lamp_hours", "lampHours", "灯管累计", "h"),
                i("filter_pct", "filterPct", "滤网余量", "%"), b("occupied", "occupied", "有人占用", ""));
        reg("robot", "物流机器人", "st_robot",
                s("task_state", "state", "任务状态", ""), s("location", "location", "位置", ""),
                i("battery", "battery", "电量", "%"), d("load_kg", "loadKg", "载重", "kg"),
                d("odometer_m", "odometerM", "里程", "m"));
    }

    private DeviceTypeSchema() {
    }

    /**
     * 按设备类型取映射，未知类型返回 null（落库侧据此跳过并计数）
     */
    public static Schema of(String deviceType) {
        return deviceType == null ? null : REGISTRY.get(deviceType);
    }

    /**
     * 全部设备类型，顺序固定（冷链/输液在前，与看板排列一致）
     */
    public static Collection<Schema> all() {
        return REGISTRY.values();
    }

    public static int size() {
        return REGISTRY.size();
    }

    /**
     * 子表名规则与建表脚本一致：设备编号小写、非字母数字一律换下划线。PUMP-01 → pump_01
     */
    public static String childTable(String deviceNo) {
        return deviceNo.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }
}

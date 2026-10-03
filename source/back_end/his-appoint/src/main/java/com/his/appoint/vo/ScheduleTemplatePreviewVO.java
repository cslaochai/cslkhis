package com.his.appoint.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 按模板生成排班——预览结果（dryRun，不落库）
 */
@Data
public class ScheduleTemplatePreviewVO {

    private String weekStart;

    private String weekEnd;

    /**
     * 将生成的排班描述
     */
    private List<String> willCreate = new ArrayList<>();

    /**
     * 跳过：该周同班次已有排班
     */
    private List<String> skipExist = new ArrayList<>();

    /**
     * 跳过：时间冲突
     */
    private List<String> skipOverlap = new ArrayList<>();

    /**
     * 跳过：单双周不匹配
     */
    private List<String> skipParity = new ArrayList<>();

    /**
     * 跳过：不在生效日期范围内
     */
    private List<String> skipExpired = new ArrayList<>();

    /**
     * 跳过：目标日期已过（不能往已就诊完的历史日期上造班次）
     */
    private List<String> skipPast = new ArrayList<>();

    /**
     * 跳过：模板没选班次、或所选班次已被删除（时间段带不出来）
     */
    private List<String> skipInvalid = new ArrayList<>();

    /**
     * 涉及医生名单（供排班员人工核对出诊人，医生停用数据源不可靠，不自动过滤）
     */
    private List<String> doctorNames = new ArrayList<>();

    public int total() {
        return willCreate.size() + skipExist.size() + skipOverlap.size() + skipParity.size() + skipExpired.size()
                + skipPast.size() + skipInvalid.size();
    }
}

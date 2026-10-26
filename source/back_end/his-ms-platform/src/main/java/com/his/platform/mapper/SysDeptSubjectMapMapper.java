package com.his.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.platform.entity.SysDeptSubjectMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 科室-诊疗科目映射 Mapper。
 */
@Mapper
public interface SysDeptSubjectMapMapper extends BaseMapper<SysDeptSubjectMap> {

    /**
     * 每个科目被多少科室引用。用于科目树展示「已映射 N 个科室」，一次查完避免按科目逐条 count。
     *
     * <p>返回 Map 而不是 VO：只有两列且仅内部消费，alias 已写成驼峰，Map 不受
     * map-underscore-to-camel-case 影响。
     */
    @Select("SELECT subject_code AS subjectCode, COUNT(*) AS deptCount "
            + "FROM sys_dept_subject_map GROUP BY subject_code")
    List<Map<String, Object>> selectDeptCountGroupBySubject();

    /**
     * 某科目下的科室ID清单（不含名称，名称由 service 侧批量补齐）。
     */
    @Select("SELECT dept_id FROM sys_dept_subject_map WHERE subject_code = #{subjectCode} AND is_primary = 1")
    List<Long> selectPrimaryDeptIdsBySubject(@Param("subjectCode") String subjectCode);
}

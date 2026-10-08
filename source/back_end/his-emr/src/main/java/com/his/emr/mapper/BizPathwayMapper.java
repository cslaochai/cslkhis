package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.emr.entity.BizPathway;
import com.his.emr.vo.PathwayVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 临床路径模板 Mapper。
 */
@Mapper
public interface BizPathwayMapper extends BaseMapper<BizPathway> {

    /**
     * 模板分页（关键字模糊编码/名称/诊断，状态/科室过滤；排序补 id 二级键防同秒行序抖动）
     */
    @Select("""
            <script>
            SELECT p.*
              FROM biz_pathway p
             WHERE p.del_flag = 0
               <if test="keyword != null and keyword != ''">
                 AND (p.pathway_code LIKE CONCAT('%', #{keyword}, '%')
                   OR p.pathway_name LIKE CONCAT('%', #{keyword}, '%')
                   OR p.diagnosis LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="status != null"> AND p.status = #{status}</if>
               <if test="deptId != null"> AND p.dept_id = #{deptId}</if>
             ORDER BY p.status ASC, p.id DESC
            </script>
            """)
    List<PathwayVO> selectPathwayPage(IPage<PathwayVO> page,
                                      @Param("keyword") String keyword,
                                      @Param("status") Integer status,
                                      @Param("deptId") Long deptId);

    @Select("SELECT p.* FROM biz_pathway p WHERE p.id = #{id} AND p.del_flag = 0")
    PathwayVO selectPathwayById(@Param("id") Long id);

    /**
     * 使用中模板下拉（入径选择用；可按科室过滤，不传则全院）
     */
    @Select("""
            <script>
            SELECT p.* FROM biz_pathway p
             WHERE p.del_flag = 0 AND p.status = 2
               <if test="deptId != null"> AND p.dept_id = #{deptId}</if>
             ORDER BY p.pathway_code ASC, p.version DESC
            </script>
            """)
    List<PathwayVO> selectActiveList(@Param("deptId") Long deptId);

    /**
     * 同编码是否已有别的「使用中」模板（发布前校验；excludeId 为自己）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_pathway
             WHERE del_flag = 0 AND status = 2 AND pathway_code = #{code} AND id <> #{excludeId}
            """)
    int countOtherActiveByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    /**
     * 科室名快照（科室跨模块裸 SQL 取，取不到返回 NULL 不编造）
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);
}

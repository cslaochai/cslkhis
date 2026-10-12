package com.his.ai.service;

import com.his.ai.dto.OperationQaColumnQueryPageDTO;
import com.his.ai.dto.OperationQaColumnUpsertDTO;
import com.his.ai.dto.OperationQaSchemaQueryPageDTO;
import com.his.ai.dto.OperationQaSchemaUpsertDTO;
import com.his.ai.dto.OperationQaSemanticQueryPageDTO;
import com.his.ai.dto.OperationQaSemanticUpsertDTO;
import com.his.ai.support.OperationSchemaCatalog;
import com.his.ai.vo.OperationQaColumnListVO;
import com.his.ai.vo.OperationQaSchemaListVO;
import com.his.ai.vo.OperationQaSchemaSelectListVO;
import com.his.ai.vo.OperationQaSemanticListVO;
import com.his.common.base.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 运营问数配置管理（schema/column/semantic 维护 + 运行时按需查询 + 重写刷新）。
 *
 * <p>配置表是唯一事实来源：运行时不加载内存快照，问数链路按需查库；
 * 表结构同步只走「重写刷新」（扫描当前库全表结构，一次性覆盖表/列配置）。
 */
public interface OperationQaConfigService {

    // ---------------- 配置维护 CRUD ----------------

    /**
     * 数据表配置分页查询
     */
    PageResult<OperationQaSchemaListVO> listSchemaPage(OperationQaSchemaQueryPageDTO dto);

    /**
     * 数据表配置新增/修改
     */
    void upsertSchema(OperationQaSchemaUpsertDTO dto);

    /**
     * 删除数据表配置（级联物理删该表全部列配置）
     */
    void deleteSchemaById(Long id);

    /**
     * 数据列配置分页查询（按表）
     */
    PageResult<OperationQaColumnListVO> listColumnPage(OperationQaColumnQueryPageDTO dto);

    /**
     * 数据列配置新增/修改
     */
    void upsertColumn(OperationQaColumnUpsertDTO dto);

    /**
     * 删除数据列配置
     */
    void deleteColumnById(Long id);

    /**
     * 语义映射分页查询
     */
    PageResult<OperationQaSemanticListVO> listSemanticPage(OperationQaSemanticQueryPageDTO dto);

    /**
     * 语义映射新增/修改
     */
    void upsertSemantic(OperationQaSemanticUpsertDTO dto);

    /**
     * 删除语义映射
     */
    void deleteSemanticById(Long id);

    /**
     * 启用表下拉选项（配置弹框选表用，仅启用表）
     */
    List<OperationQaSchemaSelectListVO> schemaSelectList();

    // ---------------- 运行时按需查询（问数链路用，直接查库） ----------------

    /**
     * 启用表名集合（白名单）。配置为空时返回空集（fail-closed）。
     */
    Set<String> enabledTableNames();

    /**
     * 启用表的表定义（含启用列）。tableNames 为空 = 全部启用表。
     */
    List<OperationSchemaCatalog.TableDef> tableDefs(Collection<String> tableNames);

    /**
     * 启用语义映射（按优先级降序）。
     */
    List<OperationSchemaCatalog.SemanticDef> semanticDefs();

    /**
     * 启用表下的敏感列配置（脱敏/禁止出库），问数链路按此约束提示词与出参。
     */
    List<OperationSchemaCatalog.SensitiveDef> sensitiveDefs();

    // ---------------- 重写刷新 ----------------

    /**
     * 重写刷新：扫描当前库所有表结构（information_schema），一次性覆盖现有
     * 表/列配置。表级对齐全库表（新增表默认停用，保留既有启用状态与用途说明）；
     * 列级对齐真实 DDL（新增列默认启用，更新注释/类型，删除库中已不存在的列）。
     * 这是表结构同步的唯一入口 —— 日常改表结构不会自动同步到配置。
     *
     * @return 重写结果摘要（扫描表数/写入列数/新增/更新/删除）
     */
    String rewriteSchema();
}

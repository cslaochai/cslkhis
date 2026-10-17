package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.ai.dto.OperationQaColumnQueryPageDTO;
import com.his.ai.dto.OperationQaColumnUpsertDTO;
import com.his.ai.dto.OperationQaRelationQueryPageDTO;
import com.his.ai.dto.OperationQaRelationUpsertDTO;
import com.his.ai.dto.OperationQaSchemaQueryPageDTO;
import com.his.ai.dto.OperationQaSchemaUpsertDTO;
import com.his.ai.dto.OperationQaSemanticQueryPageDTO;
import com.his.ai.dto.OperationQaSemanticUpsertDTO;
import com.his.ai.entity.SysAiQaColumn;
import com.his.ai.entity.SysAiQaRelation;
import com.his.ai.entity.SysAiQaSchema;
import com.his.ai.entity.SysAiQaSemantic;
import com.his.ai.mapper.SysAiQaColumnMapper;
import com.his.ai.mapper.SysAiQaRelationMapper;
import com.his.ai.mapper.SysAiQaSchemaMapper;
import com.his.ai.mapper.SysAiQaSemanticMapper;
import com.his.ai.service.OperationQaConfigService;
import com.his.ai.support.OperationSchemaCatalog;
import com.his.ai.vo.OperationQaColumnListVO;
import com.his.ai.vo.OperationQaRelationListVO;
import com.his.ai.vo.OperationQaSchemaListVO;
import com.his.ai.vo.OperationQaSchemaSelectListVO;
import com.his.ai.vo.OperationQaSemanticListVO;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.ai.vo.OperationQaColumnSelectListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 运营问数配置管理：配置 CRUD + 运行时按需查询 + 重写刷新。
 *
 * <p>运行时不加载内存快照：问数链路直接查库（{@link #enabledTableNames}/{@link #tableDefs}/
 * {@link #semanticDefs}）。表结构同步唯一入口是 {@link #rewriteSchema()} —— 扫描
 * information_schema 全库表结构一次性覆盖表/列配置；日常改表结构不会自动同步。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationQaConfigServiceImpl
        extends ServiceImpl<SysAiQaSchemaMapper, SysAiQaSchema> implements OperationQaConfigService {

    private final SysAiQaSchemaMapper schemaMapper;

    private final SysAiQaColumnMapper columnMapper;

    private final SysAiQaSemanticMapper semanticMapper;

    private final SysAiQaRelationMapper relationMapper;

    private final DataSource dataSource;

    // ---------------- schema（表级） ----------------

    @Override
    public PageResult<OperationQaSchemaListVO> listSchemaPage(OperationQaSchemaQueryPageDTO dto) {
        LambdaQueryWrapper<SysAiQaSchema> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(dto.getTableName()), SysAiQaSchema::getTableName, dto.getTableName())
                .eq(dto.getIsEnabled() != null, SysAiQaSchema::getIsEnabled, dto.getIsEnabled())
                .orderByAsc(SysAiQaSchema::getSortOrder);

        Page<SysAiQaSchema> page = schemaMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        // 启用列数按表一次取回（列少，直接全量统计；不要为逐表 count）
        Map<String, Long> enabledColumnCount = columnMapper.selectList(
                        new LambdaQueryWrapper<SysAiQaColumn>().eq(SysAiQaColumn::getIsEnabled, 1))
                .stream()
                .collect(Collectors.groupingBy(SysAiQaColumn::getTableName, Collectors.counting()));

        List<OperationQaSchemaListVO> records = page.getRecords().stream().map(entity -> {
            OperationQaSchemaListVO vo = new OperationQaSchemaListVO();
            BeanUtils.copyProperties(entity, vo);
            vo.setEnabledColumnCount(Math.toIntExact(
                    enabledColumnCount.getOrDefault(entity.getTableName(), 0L)));
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertSchema(OperationQaSchemaUpsertDTO dto) {
        String tableName = dto.getTableName().trim();
        assertTableNameAvailable(tableName, dto.getId());
        SysAiQaSchema entity = new SysAiQaSchema();
        BeanUtils.copyProperties(dto, entity);
        entity.setTableName(tableName);
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        if (dto.getId() == null) {
            save(entity);
        } else {
            updateById(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSchemaById(Long id) {
        SysAiQaSchema schema = schemaMapper.selectById(id);
        if (schema == null) {
            throw new BusinessException("数据表配置不存在");
        }
        // 级联物理删该表全部列配置（配置无留档价值；软删旧行占唯一键，同名列重建会撞键）
        columnMapper.purgeByTable(schema.getTableName());
        schemaMapper.purgeById(id);
    }

    // ---------------- column（列级） ----------------

    @Override
    public PageResult<OperationQaColumnListVO> listColumnPage(OperationQaColumnQueryPageDTO dto) {
        LambdaQueryWrapper<SysAiQaColumn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAiQaColumn::getTableName, dto.getTableName().trim())
                .eq(dto.getIsEnabled() != null, SysAiQaColumn::getIsEnabled, dto.getIsEnabled())
                .orderByAsc(SysAiQaColumn::getSortOrder);

        Page<SysAiQaColumn> page = columnMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<OperationQaColumnListVO> records = page.getRecords().stream().map(entity -> {
            OperationQaColumnListVO vo = new OperationQaColumnListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertColumn(OperationQaColumnUpsertDTO dto) {
        String tableName = dto.getTableName().trim();
        String columnName = dto.getColumnName().trim();
        // 目标表必须已启用（列配置不允许挂在停用/未维护的表上）
        if (!enabledTableNames().contains(tableName.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("数据表 " + tableName + " 不在启用数据表配置中，请先启用数据表");
        }
        Long exist = columnMapper.selectCount(new LambdaQueryWrapper<SysAiQaColumn>()
                .eq(SysAiQaColumn::getTableName, tableName)
                .eq(SysAiQaColumn::getColumnName, columnName)
                .ne(dto.getId() != null, SysAiQaColumn::getId, dto.getId()));
        if (exist != null && exist > 0) {
            throw new BusinessException("列 " + tableName + "." + columnName + " 已存在");
        }
        SysAiQaColumn entity = new SysAiQaColumn();
        BeanUtils.copyProperties(dto, entity);
        entity.setTableName(tableName);
        entity.setColumnName(columnName);
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }
        if (entity.getIsFilterable() == null) {
            entity.setIsFilterable(0);
        }
        if (entity.getIsGroupable() == null) {
            entity.setIsGroupable(1);
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        if (dto.getId() == null) {
            columnMapper.insert(entity);
        } else {
            columnMapper.updateById(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteColumnById(Long id) {
        if (columnMapper.selectById(id) == null) {
            throw new BusinessException("数据列配置不存在");
        }
        // 物理删：软删旧行占唯一键 uk_table_column，同名列重建会撞键（G12 口径，配置无留档价值）
        columnMapper.purgeById(id);
    }

    // ---------------- semantic（语义映射） ----------------

    @Override
    public PageResult<OperationQaSemanticListVO> listSemanticPage(OperationQaSemanticQueryPageDTO dto) {
        LambdaQueryWrapper<SysAiQaSemantic> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(dto.getTerm()), SysAiQaSemantic::getTerm, dto.getTerm())
                .eq(TextUtil.hasText(dto.getTableName()), SysAiQaSemantic::getTableName, dto.getTableName())
                .eq(dto.getIsEnabled() != null, SysAiQaSemantic::getIsEnabled, dto.getIsEnabled())
                .orderByDesc(SysAiQaSemantic::getPriority)
                .orderByAsc(SysAiQaSemantic::getId);

        Page<SysAiQaSemantic> page = semanticMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<OperationQaSemanticListVO> records = page.getRecords().stream().map(entity -> {
            OperationQaSemanticListVO vo = new OperationQaSemanticListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertSemantic(OperationQaSemanticUpsertDTO dto) {
        String term = dto.getTerm().trim();
        String tableName = dto.getTableName().trim();
        if (!enabledTableNames().contains(tableName.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("数据表 " + tableName + " 不在启用数据表配置中，请先启用数据表");
        }
        Long exist = semanticMapper.selectCount(new LambdaQueryWrapper<SysAiQaSemantic>()
                .eq(SysAiQaSemantic::getTerm, term)
                .ne(dto.getId() != null, SysAiQaSemantic::getId, dto.getId()));
        if (exist != null && exist > 0) {
            throw new BusinessException("语义词「" + term + "」已存在");
        }
        SysAiQaSemantic entity = new SysAiQaSemantic();
        BeanUtils.copyProperties(dto, entity);
        entity.setTerm(term);
        entity.setTableName(tableName);
        if (entity.getMatchMode() == null) {
            entity.setMatchMode(1);
        }
        if (entity.getPriority() == null) {
            entity.setPriority(0);
        }
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }
        if (dto.getId() == null) {
            semanticMapper.insert(entity);
        } else {
            semanticMapper.updateById(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSemanticById(Long id) {
        if (semanticMapper.selectById(id) == null) {
            throw new BusinessException("语义映射不存在");
        }
        // 物理删：软删旧行占唯一键 uk_term，同名词条重建后再次删除会撞键（G12 口径，配置无留档价值）
        semanticMapper.purgeById(id);
    }

    // ---------------- relation（表间关系/join 图谱） ----------------

    @Override
    public PageResult<OperationQaRelationListVO> listRelationPage(OperationQaRelationQueryPageDTO dto) {
        LambdaQueryWrapper<SysAiQaRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(dto.getSourceTable()), SysAiQaRelation::getSourceTable, dto.getSourceTable())
                .like(TextUtil.hasText(dto.getTargetTable()), SysAiQaRelation::getTargetTable, dto.getTargetTable())
                .eq(dto.getRelationType() != null, SysAiQaRelation::getRelationType, dto.getRelationType())
                .eq(dto.getIsEnabled() != null, SysAiQaRelation::getIsEnabled, dto.getIsEnabled())
                .orderByAsc(SysAiQaRelation::getSortOrder)
                .orderByAsc(SysAiQaRelation::getId);

        Page<SysAiQaRelation> page = relationMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<OperationQaRelationListVO> records = page.getRecords().stream().map(entity -> {
            OperationQaRelationListVO vo = new OperationQaRelationListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertRelation(OperationQaRelationUpsertDTO dto) {
        String sourceTable = dto.getSourceTable().trim();
        String sourceColumn = dto.getSourceColumn().trim();
        String targetTable = dto.getTargetTable().trim();
        String targetColumn = dto.getTargetColumn().trim();
        // 源/目标表必须都在启用表中（关系只描述白名单内可查表的 join 路径）
        Set<String> enabled = enabledTableNames();
        if (!enabled.contains(sourceTable.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("源表 " + sourceTable + " 不在启用数据表配置中，请先启用数据表");
        }
        if (!enabled.contains(targetTable.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("目标表 " + targetTable + " 不在启用数据表配置中，请先启用数据表");
        }
        Long exist = relationMapper.selectCount(new LambdaQueryWrapper<SysAiQaRelation>()
                .eq(SysAiQaRelation::getSourceTable, sourceTable)
                .eq(SysAiQaRelation::getSourceColumn, sourceColumn)
                .eq(SysAiQaRelation::getTargetTable, targetTable)
                .eq(SysAiQaRelation::getTargetColumn, targetColumn)
                .ne(dto.getId() != null, SysAiQaRelation::getId, dto.getId()));
        if (exist != null && exist > 0) {
            throw new BusinessException("关系 " + sourceTable + "." + sourceColumn
                    + " → " + targetTable + "." + targetColumn + " 已存在");
        }
        SysAiQaRelation entity = new SysAiQaRelation();
        BeanUtils.copyProperties(dto, entity);
        entity.setSourceTable(sourceTable);
        entity.setSourceColumn(sourceColumn);
        entity.setTargetTable(targetTable);
        entity.setTargetColumn(targetColumn);
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        if (dto.getId() == null) {
            relationMapper.insert(entity);
        } else {
            relationMapper.updateById(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelationById(Long id) {
        if (relationMapper.selectById(id) == null) {
            throw new BusinessException("表间关系配置不存在");
        }
        // 物理删：软删旧行占唯一键 uk_rel_src_tgt，同关系重建后再次删除会撞键（G12 口径，配置无留档价值）
        relationMapper.purgeById(id);
    }

    // ---------------- 下拉与运行时查询（直接查库，无内存快照） ----------------

    @Override
    public List<OperationQaSchemaSelectListVO> schemaSelectList() {
        return schemaMapper.selectList(
                        new LambdaQueryWrapper<SysAiQaSchema>()
                                .eq(SysAiQaSchema::getIsEnabled, 1)
                                .orderByAsc(SysAiQaSchema::getSortOrder))
                .stream()
                .map(s -> {
                    OperationQaSchemaSelectListVO vo = new OperationQaSchemaSelectListVO();
                    vo.setTableName(s.getTableName());
                    vo.setTableDesc(s.getTableDesc());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<OperationQaColumnSelectListVO> columnSelectList(String tableName) {
        if (!TextUtil.hasText(tableName)) {
            return List.of();
        }
        return columnMapper.selectList(
                        new LambdaQueryWrapper<SysAiQaColumn>()
                                .eq(SysAiQaColumn::getTableName, tableName.trim())
                                .eq(SysAiQaColumn::getIsEnabled, 1)
                                .orderByAsc(SysAiQaColumn::getSortOrder))
                .stream()
                .map(c -> {
                    OperationQaColumnSelectListVO vo = new OperationQaColumnSelectListVO();
                    vo.setColumnName(c.getColumnName());
                    vo.setColumnDesc(c.getColumnDesc());
                    vo.setColumnType(c.getColumnType());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> enabledTableNames() {
        return schemaMapper.selectList(new LambdaQueryWrapper<SysAiQaSchema>()
                        .eq(SysAiQaSchema::getIsEnabled, 1))
                .stream()
                .map(s -> s.getTableName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    @Override
    public List<OperationSchemaCatalog.TableDef> tableDefs(Collection<String> tableNames) {
        boolean scoped = tableNames != null && !tableNames.isEmpty();
        Set<String> scopedLower = scoped
                ? tableNames.stream().map(t -> t.toLowerCase(Locale.ROOT)).collect(Collectors.toSet())
                : null;

        LambdaQueryWrapper<SysAiQaSchema> schemaWrapper = new LambdaQueryWrapper<SysAiQaSchema>()
                .eq(SysAiQaSchema::getIsEnabled, 1)
                .orderByAsc(SysAiQaSchema::getSortOrder);
        if (scoped) {
            schemaWrapper.in(SysAiQaSchema::getTableName, scopedLower);
        }
        List<SysAiQaSchema> schemas = schemaMapper.selectList(schemaWrapper);
        if (schemas.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<SysAiQaColumn> columnWrapper = new LambdaQueryWrapper<SysAiQaColumn>()
                .eq(SysAiQaColumn::getIsEnabled, 1)
                .in(SysAiQaColumn::getTableName, schemas.stream()
                        .map(SysAiQaSchema::getTableName).collect(Collectors.toList()));
        List<SysAiQaColumn> columns = columnMapper.selectList(columnWrapper);

        Map<String, List<OperationSchemaCatalog.ColumnDef>> columnsByTable = columns.stream()
                .collect(Collectors.groupingBy(SysAiQaColumn::getTableName,
                        Collectors.mapping(c -> new OperationSchemaCatalog.ColumnDef(
                                c.getColumnName(), c.getColumnDesc(), c.getColumnType(),
                                c.getIsFilterable() != null && c.getIsFilterable() == 1,
                                c.getIsGroupable() != null && c.getIsGroupable() == 1),
                                Collectors.toList())));

        return schemas.stream()
                .map(s -> new OperationSchemaCatalog.TableDef(
                        s.getTableName(), s.getTableDesc(),
                        columnsByTable.getOrDefault(s.getTableName(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public List<OperationSchemaCatalog.SemanticDef> semanticDefs() {
        return semanticMapper.selectList(new LambdaQueryWrapper<SysAiQaSemantic>()
                        .eq(SysAiQaSemantic::getIsEnabled, 1)
                        .orderByDesc(SysAiQaSemantic::getPriority))
                .stream()
                .map(s -> new OperationSchemaCatalog.SemanticDef(
                        s.getTerm(), s.getTableName(), s.getColumnName(), s.getScopeDesc(),
                        s.getMatchMode(), s.getPriority()))
                .collect(Collectors.toList());
    }

    @Override
    public List<OperationSchemaCatalog.SensitiveDef> sensitiveDefs() {
        // 只取启用表下的敏感列（停用表不进白名单，配置了也不生效，避免噪音）
        Set<String> enabled = enabledTableNames();
        return columnMapper.selectList(new LambdaQueryWrapper<SysAiQaColumn>()
                        .gt(SysAiQaColumn::getSensitiveType, 0))
                .stream()
                .filter(c -> enabled.contains(c.getTableName().toLowerCase(Locale.ROOT)))
                .map(c -> new OperationSchemaCatalog.SensitiveDef(
                        c.getTableName(), c.getColumnName(), c.getSensitiveType(), c.getMaskType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<OperationSchemaCatalog.RelationDef> relationDefs() {
        return relationMapper.selectList(new LambdaQueryWrapper<SysAiQaRelation>()
                        .eq(SysAiQaRelation::getIsEnabled, 1)
                        .orderByAsc(SysAiQaRelation::getSortOrder))
                .stream()
                .map(r -> new OperationSchemaCatalog.RelationDef(
                        r.getSourceTable(), r.getSourceColumn(),
                        r.getTargetTable(), r.getTargetColumn(),
                        r.getRelationType(), r.getDescription()))
                .collect(Collectors.toList());
    }

    // ---------------- 重写刷新（表结构同步唯一入口） ----------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String rewriteSchema() {
        JdbcTemplate template = new JdbcTemplate(dataSource);

        // 1. 扫描当前库全部 BASE TABLE 与其列（information_schema，真实 DDL）
        List<Map<String, Object>> dbTables = template.queryForList(
                "SELECT table_name, table_comment FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'");
        List<Map<String, Object>> dbColumns = template.queryForList(
                "SELECT table_name, column_name, column_comment, data_type FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE()");

        // 2. 现有配置索引（大小写不敏感）
        Map<String, SysAiQaSchema> schemaByLower = schemaMapper.selectList(
                        new LambdaQueryWrapper<SysAiQaSchema>().orderByAsc(SysAiQaSchema::getSortOrder))
                .stream()
                .collect(Collectors.toMap(
                        s -> s.getTableName().toLowerCase(Locale.ROOT), s -> s, (a, b) -> a));
        Map<String, SysAiQaColumn> columnByKey = columnMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(
                        c -> (c.getTableName() + "." + c.getColumnName()).toLowerCase(Locale.ROOT),
                        c -> c, (a, b) -> a));

        Set<String> dbTableSet = new HashSet<>();
        Map<String, String> dbTableComment = new HashMap<>();
        for (Map<String, Object> row : dbTables) {
            String name = String.valueOf(row.get("table_name")).toLowerCase(Locale.ROOT);
            dbTableSet.add(name);
            dbTableComment.put(name, row.get("table_comment") == null ? "" : String.valueOf(row.get("table_comment")));
        }
        // 库中存在但尚未入库的表名（大小写不敏感 → 与库名一致的原始名）
        Map<String, String> dbNameByLower = new HashMap<>();
        for (Map<String, Object> row : dbTables) {
            dbNameByLower.put(String.valueOf(row.get("table_name")).toLowerCase(Locale.ROOT),
                    String.valueOf(row.get("table_name")));
        }

        int addedTables = 0, deletedTables = 0;
        int addedColumns = 0, updatedColumns = 0, deletedColumns = 0;

        // 3. 表级对齐：库中存在的新表默认停用入库；库中已删的表配置物理删（级联列）
        for (String dbTable : dbTableSet) {
            if (!schemaByLower.containsKey(dbTable)) {
                SysAiQaSchema entity = new SysAiQaSchema();
                entity.setTableName(dbNameByLower.get(dbTable));
                String comment = dbTableComment.getOrDefault(dbTable, "");
                entity.setTableDesc(TextUtil.hasText(comment) ? comment : dbNameByLower.get(dbTable));
                entity.setIsEnabled(0); // 新表默认停用，不进白名单（安全：需运营手动启用）
                entity.setSortOrder(0);
                schemaMapper.insert(entity);
                addedTables++;
            }
        }
        for (String cfgTable : schemaByLower.keySet()) {
            if (!dbTableSet.contains(cfgTable)) {
                // 表已从库中删除：级联物理删列配置 + 表配置
                columnMapper.purgeByTable(schemaByLower.get(cfgTable).getTableName());
                schemaMapper.purgeById(schemaByLower.get(cfgTable).getId());
                deletedTables++;
            }
        }

        // 4. 列级对齐：新增列默认启用；已有列用真实 DDL 覆盖注释/类型（保留启用与过滤/分组标记）；已删列物理删
        Set<String> dbColumnSet = new HashSet<>();
        for (Map<String, Object> row : dbColumns) {
            String t = String.valueOf(row.get("table_name")).toLowerCase(Locale.ROOT);
            String c = String.valueOf(row.get("column_name"));
            dbColumnSet.add((t + "." + c).toLowerCase(Locale.ROOT));
        }
        for (Map<String, Object> row : dbColumns) {
            String table = String.valueOf(row.get("table_name")).toLowerCase(Locale.ROOT);
            if (!dbTableSet.contains(table)) {
                continue;
            }
            String column = String.valueOf(row.get("column_name"));
            String key = (table + "." + column).toLowerCase(Locale.ROOT);
            String comment = row.get("column_comment") == null ? "" : String.valueOf(row.get("column_comment"));
            String type = row.get("data_type") == null ? "" : String.valueOf(row.get("data_type"));
            SysAiQaColumn exist = columnByKey.get(key);
            if (exist != null) {
                // 真实 DDL 覆盖结构信息；运营开关原样保留
                if (!Objects.equals(exist.getColumnDesc(), comment)
                        || !Objects.equals(exist.getColumnType(), type)) {
                    SysAiQaColumn update = new SysAiQaColumn();
                    update.setId(exist.getId());
                    update.setColumnDesc(TextUtil.hasText(comment) ? comment : column);
                    update.setColumnType(type);
                    columnMapper.updateById(update);
                    updatedColumns++;
                }
            } else {
                SysAiQaColumn entity = new SysAiQaColumn();
                entity.setTableName(dbNameByLower.getOrDefault(table, table));
                entity.setColumnName(column);
                entity.setColumnDesc(TextUtil.hasText(comment) ? comment : column);
                entity.setColumnType(type);
                entity.setIsEnabled(1); // 新列默认喂模型（表启用即开放；停用表的列不会进入生成段）
                entity.setIsFilterable(0);
                entity.setIsGroupable(1);
                entity.setSortOrder(0);
                columnMapper.insert(entity);
                addedColumns++;
            }
        }
        // 表仍在库中但配置多出的列 = 该列已从表结构删除 → 物理删
        for (String cfgKey : columnByKey.keySet()) {
            String table = cfgKey.substring(0, cfgKey.lastIndexOf('.'));
            if (dbTableSet.contains(table) && !dbColumnSet.contains(cfgKey)) {
                columnMapper.purgeById(columnByKey.get(cfgKey).getId());
                deletedColumns++;
            }
        }

        String summary = String.format("重写刷新完成：扫描 %d 张表 / %d 列；新增表 %d、删除表 %d、新增列 %d、更新列 %d、删除列 %d",
                dbTableSet.size(), dbColumns.size(), addedTables, deletedTables, addedColumns, updatedColumns, deletedColumns);
        log.info("[AI-运营问数] {}", summary);
        return summary;
    }

    /**
     * 表名唯一校验（含逻辑删除行：软删后可重建同名配置，靠唯一键含 del_flag 兜底）
     */
    private void assertTableNameAvailable(String tableName, Long selfId) {
        Long exist = schemaMapper.selectCount(new LambdaQueryWrapper<SysAiQaSchema>()
                .eq(SysAiQaSchema::getTableName, tableName)
                .ne(selfId != null, SysAiQaSchema::getId, selfId));
        if (exist != null && exist > 0) {
            throw new BusinessException("数据表 " + tableName + " 已存在");
        }
    }
}

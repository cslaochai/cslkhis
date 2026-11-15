package com.his.support;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * SQL 耗时指标 + 慢 SQL 告警日志。
 *
 * <p>代理的是 {@link Executor}（MyBatis 执行 SQL 的唯一入口），所以 Mapper 注解 SQL、XML SQL、
 * 以及 MyBatis-Plus 的 Lambda 查询全都会被统计到，不需要在每个 Mapper 上写埋点。</p>
 *
 * <p>两个输出：</p>
 * <ul>
 *   <li>{@code his_sql_duration_seconds}：全量 SQL 耗时分位数（P50/P95/P99），用来看"慢是慢在接口还是慢在库"；</li>
 *   <li>{@code his_sql_slow_total{statement}}：超阈值次数，按 Mapper 方法聚合，直接指到要优化的那条语句。</li>
 * </ul>
 */
@Slf4j
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class SlowSqlMetricsInterceptor implements Interceptor {

    private static final String DURATION_METRIC = "his.sql.duration";
    private static final String SLOW_METRIC = "his.sql.slow";
    private static final int SQL_TEXT_MAX = 500;

    private final MeterRegistry registry;

    /**
     * 预建好的 SQL 耗时计时器：带直方图桶（否则 Grafana 算不出 P95/P99，只能看均值）
     */
    private final Timer durationTimer;

    /**
     * 慢 SQL 阈值（毫秒）。医院内网库，500ms 已经明显拖接口了
     */
    private final long slowMillis;

    public SlowSqlMetricsInterceptor(MeterRegistry registry,
                                     @Value("${his.observability.slow-sql-millis:500}") long slowMillis) {
        this.registry = registry;
        this.slowMillis = slowMillis;
        this.durationTimer = Timer.builder(DURATION_METRIC)
                .description("SQL 执行耗时（全量，含分页 count）")
                .publishPercentileHistogram()
                .register(registry);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        long start = System.nanoTime();
        try {
            return invocation.proceed();
        } finally {
            long costMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            durationTimer.record(costMillis, TimeUnit.MILLISECONDS);
            if (costMillis >= slowMillis) {
                registry.counter(SLOW_METRIC, "statement", statement.getId()).increment();
                log.warn("慢 SQL {}ms（阈值 {}ms）{} sql={}", costMillis, slowMillis,
                        statement.getId(), sqlTextOf(invocation, statement));
            }
        }
    }

    /**
     * 慢 SQL 的 SQL 文本是排查的全部线索。取不到（动态 SQL 未绑定参数等）就留空，不能因为日志拼装失败把业务异常盖掉。
     */
    private static String sqlTextOf(Invocation invocation, MappedStatement statement) {
        try {
            Object parameter = invocation.getArgs().length > 1 ? invocation.getArgs()[1] : null;
            String sql = statement.getBoundSql(parameter).getSql();
            String oneLine = sql == null ? "" : sql.replaceAll("\\s+", " ").trim();
            return oneLine.length() > SQL_TEXT_MAX ? oneLine.substring(0, SQL_TEXT_MAX) + "..." : oneLine;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}

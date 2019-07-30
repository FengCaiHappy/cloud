package com.feng.autoinjection.mybatisplugin;


import com.feng.autoinjection.core.bean.QuickList;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Intercepts({@Signature(
        type= Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class ReBuildSQLPlugin implements Interceptor {
    private Logger logger = LoggerFactory.getLogger(ReBuildSQLPlugin.class);

    private Map<String, MixedSqlNode> customSQL;

    private QuickList quickList;

    public ReBuildSQLPlugin(){
        super();
    }

    public ReBuildSQLPlugin(Map<String, MixedSqlNode> customSQL, QuickList quickList){
        this();
        this.customSQL = customSQL;
        this.quickList = quickList;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        Map<String, Object> paramMap = null;
        if(args != null){
            for(int i = 0; i < args.length; i++){
                if(args[i] instanceof Map){
                    paramMap = (Map)args[i];
                }
            }
        }
        if(paramMap == null){
            return invocation.proceed();
        }
        int INDEX_MS = 0;
        MappedStatement ms = (MappedStatement)args[INDEX_MS];
        String tableName = paramMap.get("tableName").toString();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(),
                ms.getResultMaps().get(0).getId(),
                Class.forName(quickList.getBean(tableName).getMapperBeaName()),
                ms.getResultMaps().get(0).getResultMappings(),
                true).build();
        List<ResultMap> resultMapList = new ArrayList<>();
        resultMapList.add(resultMap);
        Field field = MappedStatement.class.getDeclaredField("resultMaps");
        field.setAccessible(true);
        field.set(ms, resultMapList);

        String id = ms.getId();
        String methodName = id.substring(id.lastIndexOf("."), id.length());
        MixedSqlNode sql = customSQL.get(tableName + methodName);
        if(!StringUtils.isEmpty(sql)){
            DynamicSqlSource dynamicSqlSource = new DynamicSqlSource(ms.getConfiguration(), sql);
            MappedStatement newMs = copyFromMappedStatement(ms, dynamicSqlSource);
            args[INDEX_MS] = newMs;
        }
        return invocation.proceed();
    }

    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length > 0) {
            builder.keyProperty(ms.getKeyProperties()[0]);
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target,this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}

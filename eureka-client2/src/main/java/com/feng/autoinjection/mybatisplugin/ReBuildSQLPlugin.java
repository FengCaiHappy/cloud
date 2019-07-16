package com.feng.autoinjection.mybatisplugin;


import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

@Intercepts({@Signature(
        type= Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class ReBuildSQLPlugin implements Interceptor {
    private Logger logger = LoggerFactory.getLogger(ReBuildSQLPlugin.class);

    private Map<String, String> multiTableQuerySQL;

    public ReBuildSQLPlugin(){
        super();
    }

    public ReBuildSQLPlugin(Map<String, String> multiTableQuerySQL){
        this();
        this.multiTableQuerySQL = multiTableQuerySQL;
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
        int INDEX_MS = 0;
        MappedStatement ms = (MappedStatement)args[INDEX_MS];
        String id =  ms.getId();
        String methodName = id.substring(id.lastIndexOf("."), id.length());
        String sql = multiTableQuerySQL.get(paramMap.get("tableName") + methodName);
        if(!StringUtils.isEmpty(sql)){
            SqlCommandType sqlCommandType = ms.getSqlCommandType();
            logger.info("-->intercept sqlCommandType: "+sqlCommandType);
            TextSqlNode textSqlNode = new TextSqlNode(sql);
            DynamicSqlSource dynamicSqlSource = new DynamicSqlSource(ms.getConfiguration(), textSqlNode);
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

package com.feng.autoinjection.core.resulthandler;

import com.feng.autoinjection.autoannotation.FTableName;

@FTableName
public interface IResultHandler<T> {
    <T> T handler(T result);
}

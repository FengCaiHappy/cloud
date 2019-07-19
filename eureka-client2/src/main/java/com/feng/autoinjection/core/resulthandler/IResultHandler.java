package com.feng.autoinjection.core.resulthandler;

public interface IResultHandler<T> {
    <T> T handler(T result);
}

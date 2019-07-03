package com.android.baselibrary.funcInterface;

public abstract class FunctionHasParamHasResult<T, R> extends Function {

    public FunctionHasParamHasResult(String functionName) {
        super(functionName);
    }

    public abstract R function(T t);
}

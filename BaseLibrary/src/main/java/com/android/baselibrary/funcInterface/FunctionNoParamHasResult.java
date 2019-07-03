package com.android.baselibrary.funcInterface;

public abstract class FunctionNoParamHasResult<R> extends Function {

    public FunctionNoParamHasResult(String functionName) {
        super(functionName);
    }

    public abstract R function();
}

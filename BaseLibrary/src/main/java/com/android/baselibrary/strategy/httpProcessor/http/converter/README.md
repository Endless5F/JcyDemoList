该目录主要是自定义Gson Converter  转换器，并且根据不同code值，处理相应事件
该包内只需要更改：GsonResponseBodyConverter类中的code值：httpResult.getCode()==?
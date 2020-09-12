package main.kotlin.协程

/**
 * Created by Administrator on 2018/4/28.
 */
object HttpError{
    const val HTTP_ERROR_NO_DATA=999
    const val HTTP_ERROR_UNKNOWN=998
}
data class HttpException(val code:Int):Exception()
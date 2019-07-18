package luyao.util.ktx.ext

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader

/**
 * Created by luyao
 * on 2019/7/18 15:49
 */
fun executeCmd(command: String): String {
    val process = Runtime.getRuntime().exec(command)

    val resultReader = BufferedReader(InputStreamReader(process.inputStream) as Reader?)
    val errorReader = BufferedReader(InputStreamReader(process.errorStream))

    val resultBuilder = StringBuilder()
    var resultLine: String? = resultReader.readLine()

    val errorBuilder = StringBuilder()
    var errorLine = errorReader.readLine()

    while (resultLine != null) {
        resultBuilder.append(resultLine)
        resultLine = resultReader.readLine()
    }

    while (errorLine != null) {
        errorBuilder.append(errorLine)
        errorLine = errorReader.readLine()
    }

    return "$resultBuilder\n$errorBuilder"
}
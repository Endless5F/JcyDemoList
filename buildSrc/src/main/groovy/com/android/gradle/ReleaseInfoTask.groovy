import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ReleaseInfoTask extends DefaultTask {

    ReleaseInfoTask() {
        group 'android' // 指定分组
        description 'update the release info' // 添加说明信息
    }

    /**
     * 使用TaskAction注解，可以让方法在gradle的执行阶段去执行。
     * doFirst其实就是在外部为@TaskAction的最前面添加执行逻辑。
     * 而doLast则是在外部为@TaskAction的最后面添加执行逻辑。
     */
    @TaskAction
    void doAction() {
        updateInfo()
    }

    private void updateInfo() {
        // 获取gradle脚本中配置的参数(通过releaseInfo这个闭包配置的参数)
        def versionCodeMsg = project.extensions.releaseInfo.versionCode
        def versionNameMsg = project.extensions.releaseInfo.versionName
        def versionInfoMsg = project.extensions.releaseInfo.versionInfo
        def fileName = project.extensions.releaseInfo.fileName
        // 创建xml文件
        def file = project.file(fileName)
        if (file != null && !file.exists()) {
            file.createNewFile()
        }
        // 创建写入xml数据所需要的类。
        def sw = new StringWriter();
        def xmlBuilder = new groovy.xml.MarkupBuilder(sw)
        // 若xml文件中没有内容，就多创建一个realease节点，并写入xml数据
        if (file.text != null && file.text.size() <= 0) {
            xmlBuilder.releases {
                release {
                    versionCode(versionCodeMsg)
                    versionName(versionNameMsg)
                    versionInfo(versionInfoMsg)
                }
            }
            file.withWriter { writer ->
                writer.append(sw.toString())
            }
        } else { // 若xml文件中已经有内容，则在原来的内容上追加。
            xmlBuilder.release {
                versionCode(versionCodeMsg)
                versionName(versionNameMsg)
                versionInfo(versionInfoMsg)
            }
            def lines = file.readLines()
            def lengths = lines.size() - 1
            file.withWriter { writer ->
                lines.eachWithIndex { String line, int index ->
                    if (index != lengths) {
                        writer.append(line + '\r\n')
                    } else if (index == lengths) {
                        writer.append(sw.toString() + '\r\n')
                        writer.append(line + '\r\n')
                    }
                }
            }
        }
    }
}
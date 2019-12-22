import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 自定义Gradle插件
 */
class GradleStudyPlugin implements Plugin<Project> {

    /**
     * 插件引入时要执行的方法
     * @param project 引入当前插件的project
     */
    @Override
    void apply(Project project) {
        println '你好gradle学习插件。当前项目名称为 ' + project.name

        // 这样就可以在gradle脚本中，通过releaseInfo闭包来完成ReleaseInfoExtension的初始化。
        project.extensions.create("releaseInfo", ReleaseInfoExtension)

        // 创建Task
        project.tasks.create("updateReleaseInfo", ReleaseInfoTask)
    }
}
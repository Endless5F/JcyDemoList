# BaseLibrary

#### 介绍
基类-子模块

#### 软件架构
软件架构说明

#### 初始化-关联子模块

    git submodule add https://gitee.com/jcy_1995/BaseLibrary BaseLibrary
    git status
    git diff
    git add .
    git commit -m "add submodule"
    git push origin master

#### 删除子模块
    git rm --cached ModuleA
    rm -rf ModuleA
    rm .gitmodules
    vim .git/config
    删除submodule相关的内容，例如下面的内容
        [submodule "lib_http"]
        path = lib_http
        url = https://github.com/xiangzhenlee/lib_http.git
    然后提交到远程服务器
    git add .
    git commit -m "remove submodule"
    需要删除暂存区或分支上的文件, 同时工作区也不需要这个文件了,
    git rm ModuleA
    git commit -m 'delete somefile'
    git push

#### 安装教程
工程最外层 gradle 配置

    buildscript {
        ext.compile_sdk = 28
        ext.build_tools = '28.0.0'
        ext.min_sdk = 19
        ext.target_sdk = compile_sdk
        ext.android_support = '28.0.0'
        ext.constraint_layout = '1.1.3'
        ext.junit_version = '4.12'
        ext.retrofit_version = '2.4.0'
        ext.adapter_rxjava2 = "2.3.0"
        ext.logging_interceptor = '3.10.0'
        ext.rxandroid_version = '2.0.2'
        
        repositories {
            mavenCentral()
            google()
            jcenter()
            maven { url "https://jitpack.io" }
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:3.1.4'
        }
    }
    
    allprojects {
        repositories {
            mavenCentral()
            google()
            jcenter()
            maven { url "https://jitpack.io" }
        }
    }
    
    task clean(type: Delete) {
        delete rootProject.buildDir
    }

package com.android.gradle

class ReleaseInfoExtension {
    String versionCode
    String versionName
    String versionInfo
    String fileName

    ReleaseInfoExtension() {}

    @Override
    String toString() {
        return "versionCode = ${versionCode} , versionName = ${versionName} ," +
                " versionInfo = ${versionInfo} , fileName = ${fileName}"
    }
}
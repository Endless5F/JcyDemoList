package com.android.baselibrary.bean

import com.android.baselibrary.http.common.HttpResult
import com.android.baselibrary.utils.JsonUtils
import com.google.gson.JsonObject

open class ShareBeanResult: HttpResult() {
    var data: Data = Data()

    open class Data {
        open var shareInfo: ShareInfo? = null
    }
}

class ShareInfo {

    constructor(data: JsonObject) {
        val coinInfoJson = JsonUtils.getJsonObject(data, "coinInfo")
        coinInfoJson?.let {
            parseCoinInfo(it)
        }
    }

    private fun parseCoinInfo(jsonItem: JsonObject) {
        this.coinCount = JsonUtils.getInt(jsonItem, "coinCount")
        this.level = JsonUtils.getInt(jsonItem, "level")
        this.rank = JsonUtils.getString(jsonItem, "rank")
        this.userId = JsonUtils.getInt(jsonItem, "userId")
        this.username = JsonUtils.getString(jsonItem, "username")
    }

    var coinCount: Int = 0
    var level: Int = 0
    var rank: String = ""
    var userId: Int = 0
    var username: String = ""
}

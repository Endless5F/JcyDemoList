package com.android.baselibrary.bean.deser;

import com.android.baselibrary.bean.ShareBeanResult;
import com.android.baselibrary.bean.ShareInfo;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ShareBeanResultDeser implements JsonDeserializer<ShareBeanResult> {
    @Override
    public ShareBeanResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ShareBeanResult result = new ShareBeanResult();
        ShareBeanResult.Data data = new ShareBeanResult.Data();
        JsonObject asJsonObject = json.getAsJsonObject();
        if (asJsonObject != null) {
            result.parseErrorData(asJsonObject);
            JsonObject jsonData = asJsonObject.getAsJsonObject("data");
            if (jsonData != null) {
                data.setShareInfo(new ShareInfo(jsonData));
            }
        }
        result.setData(data);
        return result;
    }
}

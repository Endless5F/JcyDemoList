package com.android.baselibrary.strategy.demo.httpProcessor;

//import android.content.Context;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;

import java.util.Map;

public class VolleyProcessor implements IHttpProcessor {
//    private static RequestQueue mQueue = null;
//    public VolleyProcessor(Context context){
//        mQueue = Volley.newRequestQueue(context);
//    }

    @Override
    public void post(String url, Map<String, Object> params, final HttpCallback callback) {
//        StringRequest stringRequest=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                callback.onSuccess(response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                callback.onFailure(error.toString());
//            }
//        });
//        mQueue.add(stringRequest);
    }

    @Override
    public void get(String url, Map<String, Object> params, HttpCallback callback) {

    }
}


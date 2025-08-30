package com.example.pizzamania;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private final Context ctx;

    private VolleySingleton(Context context) {
        ctx = context.getApplicationContext();
        requestQueue = Volley.newRequestQueue(ctx);
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public <T> void add(Request<T> req) {
        getRequestQueue().add(req);
    }
}

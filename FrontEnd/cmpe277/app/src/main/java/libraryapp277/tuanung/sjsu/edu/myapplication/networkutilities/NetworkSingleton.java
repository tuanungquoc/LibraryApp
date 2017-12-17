package libraryapp277.tuanung.sjsu.edu.myapplication.networkutilities;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;

/**
 * Created by t0u000c on 11/3/17.
 *
 * Volley related network functionality
 */

public class NetworkSingleton {
    private static NetworkSingleton mNetworkSingleton;
    private static RequestQueue mRequestQueue;
    private Context mContext;

    private NetworkSingleton(Context context){
        this.mContext = context;

    }

    public static NetworkSingleton get(Context c){
        if(mNetworkSingleton == null  ){
            mNetworkSingleton = new NetworkSingleton(c.getApplicationContext());
        }
        return  mNetworkSingleton;
    }
    public  RequestQueue getVolleyRequestQueue()
    {
        if (mRequestQueue == null)
        {
            mRequestQueue = Volley.newRequestQueue
                    (mContext, new OkHttpStack(new OkHttpClient()));
        }

        return mRequestQueue;
    }

    public static void addRequest
            (@NonNull final Request<?> request, @NonNull final String tag)
    {
        request.setTag(tag);
        addRequest(request);
    }

    private static void addRequest(@NonNull final Request<?> request)
    {
        mNetworkSingleton.getVolleyRequestQueue().add(request);
    }

    public static void cancelAllRequests(@NonNull final String tag)
    {
        mNetworkSingleton.getVolleyRequestQueue().cancelAll(tag);
    }
}

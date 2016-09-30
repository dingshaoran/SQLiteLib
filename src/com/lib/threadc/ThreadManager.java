package com.lib.threadc;

import android.app.Activity;
import android.view.View;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by dsr on 16/7/18.
 */
public class ThreadManager {
    public static int COREPOOLSIZE = 3;
    private static ThreadManager l = new ThreadManager();
    private ThreadPoolExecutor mPool;

    private ThreadManager() {
        mPool = new ThreadPoolExecutor(COREPOOLSIZE, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("xin_pool");
                return thread;
            }
        });
    }

    public static ThreadManager getInstance() {
        return l;
    }

    public void execute(final View context, final Task r, final Object obj) {
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                final Object res = r.onSubThread(obj);
                context.post(new Runnable() {
                    @Override
                    public void run() {
                        r.onFinish(res);
                    }
                });
            }
        });
    }

    public void execute(final Activity context, final Task r, final Object obj) {
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                final Object res = r.onSubThread(obj);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        r.onFinish(res);
                    }
                });
            }
        });
    }

    /**
     * 已经在子线程post 到主线程
     *
     * @param context
     * @param r
     */
    public void post(final Activity context, final Task r, final Object res) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                r.onFinish(res);
            }
        });
    }

    public void execute(Runnable r) {
        mPool.execute(r);
    }

    public static interface Task<Param, Result> {

        void onFinish(Result obj);

        Result onSubThread(Param obj);
    }
}

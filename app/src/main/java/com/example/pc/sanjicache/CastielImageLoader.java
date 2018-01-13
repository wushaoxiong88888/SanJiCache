package com.example.pc.sanjicache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

/**
 * Created by pc on 2017/12/7.
 */

public class CastielImageLoader {
    private static final int MAX_CAPACITY = 20;// 链表长度
    private static Context mContext;// 获取APP的缓存地址
    private static CastielImageLoader castielImageLoader;
    // 键是图片地址、值是软引用
    private static final LinkedHashMap<String, SoftReference<Bitmap>> firstCacheMap = new LinkedHashMap<String, SoftReference<Bitmap>>(
            MAX_CAPACITY) {
        protected boolean removeEldestEntry(java.util.Map.Entry<String, java.lang.ref.SoftReference<Bitmap>> eldest) {
            // 返回true表示移除最老的软引用，保证内存平衡
            if (this.size() > MAX_CAPACITY) {
                return true;
            } else {// 否则往磁盘中添加
                diskCache(eldest.getKey(), eldest.getValue());
                return false;
            }
        }
    };

    /**
     * 单例模式加载CastielImageLoader
     * @return
     */
    public static CastielImageLoader getInstance() {
        if (castielImageLoader == null) {
            castielImageLoader = new CastielImageLoader();
        }
        return castielImageLoader;
    }
    /**
     * 加载图片到对应组件
     *
     * @param key 所需加载的路径
     * @param view 被加载的组件
     * @param
     */
    @SuppressWarnings("deprecation")
    public void loadImage(String key, ImageView view, Context context) {
        mContext = context;
        synchronized (view) {
            // 检查缓存中是否已有
            Bitmap bitmap = getFromCache(key);
            if (bitmap != null) {
                // 如果有了就从缓存中取出显示
                view.setImageBitmap(bitmap);
            } else {
                // 软应用缓存中不存在，磁盘中也不存在，只能下载
                // 下载之前应该先放一张默认图，用来友好显示
                //view.setBackgroundDrawable(drawable);
                // 用异步任务去下载
                new CastielAsyncImageLoaderTask(view).execute(key);
            }
        }
    }

    /**
     * 判断缓存中是否已经有了，如果有了就从缓存中取出
     *
     * @param key
     * @return
     */
    private Bitmap getFromCache(String key) {
        // 检查内存软引中是否存在
        synchronized (firstCacheMap) {
            if (firstCacheMap.get(key) != null) {// 内存软引用中有
                Bitmap bitmap = firstCacheMap.get(key).get();
                if (bitmap != null) {// 说明拿到了
                    firstCacheMap.put(key, new SoftReference<Bitmap>(bitmap));
                    Log.e("TAG----","内存里面的");
                    return bitmap;
                }
            }
        }
        // 检查磁盘中是否存在
        Bitmap bitmap = getFromLocalSD(key);
        if (bitmap != null) {// 硬盘中有
            firstCacheMap.put(key, new SoftReference<Bitmap>(bitmap));
            Log.e("TAG----","从磁盘中");
            return bitmap;
        }
        return null;
    }

    /**
     * 判断本地磁盘中是否已经有了该图片，如果有了就从本地磁盘中取出
     * @param key
     * @return
     */
    private Bitmap getFromLocalSD(String key) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bytes = md5.digest(key.getBytes());
        String fileName = new BigInteger(1, bytes).toString(16);
        if (fileName == null) {// 如果文件名为Null，直接返回null
            return null;
        } else {
            String filePath = mContext.getCacheDir().getAbsolutePath() + File.separator + fileName;
            InputStream is = null;
            try {
                is = new FileInputStream(new File(filePath));
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                return bitmap;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 把图片缓存到本地磁盘，拿到图片，写到SD卡中
     *
     * @param key 图片的URL
     * @param value Bitmap
     */
    private static void diskCache(String key, SoftReference<Bitmap> value) {
        // 把写入SD的图片名字改为基于MD5加密算法加密后的名字
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bytes = md5.digest(key.getBytes());
        String fileName = new BigInteger(1, bytes).toString(16);
        String filePath = mContext.getCacheDir().getAbsolutePath() + File.separator + fileName;
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(new File(filePath));
            if (value.get() != null) {
                value.get().compress(Bitmap.CompressFormat.JPEG, 60, os);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *
     * @ClassName: MyAsyncImageLoaderTask
     * @Description: 异步加载图片
     * @author
     */
    class CastielAsyncImageLoaderTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView imageView;// 图片组件
        private String key;//图片路径

        public CastielAsyncImageLoaderTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            this.key = params[0];// 图片的路径
            Bitmap bitmap = castielDownload(key);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {// 说明已经下载下来了
                addFirstCache(key,result);
                imageView.setImageBitmap(result);// 加载网络中的图片
                Log.e("TAG----","网络加载的");
            }
        }
    }

    /**
     * 根据图片路径执行图片下载
     * @param key
     * @return
     */
    public Bitmap castielDownload(String key) {
        InputStream is = null;
        try {
            is = CastielHttpUtils.castielDownLoad(key);
            return BitmapFactory.decodeStream(is);// InputStream这种加载方式暂用内存最小
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 添加到缓存中去
     * @param key
     * @param result
     */
    public void addFirstCache(String key, Bitmap result) {
        if (result != null) {
            synchronized (firstCacheMap) {
                firstCacheMap.put(key, new SoftReference<Bitmap>(result));
                diskCache(key,new SoftReference<Bitmap>(result));
            }
        }
    }
}
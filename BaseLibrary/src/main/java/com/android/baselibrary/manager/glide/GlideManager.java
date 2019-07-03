package com.android.baselibrary.manager.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.android.baselibrary.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class GlideManager {

    private static GlideManager instance;

    private GlideManager() {
    }

    public static GlideManager getInstance() {
        if (instance == null) {
            synchronized (GlideManager.class) {
                if (instance == null) {
                    instance = new GlideManager();
                }
            }
        }
        return instance;
    }

    public void loadImage(Context context, Object iconUrl, ImageView imageView) {
        Glide.with(context)
                .load(iconUrl)
                .apply(getOptions())
                .into(imageView);
    }

    public void loadImage(Context context, String iconUrl, ImageView imageView) {
        GlideApp.with(context)
                .load(iconUrl)
                .apply(getOptions())
                .into(imageView);
//        imageView.setImageResource(R.drawable.bg_default);

//        Glide.with(context)
//                .load(iconUrl)
//                .apply(getOptions())
//                .into(imageView);
    }

    public void loadImage(Context context, Object iconUrl, ImageView imageView, ImageView
            .ScaleType scaleType) {
        RequestBuilder<Drawable> load = Glide.with(context)
                .load(iconUrl);
        switch (scaleType) {
            case CENTER_CROP:
                load.apply(getOptions().centerCrop())
                        .into(imageView);
                break;
            default:
                load.apply(getOptions().fitCenter())
                        .into(imageView);
                break;
        }
    }

    // 加载Drawable
    public void loadImageDrawable(Context context, Object url
            , final IGlideLoadedLisenter iGlideLoadedLisenter) {
        GlideApp.with(context)
                .load(url)
                .apply(getOptions())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource
                            , @Nullable Transition<? super Drawable> transition) {
                        iGlideLoadedLisenter.onLoaded(resource);
                    }
                });

//        Glide.with(context).load(url).into(new SimpleTarget<Drawable>() {
//            @Override
//            public void onResourceReady(@NonNull Drawable resource
//                    , @Nullable Transition<? super Drawable> transition) {
//                iGlideLoadedLisenter.onLoaded(resource);
//            }
//        });
    }

    // 根据Drawable加载Drawable高斯模糊
    public void loadImageDrawableBlur(Context context, Drawable drawable, ImageView imageView) {

        GlideApp.with(context)
                .load(drawable)
                .apply(bitmapTransform(new BlurTransformation(14, 3)))
                .into(imageView);

//        Glide.with(context)
//                .load(drawable)
//                .apply(RequestOptions.bitmapTransform(new BlurTransformation(context,14, 3)))
//                .into(imageView);
    }

    // 根据Drawable加载Drawable高斯模糊
    public void loadImageDrawableBlur(Context context
            , Drawable drawable, final IGlideLoadedLisenter iGlideLoadedLisenter) {
        GlideApp.with(context)
                .load(drawable)
                .apply(bitmapTransform(new BlurTransformation(14,3)))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource
                            , Transition<? super Drawable> transition) {
                        iGlideLoadedLisenter.onLoaded(resource);
                    }
                });


//        Glide.with(context)
//                .load(drawable)
//                .apply(RequestOptions.bitmapTransform(new BlurTransformation2(14, 3)))
//                .into(new SimpleTarget<Drawable>() {
//                    @Override
//                    public void onResourceReady(@NonNull Drawable resource
//                            , Transition<? super Drawable> transition) {
//                        iGlideLoadedLisenter.onLoaded(resource);
//                    }
//                });
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ?
                        Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    // 设置选项
    private RequestOptions getOptions() {
        return new RequestOptions()
                .dontAnimate()
                .placeholder(R.drawable.ic_no_picture)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.drawable.ic_no_picture);
    }

    public interface IGlideLoadedLisenter {
        void onLoaded(Drawable drawable);
    }
}

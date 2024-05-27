package com.mobilepulse.gestioncine.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.mobilepulse.gestioncine.interfaces.OnItemClickListener;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {

    private final List<String> imagePaths;
    private final OnItemClickListener listener;

    public ImagePagerAdapter(List<String> imagePaths, OnItemClickListener listener) {
        this.imagePaths = imagePaths;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(container.getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(container.getContext())
                .load(imagePaths.get(position))
                .into(imageView);
        container.addView(imageView);

        imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });

        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
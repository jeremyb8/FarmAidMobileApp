package com.uct.jeremy.farmaid;

/**
 * Created by rushd on 29/09/2016.
 * Adapted by Jeremy Bishop
 * Helper class that populates the contents of TutorialActivity
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {
    private ArrayList<ImageRef> galleryList;
    private Context context;
    private String[] stepsText = null;
    private ArrayList<Bitmap> tutImages;

    public TutorialAdapter(Context context, ArrayList<ImageRef> galleryList) {
        this.galleryList = galleryList;
        this.context = context;
    }

    public TutorialAdapter(Context context, ArrayList<Bitmap> tutImages, String[] stepsText) {
        this.tutImages = tutImages;
        this.stepsText = stepsText;
        this.context = context;
    }

    @Override
    public TutorialAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tutorial_step_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TutorialAdapter.ViewHolder viewHolder, final int i) {

        if (stepsText == null) viewHolder.title.setText(galleryList.get(i).getImage_title());
        else viewHolder.title.setText(stepsText[i]);

        viewHolder.img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (tutImages!=null){
            viewHolder.img.setImageBitmap(tutImages.get(i));
        }
    }

    @Override
    public int getItemCount() {
        if (galleryList == null && tutImages != null){
            return tutImages.size();
        }
        else if(tutImages == null && galleryList != null){
            return galleryList.size();
        }
        else return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private ImageButton img;
        public ViewHolder(View view) {
            super(view);
            title = (TextView)view.findViewById(R.id.title);
            img = (ImageButton) view.findViewById(R.id.img);
        }
    }

}
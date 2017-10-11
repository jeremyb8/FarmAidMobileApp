package com.uct.jeremy.farmaid;

/**
 * Created by rushd on 29/09/2016.
 * Adapted by Jeremy Bishop
 * Helper class that populates the recyclable grid in CropsDisplayActivity
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class CropsDisplayAdapter extends RecyclerView.Adapter<CropsDisplayAdapter.ViewHolder> {
    private ArrayList<Bitmap> bitmapList;
    private ArrayList<String> bitmapNames;
    private File[] cropFolders;
    private Context context;
    private String email = "";
    private String farmName = "";

    public CropsDisplayAdapter(Context context, ArrayList<Bitmap> bitmapList,
                               ArrayList<String> bitmapNames, File[] cropFolders, String email,
                               String farmName) {
        this.bitmapList = bitmapList;
        this.bitmapNames = bitmapNames;
        this.cropFolders = cropFolders;
        this.context = context;
        this.email = email;
        this.farmName = farmName;
    }

    @Override
    public CropsDisplayAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CropsDisplayAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int padding = context.getResources().getDimensionPixelOffset(R.dimen.thumbnail_padding);
        viewHolder.img.setPadding(padding, padding, padding, padding);
        viewHolder.img.setImageBitmap(bitmapList.get(i));
        viewHolder.img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CropViewActivity.class);
                if (i<cropFolders.length){
                    intent.putExtra("dirPath", cropFolders[i].getAbsolutePath());
                    intent.putExtra("cropName", bitmapNames.get(i));
                    intent.putExtra("email", email);
                    intent.putExtra("farmName", farmName);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bitmapList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageButton img;
        public ViewHolder(View view) {
            super(view);
            img = (ImageButton) view.findViewById(R.id.img);
        }
    }

}
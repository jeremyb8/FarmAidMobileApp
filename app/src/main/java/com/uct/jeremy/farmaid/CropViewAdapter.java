package com.uct.jeremy.farmaid;

/**
 * Created by rushd on 29/09/2016.
 * Adapted by Jeremy Bishop
 * Helper class that populates the contents of CropActivity
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
import android.widget.TextView;

import java.util.ArrayList;

public class CropViewAdapter extends RecyclerView.Adapter<CropViewAdapter.ViewHolder> {
    private ArrayList<Bitmap> bitmapList;
    private ArrayList<String> bitmapNames;
    private String dirPath;
    private Context context;
    private String email;
    String farmName = "";

    public CropViewAdapter(Context context, ArrayList<Bitmap> bitmapList,
                           ArrayList<String> bitmapNames, String dirPath, String email, String farmName) {
        this.bitmapList = bitmapList;
        this.bitmapNames = bitmapNames;
        this.dirPath = dirPath;
        this.context = context;
        this.email = email;
        this.farmName = farmName;
    }

    @Override
    public CropViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tutorial_step_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CropViewAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.title.setText(bitmapNames.get(i));
        viewHolder.img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        viewHolder.img.setImageBitmap(bitmapList.get(i));
        viewHolder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TutorialActivity.class);
                intent.putExtra("tutName", bitmapNames.get(i));
                intent.putExtra("dirPath", dirPath+"/tutorial_"+bitmapNames.get(i));
                intent.putExtra("email", email);
                intent.putExtra("farmName", farmName);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bitmapList.size();
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
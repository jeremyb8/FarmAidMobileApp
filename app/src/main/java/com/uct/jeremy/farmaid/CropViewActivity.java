package com.uct.jeremy.farmaid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Activity that handles the layout and logic for an individual crop
 */

public class CropViewActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    Toolbar myToolbar;
    String imageName = "";
    String dirPath = ""; // the location of the folder for this crop (e.g. farm_my.farm/crops/crop_apple)
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    ArrayList<String> bitmapNames = new ArrayList<>();
    String email = "";
    String farmName = "";

    private final String image_titles[] = {
            "Planting", "Soil", "Harvesting", "Watering", "Pests", "Other" };

    private final Integer image_ids[] = {
            R.drawable.planting, R.drawable.soil, R.drawable.harvesting,
            R.drawable.watering, R.drawable.pests, R.drawable.other };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_view);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {

            } else {
                dirPath = extras.getString("dirPath");
                email = extras.getString("email");
                imageName = extras.getString("cropName");
                farmName = extras.getString("farmName");
                if (imageName != null){
                    int index = imageName.indexOf("_");
                    if (index!=-1){
                        this.setTitle(((new String ("" +imageName.charAt(index+1))).toUpperCase()
                                +imageName.substring(index+2)).replace(".png", ""));
                    }
                }
            }
        } else {
            dirPath = (String) savedInstanceState.getSerializable("dirPath");
            imageName = (String) savedInstanceState.getSerializable("cropName");
            email = (String) savedInstanceState.getSerializable("email");
            farmName = (String) savedInstanceState.getSerializable("farmName");
        }

        Log.i(TAG, ">>>>> CropViewActivity: dirPath =\n"+dirPath);

        ImageView iv = (ImageView) findViewById(R.id.imageView);
        File image = new File(dirPath, imageName);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        iv.setImageBitmap(bitmap);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        TextView cropText = (TextView) findViewById(R.id.cropText);
        File text = new File(dirPath, imageName.replace("png", "txt"));
        if (text.exists()){
            try{
                cropText.setText(readFile(text));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            String displayName = imageName.replace("thumbnail", "").replace("_", "").replace(".png", "");
            cropText.setText(displayName+"\nThis is a "+displayName);
        }

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),1);
        recyclerView.setLayoutManager(layoutManager);

        populateTutThumbnails();
        if (bitmapNames.size()>0){
            TextView noTutsText = (TextView)findViewById(R.id.no_tutorials_text);
            noTutsText.setVisibility(View.INVISIBLE);
        }

        CropViewAdapter adapter = new CropViewAdapter(CropViewActivity.this, bitmaps, bitmapNames,
                dirPath, email, farmName);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        switch (id){
            case R.id.logout_action:
                intent = new Intent(CropViewActivity.this, LoginActivity.class);
                break;
            case R.id.message_action:
                intent = new Intent(CropViewActivity.this, ChatActivity.class);
                break;
            case R.id.home_action:
                intent = new Intent(CropViewActivity.this, MainActivity.class);
                break;
            default: break;
        }
        if (intent!=null){
            intent.putExtra("email", email);
            intent.putExtra("farmName", farmName);
            if (id==R.id.logout_action){
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        }
        startActivity(intent);
        finish();
        return super.onOptionsItemSelected(item);
    }

    //    Return the given file as a String
    private String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    /**
     * Check all folders in the currect dirPath folder. If a tutorial folder,  retrieve the index of
     * that tutorial's name in image_titles, and if present there, add a thumbnail with the associated
     * pre-defined thumbnail image
     */
    private void populateTutThumbnails(){
        File cropFolder = new File(dirPath);
        if (cropFolder.exists() && cropFolder.isDirectory()){
            String fileName;
            int index;
            Bitmap image;
            for (File file : cropFolder.listFiles()){
                if (file.isDirectory() && file.getName().startsWith("tutorial")){
                    fileName = file.getName();
                    assert fileName.contains("_");
                    index = getIndexInImageTitles(fileName.substring(fileName.indexOf("_")+1));
                    if (index>=0){
                        image = BitmapFactory.decodeResource(getResources(),image_ids[index]);
                        bitmaps.add(image);
                        bitmapNames.add(image_titles[index]);
                    }
                }
            }
        }
    }

    /**
     * @param tutName the tutorial's name
     * @return the index of that tutorial's name within the list of pre-defined tutorial names
     */
    private int getIndexInImageTitles(String tutName){
        int index = -1;
        for (int i=0;i<image_titles.length;i++) {
            if (image_titles[i].equals(tutName)) {
                index = i;
                break;
            }
        }
        return index;
    }

}
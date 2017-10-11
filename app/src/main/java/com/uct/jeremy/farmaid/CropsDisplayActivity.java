package com.uct.jeremy.farmaid;

/**
 * Created by rushd on 29/09/2016.
 * Adapted by Jeremy Bishop
 * Manages a recyclable grid arrangement of crop thumbnails that can be selected
 */

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
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class CropsDisplayActivity extends AppCompatActivity {
    final String TAG = CropsDisplayActivity.this.toString();
    ArrayList<String> bitmapNames = new ArrayList<>();
    ArrayList<Bitmap> bitmaps;
    File[] cropFolders;
    Toolbar myToolbar;
    String email = "";
    String farmName = "farm_Bfarm";
    String dirPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crops_display);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

//        Extract path of crops directory from Extras
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                dirPath = extras.getString("dirPath");
                email = extras.getString("email");
//                farmName = extras.getString("farmName");
            }
        } else {
            dirPath = (String) savedInstanceState.getSerializable("dirPath");
            email = (String) savedInstanceState.getSerializable("email");
//            farmName = (String) savedInstanceState.getSerializable("farmName");
        }
        Log.i(CropsDisplayActivity.this.toString(), ">>>>> DISPLAY CROPS directoryListing =\n"+dirPath);
// Create an arrayList of Bitmaps of the images of crops available
        bitmaps = loadImagesFromStorage(dirPath);
        if (bitmaps.size()>0){
            TextView textView = (TextView)findViewById(R.id.no_crops_text);
            textView.setVisibility(View.INVISIBLE);
        }
        Log.i(TAG, ">>>>> DISPLAY CROPS directoryPath =\n"+dirPath);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(layoutManager);
        CropsDisplayAdapter adapter = new CropsDisplayAdapter(CropsDisplayActivity.this, bitmaps,
                bitmapNames, cropFolders, email, farmName);
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
                intent = new Intent(CropsDisplayActivity.this, LoginActivity.class);
                break;
            case R.id.message_action:
                intent = new Intent(CropsDisplayActivity.this, ChatActivity.class);
                break;
            case R.id.home_action:
                intent = new Intent(CropsDisplayActivity.this, MainActivity.class);
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

    /**
     * @param path Directory to look inside
     * @return all the images in the supplied directory as a Bitmap ArrayList
     */
    private ArrayList<Bitmap> loadImagesFromStorage(String path)
    {
        File dir = new File(path);
//        File[] cropFolders represents an array of all folders with naming "crop_<cropname> (e.g. crop_apple) in the crops folder
        cropFolders = dir.listFiles();
        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
        if (cropFolders != null) {
            String fileName = "";
            for (File cropFolder : cropFolders) {
                if (cropFolder.isDirectory()){
                    File[] cropResources = cropFolder.listFiles();
                    for (File res : cropResources) {
                        if (!res.isDirectory()) {
                            try {
                                fileName = res.getName();
                                if (fileName.startsWith("thumbnail") && fileName.endsWith("png")) {
                                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(res));
                                    bitmaps.add(b);
                                    bitmapNames.add(fileName);
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
            Log.i(TAG, ">>>>> DISPLAY CROPS directoryListing = null");
        }
        return bitmaps;
    }

}

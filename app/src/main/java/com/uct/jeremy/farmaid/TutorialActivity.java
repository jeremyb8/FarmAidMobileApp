package com.uct.jeremy.farmaid;

/**
 * Activity that handles the layout and logic for an individual tutorial
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TutorialActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    Toolbar myToolbar;
    String email="";
    String farmName = "";
    private final String image_titles[]={"tutone", "tuttwo", "tutthree" };
    private final Integer image_ids[]={R.drawable.tutone, R.drawable.tuttwo, R.drawable.tutthree };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        String dirPath="";
        String tutName="";

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
            } else {
                dirPath = extras.getString("dirPath");
                tutName = extras.getString("tutName");
                email = extras.getString("email");
                farmName = extras.getString("farmName");
            }
        } else {
            email = (String) savedInstanceState.getSerializable("email");
            dirPath = (String) savedInstanceState.getSerializable("dirPath");
            tutName = (String) savedInstanceState.getSerializable("tutName");
            farmName = (String) savedInstanceState.getSerializable("farmName");
        }
        String[] temp = dirPath.split("/");
        File tutTextFile = new File (dirPath, temp[temp.length-1]+".txt");

        String[] stepsText = null;
        ArrayList<Bitmap> tutImages = null;
        if (tutTextFile.exists()) {
            try {
                stepsText = splitSteps(tutTextFile);
                tutImages = getTutImages(tutTextFile.getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.setTitle(tutName);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),1);
        recyclerView.setLayoutManager(layoutManager);

        TutorialAdapter adapter;
        if (tutImages == null){
            ArrayList<ImageRef> theImages = prepareData();
            adapter = new TutorialAdapter(TutorialActivity.this, theImages);
        }
        else{
            adapter = new TutorialAdapter(TutorialActivity.this, tutImages, stepsText);
        }
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
                intent = new Intent(TutorialActivity.this, LoginActivity.class);
                break;
            case R.id.message_action:
                intent = new Intent(TutorialActivity.this, ChatActivity.class);
                break;
            case R.id.home_action:
                intent = new Intent(TutorialActivity.this, MainActivity.class);
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
     * @return an ArrayList<ImageRef> representing the default tut images when none are supplied
     */
    private ArrayList<ImageRef> prepareData(){
        ArrayList<ImageRef> theImages = new ArrayList<>();
        for(int i = 0; i< image_titles.length; i++){
            ImageRef imageRef = new ImageRef();
            imageRef.setImage_title(image_titles[i]);
            imageRef.setImage_ID(image_ids[i]);
            theImages.add(imageRef);
        }
        return theImages;
    }

    /**
     * @param file
     * @return the given file as a String
     * @throws IOException
     */
    private String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

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
     * @param tutTextFile textFile containing the tutorial's steps
     * @return tutorial steps split into a String[]
     * @throws IOException
     */
    private String[] splitSteps (File tutTextFile) throws IOException{
        String tutText = readFile(tutTextFile);
        String[] stepsText = null;
        ArrayList<Integer> indexes = new ArrayList<>();
        int i = tutText.indexOf("Step ");
        while(i >= 0) {
            indexes.add(i);
            i = tutText.indexOf("Step ", i+1);
        }
        if (indexes.size()>0){
            stepsText = new String[indexes.size()];
            for (int j = 0; j<indexes.size(); j++){
                if (j+1 == indexes.size()){
                    stepsText[j]=tutText.substring(indexes.get(j));
                }
                else{
                    stepsText[j]=tutText.substring(indexes.get(j), indexes.get(j+1));
                }
            }
        }
        return stepsText;
    }

    /**
     * @param tutFolder
     * @return all the png images within the given tutorial folder as a ArrayList<Bitmap>
     * @throws FileNotFoundException
     */
    private ArrayList<Bitmap> getTutImages(File tutFolder) throws FileNotFoundException {
        ArrayList<Bitmap> tutImages = new ArrayList<>();
        if (tutFolder != null && tutFolder.isDirectory()){
            ArrayList<String> tutImagePaths = new ArrayList<>();
            File[] temp = tutFolder.listFiles();
            if (temp!=null){
                for (File file : temp){
                    if (!file.isDirectory() && file.getAbsolutePath().endsWith("png")){
                        tutImagePaths.add(file.getAbsolutePath());
                    }
                }
                if(tutImagePaths.size()>0){
                    Collections.sort(tutImagePaths, new Comparator<String>() {
                        @Override
                        public int compare(String s1, String s2) {
                            return s1.compareToIgnoreCase(s2);
                        }
                    });
                    Bitmap b;
                    for (String path: tutImagePaths){
                        b = BitmapFactory.decodeStream(new FileInputStream(new File (path)));
                        tutImages.add(b);
                    }
                }
            }
        }
        return tutImages;
    }

}

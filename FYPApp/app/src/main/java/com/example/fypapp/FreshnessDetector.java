package com.example.fypapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import de.hdodenhof.circleimageview.CircleImageView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class FreshnessDetector extends AppCompatActivity {


    Spinner choiceSpinner;
    ImageView image;
    TextView label;
    CircleImageView image_capture,profile_gallery;
    Handler handler;
    byte[] byteImage;
    String encodedImage;
    String selectedOption;
    Button checkFreshness;
    ProgressBar progressBar;
    LinearLayout icons;
    TextView chooseText;
    String serverIP = "192.168.8.103";
    int serverPORT = 5002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        choiceSpinner = (Spinner) findViewById(R.id.choiceSpinner);
        image =findViewById(R.id.image);
        label =findViewById(R.id.label);
        label.setVisibility(View.GONE);
        image_capture = findViewById(R.id.image_capture);
        profile_gallery = findViewById(R.id.profile_gallery);
        handler = new Handler();
        checkFreshness = findViewById(R.id.checkFreshness);
        progressBar = findViewById(R.id.progressBar_cyclic);
        chooseText = findViewById(R.id.chooseOption);
        icons = findViewById(R.id.icons);
        //disable check freshness button
        checkFreshness.setEnabled(false);

        //if select type choice spinner is touched, then enable check freshness button
        choiceSpinner.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                enableCheckFreshness();
                return false;
            }
        });

        //register listener on gallery icon for loading image from gallery
        profile_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, 1);
            }
        });

        //register listener on camera icon for capturing image from camera
        image_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, 0);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                    Toast.makeText(FreshnessDetector.this, "Camera not opening", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //register listener on check freshness button for sending image to server for freshness prediction
        checkFreshness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject object=new JSONObject();
                try {
                    checkFreshness.setVisibility(View.GONE);
                    choiceSpinner.setVisibility(View.GONE);
                    icons.setVisibility(View.GONE);
                    chooseText.setVisibility(View.GONE);

                    progressBar.setVisibility(View.VISIBLE);
                    //put encoded image in json object
                    object.put("image",encodedImage);
                    //create thread for sending image to server and getting response
                    predictFreshness(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        choiceSpinner.setSelection(0);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    //set image in imageview
                    image.setImageBitmap(imageBitmap);
                    //resize image
                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 224, 224, false);
                    getEncodedImage(imageBitmap); //encode image in base64 format

                    checkFreshness.setVisibility(View.VISIBLE);
                    choiceSpinner.setVisibility(View.VISIBLE);
                    label.setVisibility(View.GONE);

                }

                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    image.setImageURI(selectedImage);
                    try{
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        //resize image for server
                        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);
                        getEncodedImage(bitmap); //encode image in base64 format

                        checkFreshness.setVisibility(View.VISIBLE);
                        choiceSpinner.setVisibility(View.VISIBLE);
                        label.setVisibility(View.GONE);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(FreshnessDetector.this,"Error Occured",Toast.LENGTH_SHORT);
                    }
                }
                break;
        }
    }

    private void predictFreshness(final String img) {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //create socket
                    Socket s=new Socket(serverIP,serverPORT);
                    //get output stream of socket
                    final OutputStream out=s.getOutputStream();
                    //link print writer with output stream for easy sending
                    final PrintWriter writer=new PrintWriter(out);
                    writer.write(img);  //send image to server
                    writer.flush();     //clear buffer
                    BufferedReader data=new BufferedReader(new InputStreamReader(s.getInputStream()));  //reader for server msg
                    final String st=data.readLine();    //read server response in a single line
                    handler.post(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void run() {
                            if(st.trim().length()!=0)   //trim server response by removing unnecessary characters at the end
                            {
                                disableCheckFreshness();        //disable check freshness button
                                selectedOption = choiceSpinner.getSelectedItem().toString(); //get selected item from spinner
                                if(selectedOption.equalsIgnoreCase("Select Type"))
                                {
                                    selectedOption = "Apple";
                                }
                                checkFreshness.setVisibility(View.GONE);
                                choiceSpinner.setVisibility(View.GONE);
                                try {
                                    JSONObject recData=new JSONObject(st);
                                    String lbl=recData.getString("label");  //read data corresponding to "label" key
                                    if(lbl.equalsIgnoreCase("fresh"))
                                    {
                                        label.setText("Fresh "+selectedOption);
                                    }
                                    else if(lbl.equalsIgnoreCase("medium")) {
                                        label.setText("Medium Fresh "+selectedOption);
                                    }
                                    else{
                                        label.setText("Rotten "+selectedOption);
                                    }

                                    progressBar.setVisibility(View.GONE);
                                    icons.setVisibility(View.VISIBLE);
                                    chooseText.setVisibility(View.VISIBLE);
                                    label.setVisibility(View.VISIBLE);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    writer.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    //encode image in base64 format
    public void getEncodedImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byteImage = stream.toByteArray();
        encodedImage = Base64.encodeToString(byteImage, Base64.DEFAULT);
//        Log.d("encodedImage", encodedImage);

    }

    //enable check freshness button
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void enableCheckFreshness(){
        checkFreshness.setEnabled(true);
        checkFreshness.setBackground(ContextCompat.getDrawable(FreshnessDetector.this,R.drawable.black_round));
    }
    //disable check freshness button
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void disableCheckFreshness(){
        checkFreshness.setEnabled(false);
        checkFreshness.setBackground(ContextCompat.getDrawable(FreshnessDetector.this,R.drawable.grey_round));
    }
}
package com.example.fypapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
//    static final int REQUEST_IMAGE_CAPTURE = 2;

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

        profile_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_OPEN_DOCUMENT,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);

                JSONObject object=new JSONObject();
                try {
                    object.put("image",encodedImage);
                    predictFreshness(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        image_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, 0);
                    JSONObject object=new JSONObject();
                    try {
                        object.put("image",encodedImage);
                        predictFreshness(object.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                    Toast.makeText(FreshnessDetector.this, "Camera not opening", Toast.LENGTH_SHORT).show();
                }

//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, 0);

            }
        });


//        String choice = "Apple";
//        choice = String.valueOf(choiceSpinner.getSelectedItem());


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                    image.setImageBitmap(imageBitmap);
                    getEncodedImage(imageBitmap);


//                    Bitmap image = (Bitmap) imageReturnedIntent.getExtras().get("data");
//                    //ImageView imageview = (ImageView) findViewById(R.id.ImageView01); //sets imageview as the bitmap
//                    image.setImageBitmap(image);

                }

                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    image.setImageURI(selectedImage);
                    try{
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        getEncodedImage(bitmap);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(FreshnessDetector.this,"Error Occured",Toast.LENGTH_SHORT);
                    }
//                    image.setPadding(0, 0, 0, 0);
                    //c.setPic(selectedImage.toString());
                }
                break;
        }
    }

    private void predictFreshness(final String img) {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s=new Socket("172.16.49.17",5000);
                    final OutputStream out=s.getOutputStream();
                    final PrintWriter writer=new PrintWriter(out);
                    writer.println(img);
                    writer.flush();
                    BufferedReader data=new BufferedReader(new InputStreamReader(s.getInputStream()));
                    final String st=data.readLine();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(st.trim().length()!=0)
                            {
                                try {
                                    JSONObject recData=new JSONObject(st);
                                    String lbl=recData.getString("label");
                                    if(lbl.equalsIgnoreCase("fresh"))
                                    {
                                        label.setText("Fresh");
                                    }
                                    else if(lbl.equalsIgnoreCase("medium")) {
                                        label.setText("Mild Rotten");
                                    }
                                    else{
                                        label.setText("Rotten");
                                    }
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

    public void getEncodedImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byteImage = stream.toByteArray();
        encodedImage = Base64.encodeToString(byteImage, Base64.DEFAULT);
    }
}
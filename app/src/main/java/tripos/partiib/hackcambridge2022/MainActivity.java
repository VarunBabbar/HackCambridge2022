package tripos.partiib.hackcambridge2022;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.text.MLLocalTextSetting;
import com.huawei.hms.mlsdk.text.MLRemoteTextSetting;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingAnalyzer;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingAnalyzerFactory;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingSetting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    String currentPhotoPath;

    private static final int CAMERA_PERMISSION_CODE = 1001;
    private static final int PICK_IMAGE_REQUEST = 1002;
    private static final int resultCode = -1;
    private Uri mImageUri;
    private Bitmap bitmap;
    Button setImage, analyseImage;
    ImageView imageView;
    private Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar)findViewById(R.id.toolbar_bottom);
        imageView = findViewById(R.id.imageView);

        MLApplication.getInstance().setApiKey("DAEDAB1Z77sEfUBjFRHnjOKQV9yLIRuoWQyr7RTK/0PuLYf8BLAT9cZHkFyG8pdMuZZTkw1nhiRs2oXBbLXUwPYAvsg++TLF1q7fqg==");

//        setImage.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                onCameraClick();
//            }
//        });
//
//        analyseImage.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                testAnalyse();
//            }
//        });

        if ((ActivityCompat.checkSelfPermission( this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)){
            requestPermissions();
        }

        getReceiptWords();


    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    private void testAnalyse() {
//            MLLocalTextSetting setting = new MLLocalTextSetting.Factory()
//                    .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
//                    .setLanguage("en")
//                    .create();
//            MLTextAnalyzer analyser1 = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting);


            List<String> languageList = new ArrayList();
            languageList.add("en");
    // Set parameters.
            MLRemoteTextSetting setting = new MLRemoteTextSetting.Factory()
                    // Set the on-cloud text detection mode.
                    // MLRemoteTextSetting.OCR_COMPACT_SCENE: dense text recognition
                    // MLRemoteTextSetting.OCR_LOOSE_SCENE: sparse text recognition
                    .setTextDensityScene(MLRemoteTextSetting.OCR_LOOSE_SCENE)
                    // Specify the languages that can be recognized, which should comply with ISO 639-1.
                    .setLanguageList(languageList)
                    // Set the format of the returned text border box.
                    // MLRemoteTextSetting.NGON: Return the coordinates of the four corner points of the quadrilateral.
                    // MLRemoteTextSetting.ARC: Return the corner points of a polygon border in an arc. The coordinates of up to 72 corner points can be returned.
                    .setBorderType(MLRemoteTextSetting.ARC)
                    .create();
            MLTextAnalyzer analyzer = MLAnalyzerFactory.getInstance().getRemoteTextAnalyzer(setting);

            MLFrame frame = MLFrame.fromBitmap(bitmap);
            Task<MLText> task = analyzer.asyncAnalyseFrame(frame);

            task.addOnSuccessListener(new OnSuccessListener<MLText>() {
                @Override
                public void onSuccess(MLText text) {
                    MLText tt = text;
                    if(tt.getStringValue() != null) {
                        Toast.makeText(MainActivity.this, tt.getStringValue(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {

                }
            });

    }

//    private void requestBitmap() {
//        Intent intent;
//        if(Build.VERSION.SDK_INT < 19) {
//            intent = new Intent(Intent.ACTION_GET_CONTENT);
//        } else {
//            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        }
//        intent.setType("image/*");
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK && requestCode == 1) {
//            try {
//                InputStream inputStream = getContentResolver().openInputStream(data.getData());
//                bitmap = BitmapFactory.decodeStream(inputStream);
//                imageView.setImageBitmap(bitmap);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private void requestPermissions() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_CODE);
        }
    }

    public void onCameraClick() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent

        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();

        } catch (IOException ex) {
            // Error occurred while creating the File

        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "tripos.partiib.hackcambridge2022",
                    photoFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK)
        {
            galleryAddPic();
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmpFactoryOptions);

            imageView.setImageBitmap(bitmap);

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Log.d("hi", currentPhotoPath.toString());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void getEmbeddings(Set<String> stringSet) {
        MLTextEmbeddingSetting setting = new MLTextEmbeddingSetting.Factory()
                // Set languages that can be recognized. Currently, English (MLTextEmbeddingSetting.LANGUAGE_EN) and Simplified Chinese (MLTextEmbeddingSetting.LANGUAGE_ZH) are supported.
                .setLanguage(MLTextEmbeddingSetting.LANGUAGE_EN)
                .create();
        MLTextEmbeddingAnalyzer analyzer = MLTextEmbeddingAnalyzerFactory.getInstance().getMLTextEmbeddingAnalyzer(setting);
        Task<Map<String, Float[]>> wordVectorBatchTask = analyzer.analyseWordVectorBatch(stringSet);
        HashMap<String, ArrayList<Float>> vectors = new HashMap<String, ArrayList<Float>>();
        wordVectorBatchTask.addOnSuccessListener(new OnSuccessListener<Map<String, Float[]>>() {
            @Override
            public void onSuccess(Map<String, Float[]> wordsVector) {
                // Recognition success processing (Map is HashMap, key is the word to be queried, and value is the corresponding vector result.)

                for (String word :  stringSet) {
                    Float vec[] = wordsVector.get(word);
                    int i = 0;
                    ArrayList<Float> vecList = new ArrayList<>();
                    for (Float f : vec) {
                        vecList.add(i, f);
                    }
                    if(vec.length != 0) {
                        vectors.put(word, vecList);
                        Log.d(word, vec.toString());
                    }

                }
                Map<String, Object> embeddingsMap  = new HashMap<>();
                embeddingsMap.put("embeddings", vectors);
                db.collection("embeddings").document("word_embeddings")
                        .set(embeddingsMap);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // If the recognition fails, handle the exception by referring to step 10 below.
            }
        });
    }

    public void camera(View view) {
        onCameraClick();
    }

    public void data(View view) {
        testAnalyse();
    }

    public void embed(View view) {
        Intent intent = new Intent(this, AnalyticsActivity.class);
        startActivity(intent);
    }

    public void getReceiptWords() {
        final DocumentReference docRef = db.collection("processed_receipts").document("receipt_keywords");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("hi", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    List<String> words = (List<String>) snapshot.getData().get("0");
                    Set<String> stringSet = new HashSet<>(words);
                    getEmbeddings(stringSet);

                } else {
                    Log.d("hi", "Current data: null");
                }
            }
        });
    }
}
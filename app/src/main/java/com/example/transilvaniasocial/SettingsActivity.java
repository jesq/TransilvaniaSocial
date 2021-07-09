package com.example.transilvaniasocial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Toolbar mToolbar;
    private TextView mStatus, mBirthDay;
    private Spinner mGender;
    private String gender[]={"Alege","Bărbat", "Femeie", "Altceva"};
    private ArrayAdapter aa;
    private String selectedGender;
    private Button mSaveSettingsBtn;
    private CircleImageView mProfileImg;
    final static int SelectedPhoto = 1;
    final Calendar myCalendar = Calendar.getInstance();

    private FirebaseFirestore db;
    private DocumentReference userRef;
    private FirebaseAuth mAuth;
    private StorageReference UserProfilePictureRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
        UserProfilePictureRef = FirebaseStorage.getInstance().getReference().child("profile Images");

        mToolbar = (Toolbar) findViewById(R.id.settings_activity_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Setari cont");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = findViewById(R.id.et_settings_status);
        mGender = findViewById(R.id.sp_settings_gender);
        mGender.setOnItemSelectedListener(this);
        aa = new ArrayAdapter(this, R.layout.spinner_item2, gender);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGender.setAdapter(aa);
        mBirthDay = findViewById(R.id.et_settings_birthday);
        mGender = findViewById(R.id.sp_settings_gender);
        mSaveSettingsBtn = findViewById(R.id.btn_settings_save);
        mProfileImg = findViewById(R.id.civ_settings_profile);

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };
        mBirthDay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(SettingsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        mProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPhotoIntent = new Intent();
                selectPhotoIntent.setAction(Intent.ACTION_GET_CONTENT);
                selectPhotoIntent.setType("image/*");
                startActivityForResult(selectPhotoIntent, SelectedPhoto);
            }
        });

        //afisez informatiile contului din baza de date
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null){
                        mStatus.setText(documentSnapshot.getString("status"));
                        mBirthDay.setText(documentSnapshot.getString("birthday"));
                        Picasso.get().load(documentSnapshot.getString("profileImage")).into(mProfileImg);
                    }
                }
            }
        });

        //salvez informatiile contului in baza de date
        mSaveSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InformationIsValid())
                {
                    SaveProfileInfo();
                    Toast.makeText(SettingsActivity.this, "Informatiile au fost salvate!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SettingsActivity.this, "Completati toate campurile!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private boolean InformationIsValid() {
        if (TextUtils.isEmpty(mBirthDay.getText().toString())) {
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SelectedPhoto && resultCode == RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfilePictureRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                userRef.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            if(downloadUrl != null){
                                                Picasso.get().load(downloadUrl).into(mProfileImg);
                                                Map<String, Object> info = new HashMap<>();
                                                info.put("profileImage", downloadUrl);
                                                userRef.set(info, SetOptions.merge());
                                            }
                                            else{
                                                Toast.makeText(SettingsActivity.this, "Incarcati o poza de profil", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }


    }

    private void SaveProfileInfo() {
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot != null){
                        Map<String, Object> info = new HashMap<>();
                        info.put("status", mStatus.getText().toString());
                        info.put("birthday", mBirthDay.getText().toString());
                        if(!selectedGender.equals("Alege")){
                            info.put("gender", selectedGender);
                        }
                        userRef.set(info, SetOptions.merge());
                    }
                }
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        mBirthDay.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedGender = gender[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
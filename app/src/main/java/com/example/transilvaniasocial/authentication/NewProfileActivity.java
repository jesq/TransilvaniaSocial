package com.example.transilvaniasocial.authentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.transilvaniasocial.MainActivity;
import com.example.transilvaniasocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private String UserID;
    private StorageReference UserProfilePictureRef;
    DocumentReference usersReference;
    private Spinner facultySpinner;
    private CircleImageView ProfilePicture;
    private EditText LastName, FirstName;
    private Button BtnSaveInfo;
    private ArrayAdapter aa;

    final static int SelectedPhoto = 1;
    String[] faculty = {"Facultatea de Matematică și informatică", "Facultatea Design de produs și mediu", "Facultatea de Inginerie electrică și știința calculatoarelor",
    "Facultatea de Design de mobilier și inginerie a lemnului", "Facultatea de Inginerie mecanică", "Facultatea de Inginerie tehnologică și management industrial",
    "Facultatea de Silvicultură și exploatări forestiere", "Facultatea de Știinta și ingineria materialelor", "Facultatea de Drept",
    "Facultatea de Educație fizică și sporturi montane", "Facultatea de Litere", "Facultatea de Medicină", "Facultatea de Muzică",
    "Facultatea de Psihologie și științele educației", "Facultatea de Sociologie și comunicare", "Facultatea de Științe economice și administrarea afacerilor",
    "Facultatea de Alimentație și turism", "Facultatea de Construcții"};
    private String selectedFaculty;
    String downloadUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        UserID = mAuth.getCurrentUser().getUid();
        UserProfilePictureRef = FirebaseStorage.getInstance().getReference().child("profile Images");
        usersReference = fStore.collection("users").document(UserID);

        facultySpinner = (Spinner) findViewById(R.id.spinner_newProfile_Faculty);
        facultySpinner.setOnItemSelectedListener(this);
        aa = new ArrayAdapter(this, R.layout.spinner_item, faculty);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        facultySpinner.setAdapter(aa);
        ProfilePicture = (CircleImageView) findViewById(R.id.civ_newProfile);
        LastName = (EditText) findViewById(R.id.et_newProfile_lastName);
        FirstName = (EditText) findViewById(R.id.et_newProfile_firstName);
        BtnSaveInfo = (Button) findViewById(R.id.btn_save_info);

        BtnSaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    SaveProfileInformation();
            }
        });

        ProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPhotoIntent = new Intent();
                selectPhotoIntent.setAction(Intent.ACTION_GET_CONTENT);
                selectPhotoIntent.setType("image/*");
                startActivityForResult(selectPhotoIntent, SelectedPhoto);
            }
        });

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
                final StorageReference filePath = UserProfilePictureRef.child(UserID + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                usersReference.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            if(downloadUrl != null){
                                                Picasso.get().load(downloadUrl).placeholder(R.drawable.placeholderprofile).into(ProfilePicture);
                                            }
                                            else{
                                                Toast.makeText(NewProfileActivity.this, "Incarcati o poza de profil", Toast.LENGTH_SHORT).show();
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

    private void SaveProfileInformation() {
        boolean validFirstName = true, validLastName = true, validFaculty = true;
        if(TextUtils.isEmpty(FirstName.getText().toString()) || !FirstName.getText().toString().matches("^[A-Za-z]+$")){
            FirstName.setError("Te rog completeaza un prenume valid!");
            FirstName.requestFocus();
            validFirstName = false;
        }
        if(TextUtils.isEmpty(LastName.getText().toString()) || !LastName.getText().toString().matches("^[A-Za-z]+$")){
            LastName.setError("Te rog completeaza un nume valid!");
            LastName.requestFocus();
            validLastName = false;
        }
        if(TextUtils.isEmpty(selectedFaculty)){
            validFaculty = false;
        }
        if(validFirstName && validLastName && validFaculty){
            Map<String, Object> user = new HashMap<>();
            String email = mAuth.getCurrentUser().getEmail().toString();
            String fullName = FirstName.getText().toString().trim() + " " + LastName.getText().toString().trim();
            user.put("email", email);
            user.put("fullName", fullName);
            user.put("faculty", selectedFaculty);
            user.put("profileImage", downloadUrl);
            usersReference.set(user);
            OpenMainActivity();
            Toast.makeText(NewProfileActivity.this, "Informatiile au fost salvate cu succes!", Toast.LENGTH_SHORT).show();
        }
    }

    private void OpenMainActivity() {
        Intent mainIntent = new Intent(NewProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedFaculty = faculty[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(NewProfileActivity.this, "Selecteaza o facultate", Toast.LENGTH_SHORT).show();
        facultySpinner.requestFocus();
    }
}
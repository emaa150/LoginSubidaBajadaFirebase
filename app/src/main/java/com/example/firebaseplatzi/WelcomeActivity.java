package com.example.firebaseplatzi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class WelcomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;

    private String TAG = "WelcomeActivity";
    private TextView userDetail;
    private Button btonSingOut;
    private ImageView imvPhotoGoogle;
    private Button btonDescargar, btonCargar;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        userDetail = (TextView) findViewById(R.id.tvUser);
        btonSingOut = (Button) findViewById(R.id.singOut);
        imvPhotoGoogle = (ImageView) findViewById(R.id.imvPhoto);
        btonDescargar = (Button) findViewById(R.id.donwload);
        btonCargar = (Button) findViewById(R.id.upload);

        inicializate();

        storageReference = FirebaseStorage.getInstance().getReference();

        btonSingOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutClass();
            }
        });

        btonDescargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File file;
                try {
                    file = File.createTempFile("roboot", "jpg");
                    storageReference.child("roboot.jpg").getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                            imvPhotoGoogle.setImageBitmap(bitmap);


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Ocurrio un error al mostrar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        btonCargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference  astronautaRef= storageReference.child("astronauta_de_platzi.png");
                imvPhotoGoogle.setDrawingCacheEnabled(true);
                imvPhotoGoogle.buildDrawingCache();

                Bitmap bitmap = imvPhotoGoogle.getDrawingCache();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] fotosubidaByte = bos.toByteArray();

                UploadTask uploadTask = astronautaRef.putBytes(fotosubidaByte);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Ocurrio un Error en la Subida", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "La imagen se cargo Correctamente", Toast.LENGTH_SHORT).show();
                        String downLoadUri = taskSnapshot.getStorage().getDownloadUrl().toString();
                        Toast.makeText(getApplicationContext(), downLoadUri, Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });


    }

    private void signOutClass() {
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Error in Google Sign Out", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void inicializate() {

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser !=null){

                    userDetail.setText("ID USER: " + firebaseUser.getUid() + "Email: " + firebaseUser.getEmail());
                    Picasso.with(WelcomeActivity.this).load(firebaseUser.getPhotoUrl()).into(imvPhotoGoogle);



                }else{
                    Log.w(TAG, "onAuthStateChanged - signed_out");
                }
            }
        };

        // inicializacion de Google account

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

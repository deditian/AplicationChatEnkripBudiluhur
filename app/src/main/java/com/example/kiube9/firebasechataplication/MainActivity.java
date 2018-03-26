package com.example.kiube9.firebasechataplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email,password,name;
    private Button signin, signup;
    FirebaseUser user;
    static String LoggedIn_User_Email;

    private ProgressDialog progressDialog;
    EditText regnama,regemail,regpass,regNoTelp,valNama;
    Button regOK, valOK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        OneSignal.startInit(this).init();



        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance(); // important Call

        signin = (Button)findViewById(R.id.signin);
        signup = (Button)findViewById(R.id.signup);



        email = (EditText)findViewById(R.id.etEmail);
        password = (EditText)findViewById(R.id.etPassword);
        name = (EditText)findViewById(R.id.etName);



        user = mAuth.getCurrentUser();
        Log.d("LOGGED", "user: " + user);


        //Setting the tags for Current User.
        if (user != null) {
            LoggedIn_User_Email =user.getEmail();
        }
        OneSignal.sendTag("User_ID", LoggedIn_User_Email);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getemail = email.getText().toString().trim();
                String getepassword = password.getText().toString().trim();
                callsignin(getemail,getepassword);

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getnama = name.getText().toString().trim();
                String getemail = email.getText().toString().trim();
                String getepassword = password.getText().toString().trim();
                callsignup(getnama,getemail,getepassword);

            }
        });


    }

    //Create Account
    private void callsignup(String nama, final String email, final String password)
    {if (TextUtils.isEmpty(nama) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
        Toast.makeText(this, "Isi semua data",Toast.LENGTH_SHORT).show();
    }else {
        try {
            String hash = Sha1Hex.makeSHA1Hash(email + password);
            final String Karakter_5 = hash.substring(0, 5);
            Log.i("validasi", Karakter_5);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_validasi, null);
            AlertDialog.Builder builder1 = builder.setTitle("Validation Code : \n(Please Check your Email)").setView(view);
            final AlertDialog alertDialog = builder1.create();
            alertDialog.show();
            valNama = (EditText)alertDialog.findViewById(R.id.edt_Validasi);
            valOK = (Button)alertDialog.findViewById(R.id.btnValidasi);
            valOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.setMessage("Proses Registrasi ......");
                    progressDialog.show();
                    final  String validation_mahasiswa = valNama.getText().toString();
                    if(validation_mahasiswa.equals(Karakter_5)){

                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "Signed up Failed", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                        else
                                        {
                                            userProfile();
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            String UserID=user.getEmail().replace("@","").replace(".","");
                                            DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

                                            DatabaseReference ref1= mRootRef.child("Users").child(UserID);
                                            ref1.child("Name").setValue(name.getText().toString().trim());
                                            ref1.child("Image_Url").setValue("Null");
                                            ref1.child("Email").setValue(user.getEmail());

                                            Toast.makeText(MainActivity.this,"Berhasil Registrasi",Toast.LENGTH_LONG).show();
                                            progressDialog.dismiss();
                                            alertDialog.cancel();
                                        }
                                    }
                                });

                    }
                    else{
                        Snackbar.make(v, "Belum Berhasil Registrasi, cek kembali kode Anda", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        //  Log.i("test","TIDAK SAMA :"+validation_mahasiswa+": :"+chiperText+":");
                    }
                }
            });
            SendMail sm = new SendMail(MainActivity.this, email, "Chat Budi Luhur Configuration", "Welcome, " + nama + "\nSalam Budi Luhur Silahkan salin Kode tersebut ke app\n" + Karakter_5 + "\n Terima Kasih");
            sm.execute();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    }

    //Set UserDisplay Name
    private void userProfile()
    {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!= null)
        {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name.getText().toString().trim())
                    //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))  // here you can set image link also.
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("TESTING", "User profile updated.");
                            }
                        }
                    });
        }
    }


    //Now start Sign In Process
    //SignIn Process
    private void callsignin(String email,String password) {

        if(email.isEmpty() || password.isEmpty()){
        Toast.makeText(this,"Data harus diisi",Toast.LENGTH_SHORT).show();

        }
        else {
            progressDialog.setMessage("SignIn........");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("TESTING", "sign In Successful:" + task.isSuccessful());
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                progressDialog.dismiss();
                                Log.w("TESTING", "signInWithEmail:failed", task.getException());
                                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.dismiss();
                                Intent i = new Intent(MainActivity.this, ChatActivity.class);
                                finish();
                                startActivity(i);
                            }
                        }
                    });

        }
        }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setIcon(R.mipmap.ic_launcher_round);
        alertDialogBuilder.setTitle("KELUAR APLIKASI");
        alertDialogBuilder
                .setMessage("Apakah Anda Yakin ?")
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setPositiveButton("Ya",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("Tidak",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}

package com.pratik.healthtrackingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {

    String mVerificationId;
    private FirebaseAuth mAuth;
    private EditText editTextPhone, editTextCode;
    private MaterialButton Buttonverify, ButtongetCode;
    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                editTextCode.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            mVerificationId = s;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        editTextPhone = findViewById(R.id.editPhoneNumber);
        editTextCode = findViewById(R.id.vericode);
        Buttonverify = findViewById(R.id.ButtonVerify);
        ButtongetCode = findViewById(R.id.ButtonGetCode);

        mAuth = FirebaseAuth.getInstance();

        ButtongetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = editTextPhone.getText().toString().trim();

                if (mobile.isEmpty() || mobile.length() < 10) {
                    editTextPhone.setError("Enter valid Phone Number");
                    editTextPhone.requestFocus();
                    return;
                }
                sendVerificationCode(mobile);

            }
        });

        Buttonverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = editTextCode.getText().toString().trim();
                verifyVerificationCode(code);
            }
        });
    }

    //    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        // [START_EXCLUDE]
//        if (mVerificationInProgress && validatePhoneNumber()) {
//            startPhoneNumberVerification(mPhoneNumberField.getText().toString());
//        }
//        // [END_EXCLUDE]
//    }
//
//    private boolean validatePhoneNumber() {
//        String phoneNumber = mPhoneNumberField.getText().toString();
//        if (TextUtils.isEmpty(phoneNumber)) {
//            mPhoneNumberField.setError("Invalid phone number.");
//            return false;
//        }
    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            Intent intent = new Intent(SignInActivity.this, DashBoardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {

                            //verification unsuccessful.. display an error message

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                String message = "Invalid code entered...";
                            }
                        }
                    }
                });
    }
}

package com.pratik.healthtrackingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity {

    String mVerificationId;
    String checkedBox;
    RadioGroup RadioGroupGender;
    RadioButton rd;
    private FirebaseAuth mAuth;
    private EditText EditTextfullname, EditTextaddress, EditTextphonenumber, EditTextotp, EditTextdate;
    private MaterialButton Buttonregister, Buttongetotp;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                EditTextotp.setText(code);
                //verifying the code
                //verifyVerificationCode(code);
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
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        Buttonregister = findViewById(R.id.buttonverify);
        Buttongetotp = findViewById(R.id.buttonOTPgeneration);
        EditTextfullname = findViewById(R.id.editFullname);
        EditTextdate = findViewById(R.id.editDate);
        EditTextaddress = findViewById(R.id.editAddress);
        EditTextphonenumber = findViewById(R.id.editPhoneNumber);
        EditTextotp = findViewById(R.id.editotp);

        RadioGroupGender = findViewById(R.id.radioGroupGender);
//        final int rdid = RadioGroupGender.getCheckedRadioButtonId();
//        rd = findViewById(rdid);

        Buttongetotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("getOTP", EditTextotp.getText().toString());
                Toast.makeText(SignUpActivity.this, EditTextotp.getText().toString(), Toast.LENGTH_SHORT).show();

                String mobile = EditTextphonenumber.getText().toString().trim();

                if (mobile.isEmpty() || mobile.length() < 10) {
                    EditTextphonenumber.setError("Enter valid Phone Number");
                    EditTextphonenumber.requestFocus();
                    return;
                }
                sendVerificationCode(mobile);
            }
        });

        Buttonregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int checkedRadio = RadioGroupGender.getCheckedRadioButtonId();
                rd = findViewById(checkedRadio);
                checkedBox = rd.getText().toString();

                Log.i("inputdata", EditTextfullname.getText().toString() + " " + EditTextaddress.getText().toString());
                Toast.makeText(SignUpActivity.this, EditTextfullname.getText().toString() + " " + EditTextaddress.getText().toString() + " " + checkedBox, Toast.LENGTH_LONG).show();

                String code = EditTextotp.getText().toString().trim();
                verifyVerificationCode(code);
            }
        });

    }

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
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            setDocument();
                            Intent intent = new Intent(SignUpActivity.this, patientdashboard.class);
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


    public void setDocument() {
        // [START set_document]
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        RadioButton rd = findViewById(RadioGroupGender.getCheckedRadioButtonId());
        Map<String, Object> user = new HashMap<>();
        user.put("Name", EditTextfullname.getText().toString());
        user.put("Birthdate", EditTextdate.getText().toString());
        user.put("Gender", rd.getText().toString());
        user.put("Address", EditTextaddress.getText().toString());

        db.collection("Doctor").document(EditTextphonenumber.getText().toString())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error writing document", e);
                    }
                });
    }
}

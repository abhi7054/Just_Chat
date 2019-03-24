package com.example.android.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class VerifyPhoneNumber extends AppCompatActivity {
    EditText otpEditText;
    Button loginButton;
    String phoneNumber;
    private FirebaseAuth phoneNumberAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);
        otpEditText = (EditText) findViewById(R.id.otpEditText);
        FirebaseApp.initializeApp(this);
        phoneNumberAuth = FirebaseAuth.getInstance();

        phoneNumber = getIntent().getStringExtra("phoneNumber");

        sendVerificationCode(phoneNumber);

        loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = otpEditText.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    otpEditText.setError("Enter valid code");
                    otpEditText.requestFocus();
                    return;
                }

                //verifying the code entered manually
                verifyVerificationCode(code);

            }
        });

    }

    private void sendVerificationCode(String no) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + no,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code from sms
            String code = phoneAuthCredential.getSmsCode();

            //Code not entered automatically
            if (code != null) {
                otpEditText.setText(code);
                //verifying the code
                verifyVerificationCode(code);
                new SendPostRequest(phoneNumber).execute();
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerifyPhoneNumber.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            verificationId = s;
        }

    };
    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        phoneNumberAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyPhoneNumber.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            Intent intent = new Intent(VerifyPhoneNumber.this, ChatActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {

                            //verification unsuccessful.. display an error message

                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }


                        }
                    }
                });
    }

}
class SendPostRequest extends AsyncTask<String, Void, String> {
    InputStream inputStream;
    OutputStream outputStream;
    String number;

    public SendPostRequest(String phoneNumber) {
        number=phoneNumber;

    }

    protected void onPreExecute() {

    }

    protected String doInBackground(String... arg0) {

        try {


            try {
                URL url = new URL("https://6559ycjiyl.execute-api.ap-south-1.amazonaws.com/latest/add_user");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("Phone Number",number);
                Log.d(TAG, "doInBackground: "+postDataParams );
                HttpURLConnection  httpURLConnection = (HttpURLConnection) url.openConnection();
                String userCredentials = "70123104:Dinesh1234321";
                String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes(), Base64.DEFAULT);
                httpURLConnection.setRequestProperty("Authorization", basicAuth);

                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(String.valueOf(postDataParams));
                bufferedWriter.flush();
                int statusCode = httpURLConnection.getResponseCode();
                Log.d("this", " The status code is " + statusCode);
                if (statusCode == 200)
                {
                    inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                    String response =  convertInputStreamToString(inputStream);
                    Log.d("this", "The response is " + response);
                    JSONObject jsonObject = new JSONObject(response);
                    return response;
                }
                else { return null; }
            }
            catch (Exception e)
            { e.printStackTrace(); }
            finally
            {
                try { if (inputStream != null) { inputStream.close(); } if (outputStream != null) { outputStream.close(); } } catch (Exception e) { e.printStackTrace(); } }
        } catch (Exception e) {
            return new String("Exception: " + e.getMessage());
        }
        return null;
    }


    @Override
    protected void onPostExecute(String result) {

    }
    private String convertInputStreamToString(InputStream inputStream)
    { BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try { while ((line = reader.readLine()) != null)
        { sb.append(line).append('\n'); } }
        catch (IOException e) { e.printStackTrace(); }
        finally { try { inputStream.close(); } catch (IOException e) { e.printStackTrace(); } } return sb.toString(); }

}

package com.example.android.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    EditText phoneNumberEditText;
    Button continueButton;
    String phoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        phoneNumberEditText=(EditText)findViewById(R.id.phoneNumberEditText);
        continueButton=(Button)findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = phoneNumberEditText.getText().toString();
                validNo(phoneNumber);
               Intent intent = new Intent(LoginActivity.this,VerifyPhoneNumber.class);
               intent.putExtra("phoneNumber",phoneNumber);
                startActivity(intent);
                Toast.makeText(LoginActivity.this,phoneNumber,Toast.LENGTH_LONG).show();
            }
        });
    }
    private void validNo(String numberEntered){
        if(numberEntered.isEmpty() || numberEntered.length() < 10){
            phoneNumberEditText.setError("Enter a valid mobile");
            phoneNumberEditText.requestFocus();
            return;
        }
    }

}

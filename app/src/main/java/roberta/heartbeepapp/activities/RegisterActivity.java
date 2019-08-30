package roberta.heartbeepapp.activities;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import roberta.heartbeepapp.R;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.login_btn);
        Button registerButton = findViewById(R.id.register_btn);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingScreen(true);
                EditText nameEt = findViewById(R.id.name_et);
                EditText emailEt = findViewById(R.id.email_et);
                EditText passEt = findViewById(R.id.password_et);

                String name = nameEt.getText().toString();
                String email = emailEt.getText().toString();
                String password = passEt.getText().toString();

                //validate credentials
                if(name.length() == 0 || email.length() == 0 || password.length() == 0){
                    Snackbar.make(emailEt, "Please fill all fields!", Snackbar.LENGTH_LONG).show();
                    showLoadingScreen(false);
                }else if(!isEmailValid(email)){
                    Snackbar.make(emailEt, "Please enter a valid email!", Snackbar.LENGTH_LONG).show();
                    showLoadingScreen(false);
                }else if(!isPasswordValid(password)){
                    Snackbar.make(emailEt, "Password must be at least 6 characters!", Snackbar.LENGTH_LONG).show();
                    showLoadingScreen(false);
                }else{
                    signUpUser(email, password, name);
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void signUpUser(String email, String password, final String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            uploadUserName(user, name);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Snackbar.make(loginButton, "Authentication failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void uploadUserName(FirebaseUser user, String name) {
        FirebaseDatabase.getInstance().getReference("users")
                .child("phone")
                .child(Objects.requireNonNull(user).getUid())
                .child("name")
                .setValue(name);
    }

    private void updateUI(FirebaseUser currentUser){
        showLoadingScreen(false);
        if(currentUser != null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showLoadingScreen(Boolean show){
        if(show)
            findViewById(R.id.loading_screen).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.loading_screen).setVisibility(View.GONE);
    }

    public boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password){
        return password.length() > 5;
    }
}

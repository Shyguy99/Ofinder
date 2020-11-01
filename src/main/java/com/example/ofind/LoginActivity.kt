package com.example.ofind

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private var Email: EditText? = null
    private var Password: EditText? = null
    private var Login: Button? = null
    private var userregister: TextView? = null
    private var forgot_pass:TextView?=null
    private var firebaseAuth: FirebaseAuth? = null
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Email = findViewById<View>(R.id.et_email) as EditText
        Password = findViewById<View>(R.id.et_password) as EditText
        Login = findViewById<View>(R.id.btn_login) as Button
        userregister = findViewById<View>(R.id.tv_register) as TextView
        forgot_pass=findViewById<View>(R.id.tv_forgotpass)as TextView
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth!!.currentUser
        progressDialog = ProgressDialog(this)
        if (user != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
        Login!!.setOnClickListener {if(Email!!.text.isEmpty() || Password!!.text.isEmpty()){Toast.makeText(this,"LOGIN OR PASSWORD CAN'T BE EMPTY",Toast.LENGTH_SHORT).show()}
                                    else {validate(Email!!.text.toString(), Password!!.text.toString()) }}
        userregister!!.setOnClickListener { startActivity(Intent(this@LoginActivity, RegisterActivity::class.java)) }
        forgot_pass!!.setOnClickListener({startActivity(Intent(this,Forgot_passActivity::class.java))})

    }

    private fun validate(emailemail: String, passpassword: String) {
        progressDialog!!.setMessage("Progressing....")
        progressDialog!!.show()
        firebaseAuth!!.signInWithEmailAndPassword(emailemail, passpassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                progressDialog!!.dismiss()
                checkEmailVerification()
            } else {
                progressDialog!!.dismiss()
                Toast.makeText(this@LoginActivity, "Login Failed! Try Again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkEmailVerification() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val emailflag = firebaseUser!!.isEmailVerified
        if (emailflag) {
            Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
            intent=Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)

        } else {
            Toast.makeText(this, "Verify Email Address!!!", Toast.LENGTH_SHORT).show()
            firebaseAuth!!.signOut()
        }
    }
}
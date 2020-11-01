package com.example.ofind

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private var username: EditText? = null
    private var useremail: EditText? = null
    private var userpassword: EditText? = null
    private var userrepassword: EditText? = null
    private var regbutton: Button? = null
    private var userlogin: TextView? = null
    private var firebaseAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setupUIViews()
        firebaseAuth = FirebaseAuth.getInstance()
        regbutton!!.setOnClickListener {
            if (validate()) {
                val user_email = useremail!!.text.toString().trim { it <= ' ' }
                val user_password = userpassword!!.text.toString().trim { it <= ' ' }
                firebaseAuth!!.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        sendEmailVerification()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration Failed!! Try again", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        userlogin!!.setOnClickListener { startActivity(Intent(this@RegisterActivity, LoginActivity::class.java)) }
    }

    private fun setupUIViews() {
        username = findViewById<View>(R.id.et_username) as EditText
        useremail = findViewById<View>(R.id.et_useremail) as EditText
        userpassword = findViewById<View>(R.id.et_userpassword) as EditText
        userrepassword = findViewById<View>(R.id.et_repassword) as EditText
        regbutton = findViewById<View>(R.id.btn_register) as Button
        userlogin = findViewById<View>(R.id.tv_userlogin) as TextView
    }

    override fun onBackPressed() {
        finish()
        return
    }

    private fun validate(): Boolean {
        var reslt = false
        val name = username!!.text.toString()
        val email = useremail!!.text.toString()
        val password = userpassword!!.text.toString()
        val repassword = userrepassword!!.text.toString()
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || repassword.isEmpty()) {
            Toast.makeText(this, "Please Enter all Details", Toast.LENGTH_SHORT).show()
        } else {
            if (password == repassword) {
                reslt = true
            } else {
                Toast.makeText(this, "Password do not matched. Re-Enter password", Toast.LENGTH_SHORT).show()
            }
        }
        return reslt
    }

    private fun sendEmailVerification() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@RegisterActivity, "Successfully Register", Toast.LENGTH_SHORT).show()
                Toast.makeText(this@RegisterActivity, "Email Verification Sent", Toast.LENGTH_SHORT).show()
                firebaseAuth!!.signOut()
                finish()
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            } else {
                Toast.makeText(this@RegisterActivity, "Verification Email not Sent", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
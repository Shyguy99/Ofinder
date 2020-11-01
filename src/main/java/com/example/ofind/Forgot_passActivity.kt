package com.example.ofind

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth


class Forgot_passActivity : AppCompatActivity() {

    private var e_mail:TextView?=null
    private var reset_btn:Button?=null
    private var firebaseAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)
        e_mail=findViewById<View>(R.id.email)as TextView
        reset_btn=findViewById<View>(R.id.btn_reset)as Button
        firebaseAuth = FirebaseAuth.getInstance()

        reset_btn!!.setOnClickListener { if (e_mail!!.text.isEmpty()){
           Toast.makeText(this,"Email can't be empty",Toast.LENGTH_SHORT).show()}
        else{
            val emailAddress = e_mail!!.text.toString()

            firebaseAuth!!.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this,"Reset link sent to your email!!",Toast.LENGTH_LONG).show()
                        }
                        else{
                            Toast.makeText(this,"Error!!Try Later",Toast.LENGTH_SHORT).show()

                        }
                    }}
        }
    }

    override fun onBackPressed() {
        finish()
        return
    }
}
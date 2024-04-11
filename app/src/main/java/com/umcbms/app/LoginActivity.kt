package com.umcbms.app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.umcbms.app.Home.MainActivity
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.api.Status
import com.umcbms.app.api.request.LoginRequest

class LoginActivity : AppCompatActivity() {

    private lateinit var etUserName: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var dbHelper: MasterDBHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        var mLastClickLogin: Long = 0L
        dbHelper = MasterDBHelper(this)
        etUserName = findViewById(R.id.etUserName)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        if (getPrefStringData(this, "token") == null) {
            btnLogin.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickLogin < 1000) return@setOnClickListener
                mLastClickLogin = SystemClock.elapsedRealtime()
                Log.e("clickTimes",mLastClickLogin.toString())
                val email = etUserName.text.toString()
                val pass = etPassword.text.toString()

                val loginRequest = LoginRequest()
                loginRequest.email = email
                loginRequest.password = pass

                if (email == "") {
                    etUserName.error = "enter the email"
                } else {
                    if (pass == "") {
                        etPassword.error = "enter the password"
                    } else {
                        showLoader(this, "Login....")
                        loginViewModel.loginApi(loginRequest).observe(this) { resource ->
                            when (resource.status) {
                                Status.SUCCESS -> {
                                    val apiResponse = resource.data?.body()
                                    if (apiResponse != null && apiResponse.code == 1) { // Assuming 1 indicates a successful login
                                        val data = apiResponse.data
                                        if (data != null) {
                                            setPrefStringData(this, "token", data.token)
                                            setPrefBooleanData(this, "superAdmin", data.superAdmin)

                                            val userName = data.user?.name
                                            if (userName != null) {
                                                setPrefStringData(this, "userName", userName)
                                            }
                                            val accessPermission = data.access
                                            if (accessPermission != null) {
                                                accessPermission.forEachIndexed { index, access ->
                                                    val values = ContentValues()
                                                    values.put(
                                                        MasterDBHelper.SUBUNIT_ID,
                                                        access.subunit_id
                                                    )
                                                    values.put(MasterDBHelper.SUBUNIT_NAME, access.subunit_name)
                                                    values.put(
                                                        MasterDBHelper.PERMISSIONS,
                                                        access.permissions.toString()
                                                    )

                                                     dbHelper.insertData(
                                                         MasterDBHelper.ACCESS_PERMISSION_TABLE_NAME, values)

                                                }
                                            }
                                            val intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Handle case where ApiResponse data is null
                                            Toast.makeText(this, "Unexpected error occurred", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        // Handle unsuccessful login
                                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                Status.ERROR -> {
                                    Toast.makeText(
                                        this,
                                        "Error: ${resource.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                Status.LOADING -> {
                                    hideLoader()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}
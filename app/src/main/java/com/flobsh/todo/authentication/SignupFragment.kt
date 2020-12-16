package com.flobsh.todo.authentication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.flobsh.todo.MainActivity
import com.flobsh.todo.R
import com.flobsh.todo.network.Api
import kotlinx.coroutines.launch
import kotlin.math.sign

class SignupFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val signUpButton = view.findViewById<Button>(R.id.sign_up)
        signUpButton.setOnClickListener {
            val firstName = view.findViewById<EditText>(R.id.firstname).text
            val lastName = view.findViewById<EditText>(R.id.lastname).text
            val emailText = view.findViewById<EditText>(R.id.email_sign_up).text
            val passwordText = view.findViewById<EditText>(R.id.password_sign_up).text
            val passwordReplicationText = view.findViewById<EditText>(R.id.password_replication_sign_up).text

            when {
                firstName.isEmpty() || firstName.isBlank() -> Toast.makeText(context, "Enter your firstname", Toast.LENGTH_LONG).show()
                lastName.isEmpty() || lastName.isBlank() -> Toast.makeText(context, "Enter your lastname", Toast.LENGTH_LONG).show()
                emailText.isEmpty() || emailText.isBlank() -> Toast.makeText(context, "Enter your email", Toast.LENGTH_LONG).show()
                passwordText.isEmpty() || passwordText.isBlank() -> Toast.makeText(context, "Enter a valid password", Toast.LENGTH_LONG).show()
                passwordReplicationText.isEmpty() || passwordReplicationText.isBlank() -> Toast.makeText(context, "Enter a valid confirmation password", Toast.LENGTH_LONG).show()
                passwordText.toString() != passwordReplicationText.toString() -> Toast.makeText(context, "Passwords don't match", Toast.LENGTH_LONG).show()
                else -> {
                    val signUpForm = SignUpForm(firstName.toString(), lastName.toString(), emailText.toString(), passwordText.toString(), passwordReplicationText.toString())
                    lifecycleScope.launch {
                        val response = Api.INSTANCE.userService.signUp(signUpForm)
                        if (response.isSuccessful) {
                            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                            editor.putString(SHARED_PREF_TOKEN_KEY, response.body()!!.token)
                            editor.apply()

                            val intent = Intent(activity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}
package com.flobsh.todo.authentication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings.Secure.putString
import android.util.Log
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
import com.flobsh.todo.network.UserService
import com.flobsh.todo.userinfo.UserInfoActivity
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val loginButton = view.findViewById<Button>(R.id.login)
        loginButton.setOnClickListener {
            val emailText = view.findViewById<EditText>(R.id.email_text).text
            val passwordText = view.findViewById<EditText>(R.id.password_text).text

            if (emailText.isEmpty() || emailText.isBlank()) {
                Toast.makeText(context, "Enter valid email", Toast.LENGTH_LONG).show()
            }
            else if (passwordText.isEmpty() || passwordText.isBlank()) {
                Toast.makeText(context, "Enter your password", Toast.LENGTH_LONG).show()
            }
            else {
                val loginForm = LoginForm(emailText.toString(), passwordText.toString())
                lifecycleScope.launch {
                    val response = Api.INSTANCE.userService.login(loginForm)
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
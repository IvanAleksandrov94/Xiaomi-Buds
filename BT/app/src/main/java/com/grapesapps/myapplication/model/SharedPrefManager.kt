package com.grapesapps.myapplication.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.grapesapps.myapplication.R

class SharedPrefManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_NAME = "user_name"
    }

    fun saveUserName(name: String) {
        prefs.edit {
            putString(USER_NAME, name)
        }
    }

    fun saveAuthToken(token: String) {
        prefs.edit {
            putString(USER_TOKEN, token)
        }
    }

    fun getUserName(): String? {
        return prefs.getString(USER_NAME, null)

    }

    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)

    }

    fun removeUserName() {
        prefs.edit {
            remove(USER_NAME)
        }
    }

    fun removeAuthToken() {
        prefs.edit {
            remove(USER_TOKEN)
        }
    }
}



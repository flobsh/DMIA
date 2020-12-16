package com.flobsh.todo.userinfo

import androidx.lifecycle.*
import coil.load
import com.flobsh.todo.network.Api
import com.flobsh.todo.network.UserInfo
import kotlinx.coroutines.launch

class UserInfoViewModel : ViewModel() {
    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> = _userInfo

    fun loadUserInfo() {
        viewModelScope.launch {
            val response = Api.INSTANCE.userService.getInfo()
            if (response.isSuccessful) {
                _userInfo.value = response.body()!!
            }
        }
    }

    fun updateUserInfo() {

    }

}
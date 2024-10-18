package com.example.screenshotlocation.viewModel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screenshotlocation.dataModel.UserInfo
import com.example.screenshotlocation.repository.BaseRepository
import com.example.screenshotlocation.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class UserViewModel(private val userRepository: BaseRepository) : ViewModel() {

    private val _userInfoState = MutableLiveData<Resource<Any>>()
    val userInfoState: LiveData<Resource<Any>> = _userInfoState

    fun submitUserInfo(userInfo: UserInfo) {
        _userInfoState.value = Resource.Loading()

        viewModelScope.launch {
            val result = userRepository.createRecord(userInfo)
            _userInfoState.value = handleSubmitUser(result)
        }
    }

    private fun handleSubmitUser(posts: Response<Any>): Resource<Any> {
        if (posts.isSuccessful){
            posts.body()?.let{post ->
                return Resource.Success(post)
            }
        }
        return Resource.Error(posts.message())
    }

}

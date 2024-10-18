package com.example.screenshotlocation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.screenshotlocation.repository.BaseRepository

class UserViewModelProviderFactory(val baseRepository: BaseRepository ):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(baseRepository) as T
    }
}
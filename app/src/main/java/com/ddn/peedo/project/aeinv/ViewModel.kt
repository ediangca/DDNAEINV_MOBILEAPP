package com.ddn.peedo.project.aeinv

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddn.peedo.project.aeinv.model.ItemResponse
import com.ddn.peedo.project.aeinv.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemViewModel : ViewModel() {
    private val _items = MutableLiveData<List<ItemResponse>>()
    val items: LiveData<List<ItemResponse>> get() = _items

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchItem(qrCode: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getItemsByQRCode(qrCode)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _items.postValue(it)  // ✅ Correctly handling a List
                    } ?: run {
                        _errorMessage.postValue("No data found.")
                    }
                } else {
                    _errorMessage.postValue("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Network Error: ${e.message}")
            }
        }
    }
}

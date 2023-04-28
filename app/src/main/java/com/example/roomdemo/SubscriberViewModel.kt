package com.example.roomdemo

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomdemo.db.Subscriber
import com.example.roomdemo.db.SubscriberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriberViewModel(private val repository: SubscriberRepository) : ViewModel(){


    val subscribers = repository.subscribers

    private var isUpdateOrDelete = false
    private lateinit var subscriberToUpdateOrDelete : Subscriber

    val inputName = MutableLiveData<String>()
    val inputEmail = MutableLiveData<String>()

    val saveOrUpdateButtonText = MutableLiveData<String>()
    val clearAllOrDeleteButtonText = MutableLiveData<String>()

    private val statusMessage = MutableLiveData<Event<String>>()
    //status message livedata bu yüzdden bunu düzenleyebiliriz.ancak private olduğu için dışsınıflardan erişemiyoruz.


    val message : LiveData<Event<String>>
        get() = statusMessage
    //dış sınıflar yalnızca livedata yı gözlemleyebilir

    init {
        saveOrUpdateButtonText.value = "Save"
        clearAllOrDeleteButtonText.value = "Clear All"
    }


    fun saveOrUpdate(){

        if (inputName.value==null){
            statusMessage.value = Event("Please enter subscriber's name")
        }else if (inputEmail.value==null){
            statusMessage.value = Event("Please enter subscriber's email")
        }else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.value!!).matches()){
            statusMessage.value = Event("Please enter a correct email address")
        }else{
            if (isUpdateOrDelete){
                subscriberToUpdateOrDelete.name = inputName.value!!
                subscriberToUpdateOrDelete.email = inputEmail.value!!
                update(subscriberToUpdateOrDelete)
            }else{
                val name = inputName.value!!
                val email = inputEmail.value!!
                insert(Subscriber(0,name,email))
                inputName.value = ""
                inputEmail.value = ""
            }
        }








    }

    fun clearAllOrDelete(){
        if(isUpdateOrDelete){
            delete(subscriberToUpdateOrDelete)
        }else{
            clearAll()
        }

    }




    private fun insert(subscriber: Subscriber) = viewModelScope.launch (Dispatchers.IO){
            val newRowId = repository.insert(subscriber)
            withContext(Dispatchers.Main){
                if (newRowId > -1){
                    statusMessage.value = Event("Subscriber Inserted Successfully! $newRowId")
                }else{
                    statusMessage.value = Event("Error Occurred!")
                }

            }
        }


    private fun update(subscriber: Subscriber) = viewModelScope.launch (Dispatchers.IO){
        val numberOfRows = repository.update(subscriber)
        withContext(Dispatchers.Main){
            if (numberOfRows > 0) {
                inputName.value = ""
                inputEmail.value = ""
                isUpdateOrDelete = false
                saveOrUpdateButtonText.value = "Save"
                clearAllOrDeleteButtonText.value = "Clear All"
                statusMessage.value = Event("$numberOfRows Rows Updated Successfully!")
            }else{
                statusMessage.value = Event("Error Occurred!")
            }
        }
    }


    private fun delete(subscriber: Subscriber) = viewModelScope.launch (Dispatchers.IO){
        val numberOfRowsDeleted = repository.delete(subscriber)
        withContext(Dispatchers.Main){
            if (numberOfRowsDeleted > 0) {
                inputName.value = ""
                inputEmail.value = ""
                isUpdateOrDelete = false
                saveOrUpdateButtonText.value = "Save"
                clearAllOrDeleteButtonText.value = "Clear All"
                statusMessage.value = Event("$numberOfRowsDeleted Rows Deleted Successfully")
            }else{
                statusMessage.value = Event("Error Occurred!")
            }
        }
    }


    private fun clearAll() = viewModelScope.launch (Dispatchers.IO){
        val numberOfRowsDeleted = repository.deleteAll()
        withContext(Dispatchers.Main){
            if (numberOfRowsDeleted > 0) {
                statusMessage.value = Event("$numberOfRowsDeleted Rows Deleted Successfully")
            }else{
                statusMessage.value = Event("Error Occurred!")
            }
        }
    }


    fun initUpdateAndDelete(subscriber: Subscriber){
        //güncel butonlar yerine update ve delete butonları geliyor
        inputName.value = subscriber.name
        inputEmail.value = subscriber.email
        isUpdateOrDelete = true
        subscriberToUpdateOrDelete = subscriber
        saveOrUpdateButtonText.value = "Update"
        clearAllOrDeleteButtonText.value = "Delete"
    }


}
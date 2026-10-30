package com.jimac.debtcollect.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {
    private val mutableDisplayName = MutableLiveData("")
    val displayName: LiveData<String> = mutableDisplayName
    fun load(name: String) { if (mutableDisplayName.value.isNullOrEmpty()) mutableDisplayName.value = name }
}

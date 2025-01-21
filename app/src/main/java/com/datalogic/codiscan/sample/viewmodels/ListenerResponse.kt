package com.datalogic.codiscan.sample.viewmodels

import android.icu.util.Calendar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datalogic.codiscan.sample.enums.ListenerType
import kotlinx.coroutines.launch

/** Contains time stamps and selected information from listener responses triggered by the CODiScan Service. */
class ListenerResponse: ViewModel() {
    private val _batteryStatusTime = MutableLiveData<String>()
    private val _connectTime = MutableLiveData<String>()
    private val _deviceDetailsTime = MutableLiveData<String>()
    private val _disconnectTime = MutableLiveData<String>()
    private val _getConfigTime = MutableLiveData<String>()
    private val _pairingCodeTime = MutableLiveData<String>()
    private val _scanTime = MutableLiveData<String>()
    private val _setConfigTime = MutableLiveData<String>()
    private val _connected = MutableLiveData<Boolean>()

    val batteryStatusTime : LiveData<String> = _batteryStatusTime
    val connectTime : LiveData<String> = _connectTime
    val deviceDetailsTime : LiveData<String> = _deviceDetailsTime
    val disconnectTime : LiveData<String> = _disconnectTime
    val getConfigTime : LiveData<String> = _getConfigTime
    val pairingCodeTime : LiveData<String> = _pairingCodeTime
    val scanTime : LiveData<String> = _scanTime
    val setConfigTime : LiveData<String> = _setConfigTime
    val connected : LiveData<Boolean> = _connected


    fun setTime(type: ListenerType, data: String = ""){
        viewModelScope.launch {
            when(type){
                ListenerType.BATTERY -> _batteryStatusTime.value = "Battery Charge: $data\n${getCurrentDateTime()}"
                ListenerType.CONNECT -> {
                    _connected.value = true
                    _connectTime.value = getCurrentDateTime()
                }
                ListenerType.DEVICE -> _deviceDetailsTime.value = "Device ID: $data\n${getCurrentDateTime()}"
                ListenerType.DISCONNECT -> {
                    _connected.value = false
                    _disconnectTime.value = getCurrentDateTime()
                }
                ListenerType.GET -> _getConfigTime.value = "Expected config good?: $data\n${getCurrentDateTime()}"
                ListenerType.PAIRING -> _pairingCodeTime.value = "Pairing Code: $data\n${getCurrentDateTime()}"
                ListenerType.SCAN -> _scanTime.value = "Scan Data: $data\n${getCurrentDateTime()}"
                ListenerType.SET -> _setConfigTime.value = "Status: $data\n${getCurrentDateTime()}"
            }
        }
    }

    private fun getCurrentDateTime(): String {
        return Calendar.getInstance().time.toString()
    }
}
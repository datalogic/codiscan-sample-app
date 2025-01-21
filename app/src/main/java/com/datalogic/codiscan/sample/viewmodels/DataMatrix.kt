package com.datalogic.codiscan.sample.viewmodels

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/** Holds the bitmap of a pairing code formatted into a Data Matrix. Received from the CODiScan Service. */
class DataMatrix: ViewModel() {
    private val _dataMatrixImage = MutableLiveData<ImageBitmap>()
    val dataMatrixImage: LiveData<ImageBitmap> = _dataMatrixImage

    fun setDataMatrix(raw: Bitmap){
        viewModelScope.launch {
            _dataMatrixImage.value = raw.asImageBitmap()
        }
    }
}
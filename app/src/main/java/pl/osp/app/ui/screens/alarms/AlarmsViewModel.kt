package pl.osp.app.ui.screens.alarms

import android.app.Application
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {
    // Your ViewModel implementation here
}

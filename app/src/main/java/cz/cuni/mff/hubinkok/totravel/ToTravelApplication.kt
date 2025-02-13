package cz.cuni.mff.hubinkok.totravel

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class ToTravelApplication : Application(), ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}
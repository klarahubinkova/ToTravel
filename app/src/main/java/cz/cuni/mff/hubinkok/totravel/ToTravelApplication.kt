package cz.cuni.mff.hubinkok.totravel

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Custom Application class that provides a shared ViewModelStore.
 * This allows a single ViewModel instance to be shared across multiple activities.
 */
class ToTravelApplication : Application(), ViewModelStoreOwner {
    /**
     * The ViewModelStore instance that holds ViewModels.
     * This ensures ViewModels persist across different activities within the application.
     */
    override val viewModelStore = ViewModelStore()
}
package com.bookdot.app

import android.app.Application
import com.bookdot.app.data.local.database.MockDataPopulator
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BootDotApplication : Application() {
    
    @Inject
    lateinit var mockDataPopulator: MockDataPopulator
    
    override fun onCreate() {
        super.onCreate()
        
        // Mock 데이터 초기화
        CoroutineScope(Dispatchers.IO).launch {
            mockDataPopulator.populateDatabase()
        }
    }
}
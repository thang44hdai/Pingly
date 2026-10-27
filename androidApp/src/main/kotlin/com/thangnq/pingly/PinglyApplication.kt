package com.thangnq.pingly

import android.app.Application
import com.thangnq.pingly.di.initKoin
import org.koin.android.ext.koin.androidContext

class PinglyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@PinglyApplication)
        }
    }
}

package com.example

import android.app.Application
import android.content.Context
import android.os.Build

class FlowApplication : Application() {
    override fun attachBaseContext(base: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            super.attachBaseContext(base.createAttributionContext("default"))
        } else {
            super.attachBaseContext(base)
        }
    }
}

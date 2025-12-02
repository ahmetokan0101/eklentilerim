package com.keyiflerolsun

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

// @CloudstreamPlugin - Disabled: Only one plugin per project allowed
class TemelPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(Temel())
    }
}
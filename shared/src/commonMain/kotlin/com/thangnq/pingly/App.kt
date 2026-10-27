package com.thangnq.pingly

import androidx.compose.runtime.Composable
import com.thangnq.pingly.feature.home.ui.HomeRoute
import com.thangnq.pingly.ui.theme.PinglyTheme

@Composable
fun App() {
    PinglyTheme {
        HomeRoute()
    }
}

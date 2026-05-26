package com.lianshiji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lianshiji.app.ui.LianShiJiApp
import com.lianshiji.app.ui.MainViewModel
import com.lianshiji.app.ui.MainViewModelFactory
import com.lianshiji.app.ui.theme.LianShiJiTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as LianShiJiApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LianShiJiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LianShiJiApp(viewModel = viewModel)
                }
            }
        }
    }
}

package us.huseli.retaintheme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
@ReadOnlyComposable
fun isInLandscapeMode() = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

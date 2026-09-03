package com.traceapp.android.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.traceapp.android.ui.find.FindScreen
import com.traceapp.android.ui.home.HomeScreen
import com.traceapp.android.ui.home.ObjectMemoryViewModel
import com.traceapp.android.ui.profile.ProfileScreen
import com.traceapp.android.ui.scan.ScanScreen
import com.traceapp.core.auth.AuthUser

private enum class Destination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    SCAN("Scan", Icons.Outlined.PhotoCamera),
    FIND("Tìm", Icons.Outlined.Search),
    PROFILE("Cá nhân", Icons.Outlined.Person),
}

@Composable
fun TraceShell(
    user: AuthUser,
    onLogout: () -> Unit,
    objectViewModel: ObjectMemoryViewModel = hiltViewModel(),
) {
    var destination by remember { mutableStateOf(Destination.HOME) }
    var dataRevision by remember { mutableIntStateOf(0) }
    val objectState by objectViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(user.id, dataRevision) {
        objectViewModel.refresh()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { contentPadding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(contentPadding)) {
            when (destination) {
                Destination.HOME -> HomeScreen(objectState, user.displayName)
                Destination.SCAN -> ScanScreen(
                    onDataChanged = { dataRevision++ },
                    onOpenFind = {
                        objectViewModel.refresh()
                        destination = Destination.FIND
                    },
                )
                Destination.FIND -> FindScreen(objectState, objectViewModel::findLastSeen)
                Destination.PROFILE -> ProfileScreen(user = user, onLogout = onLogout)
            }
        }
    }
}

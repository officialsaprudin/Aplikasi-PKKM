package id.sch.mialirsyad.pkkm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.sch.mialirsyad.pkkm.model.PkkmTab
import id.sch.mialirsyad.pkkm.ui.PkkmViewModel
import id.sch.mialirsyad.pkkm.ui.screens.IsianScreen
import id.sch.mialirsyad.pkkm.ui.screens.MatrixScreen

private val Slate100 = Color(0xFFF1F5F9)
private val Slate400 = Color(0xFF94A3B8)
private val Slate900 = Color(0xFF0F172A)
private val Emerald100 = Color(0xFFD1FAE5)
private val Emerald600 = Color(0xFF059669)
private val Emerald800 = Color(0xFF065F46)
private val Amber100 = Color(0xFFFEF3C7)
private val Amber800 = Color(0xFF92400E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Emerald600,
                    background = Slate100,
                    surface = Color.White
                )
            ) {
                PkkmApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PkkmApp(vm: PkkmViewModel = viewModel()) {
    val currentTab by vm.currentTab.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val isOnlineMode by vm.isOnlineMode.collectAsState()
    val isianData by vm.isianData.collectAsState()
    val indicators by vm.indicators.collectAsState()
    val evidence by vm.evidence.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.messages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        containerColor = Slate100,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = Color.White
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(color = Emerald600, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "SI-PKKM",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text(
                                "PKKM AL-IRSYAD",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                color = if (isOnlineMode) Emerald100 else Amber100,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    if (isOnlineMode) "Supabase Active" else "Cache Lokal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isOnlineMode) Emerald800 else Amber800,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang", tint = Color.White)
                    }
                    IconButton(onClick = { vm.saveIsianData() }) {
                        Icon(Icons.Default.Save, contentDescription = "Simpan Data", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                PkkmTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    val icon = when (tab) {
                        PkkmTab.ISIAN -> Icons.Default.Description
                        PkkmTab.USAHA -> Icons.Default.TrendingUp
                        PkkmTab.MANAJERIAL -> Icons.Default.ManageAccounts
                        PkkmTab.KEWIRAUSAHAAN -> Icons.Default.Lightbulb
                        PkkmTab.SUPERVISI -> Icons.Default.FactCheck
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = { vm.selectTab(tab) },
                        icon = { Icon(icon, contentDescription = tab.shortLabel, modifier = Modifier.size(20.dp)) },
                        label = {
                            Text(
                                tab.shortLabel,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald600,
                            selectedTextColor = Emerald600,
                            indicatorColor = Emerald100,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate100)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Emerald600,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                when (currentTab) {
                    PkkmTab.ISIAN -> IsianScreen(
                        data = isianData,
                        onChange = { vm.updateIsian(it) },
                        onSave = { vm.saveIsianData() }
                    )
                    else -> MatrixScreen(
                        tab = currentTab,
                        indicators = indicators,
                        evidence = evidence,
                        onUpdateScore = { code, score -> vm.updateScore(code, score) },
                        onSaveIndicator = { orig, sub, code, score -> vm.saveIndicator(orig, sub, code, score) },
                        onAddEvidenceTitle = { code, title -> vm.addEvidenceTitle(code, title) },
                        onSaveEvidenceFile = { id, name, mime, bytes, url ->
                            vm.saveEvidenceFileOrUrl(id, name, mime, bytes, url)
                        },
                        onDeleteEvidence = { id -> vm.deleteEvidence(id) }
                    )
                }
            }
        }
    }
}

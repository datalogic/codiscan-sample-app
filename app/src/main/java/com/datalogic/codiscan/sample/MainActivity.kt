package com.datalogic.codiscan.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.datalogic.codiscan.Codiscan
import com.datalogic.codiscan.dataclasses.BatteryData
import com.datalogic.codiscan.dataclasses.DeviceData
import com.datalogic.codiscan.dataclasses.PairingData
import com.datalogic.codiscan.dataclasses.ScanData
import com.datalogic.codiscan.listeners.BatteryStatusListener
import com.datalogic.codiscan.listeners.ConnectListener
import com.datalogic.codiscan.listeners.DeviceDetailsListener
import com.datalogic.codiscan.listeners.DisconnectListener
import com.datalogic.codiscan.listeners.GetConfigListener
import com.datalogic.codiscan.listeners.PairingCodeListener
import com.datalogic.codiscan.listeners.ScanListener
import com.datalogic.codiscan.listeners.SetConfigListener
import com.datalogic.codiscan.sample.enums.ListenerType
import com.datalogic.codiscan.sample.ui.theme.CodiscanSampleAppTheme
import com.datalogic.codiscan.sample.viewmodels.DataMatrix
import com.datalogic.codiscan.sample.viewmodels.ListenerResponse
import kotlinx.serialization.Serializable

/** Sample activity using the functionality of the [Codiscan] SDK. */
class MainActivity : ComponentActivity(), PairingCodeListener, ConnectListener,
    BatteryStatusListener, DisconnectListener, GetConfigListener, SetConfigListener,
    ScanListener, DeviceDetailsListener {
    private val codiscan = Codiscan()
    private val dataMatrixViewModel: DataMatrix by viewModels()
    private val listenerResponseViewModel: ListenerResponse by viewModels()
    private var previousSet = 0
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codiscan.bindService(this){
            this.registerListeners()
            codiscan.deviceManager.triggerPairingObject()
        }
        setContent {
            CodiscanSampleAppTheme {
                val navItems = listOf(
                    BottomNavigationItem(title = "Connect", selectedIcon = Icons.Filled.Bluetooth, unselectedIcon = Icons.Outlined.Bluetooth, notification = false),
                    BottomNavigationItem(title = "Configure", selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings, notification = false),
                    BottomNavigationItem(title = "Listener Tests", selectedIcon = Icons.Filled.Science, unselectedIcon = Icons.Outlined.Science, notification = false)
                )
                var selectedItemIndex by rememberSaveable {
                    mutableStateOf(0)
                }
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                navItems.forEachIndexed { index, item ->
                                    NavigationBarItem(
                                        selected = selectedItemIndex == index,
                                        onClick = {
                                            selectedItemIndex = index
                                            item.notification = false
                                            when(index){
                                                0 -> navController.navigate(ConnectScreen)
                                                1 -> navController.navigate(ConfigureScreen)
                                                2 -> navController.navigate(TestScreen)
                                            }
                                        },
                                        icon = {
                                            BadgedBox(badge = {
                                                if(item.notification) { Badge() }
                                            }) {
                                                Icon(
                                                    imageVector = if(index == selectedItemIndex) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.title
                                                )
                                            }
                                        },
                                        label = {
                                            Text(text = item.title)
                                        }
                                    )
                                }
                            }
                        }
                    ) {
                        NavHost(navController = navController, startDestination = ConnectScreen){
                            composable<ConnectScreen>{
                                ConnectScreen()
                            }
                            composable<ConfigureScreen>{
                                ConfigureScreen()
                            }
                            composable<TestScreen>{
                                TestScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPairingCode(pairingData: PairingData) {
        Log.i("CODISCAN", "Pairing code: ${pairingData.pairingCode}")
        dataMatrixViewModel.setDataMatrix(pairingData.bitmap)
        listenerResponseViewModel.setTime(ListenerType.PAIRING, pairingData.pairingCode)
    }

    override fun onBatteryStatus(batteryData: BatteryData) {
        Log.i("CODISCAN", "Battery percentage: ${batteryData.batteryCharge}")
        listenerResponseViewModel.setTime(ListenerType.BATTERY, batteryData.batteryCharge.toString())
    }

    override fun onConnect() {
        Log.i("CODISCAN", "Connect event received")
        listenerResponseViewModel.setTime(ListenerType.CONNECT)
    }

    override fun onDeviceDetails(deviceData: DeviceData) {
        Log.i("CODISCAN", "Device ID: ${deviceData.deviceId}")
        listenerResponseViewModel.setTime(ListenerType.DEVICE, deviceData.deviceId)
    }

    override fun onDisconnect() {
        Log.i("CODISCAN", "Disconnect event received")
        listenerResponseViewModel.setTime(ListenerType.DISCONNECT)
    }

    override fun onGetConfig(ints: HashMap<Int, Int>, strings: HashMap<Int, String>) {
        Log.i("CODISCAN", "Received get config status")
        var result = true
        when(previousSet){
            0 -> {
                ints.entries.forEach { entry ->
                    Log.i("CODISCAN", "Property ID ${entry.key} has value: ${entry.value}")
                    if(DEFAULT_INTS[entry.key] != entry.value){
                        result = false
                    }
                }
                strings.entries.forEach { entry ->
                    if(DEFAULT_STRINGS[entry.key] != entry.value){
                        result = false
                    }
                }
            }
            1 -> {
                ints.entries.forEach { entry ->
                    if(MIN_INTS[entry.key] != entry.value){
                        result = false
                    }
                }
                strings.entries.forEach { entry ->
                    if(CUSTOM_STRINGS[entry.key] != entry.value){
                        result = false
                    }
                }
            }
            2 -> {
                ints.entries.forEach { entry ->
                    if(MAX_INTS[entry.key] != entry.value){
                        result = false
                    }
                }
                strings.entries.forEach { entry ->
                    if(CUSTOM_STRINGS[entry.key] != entry.value){
                        result = false
                    }
                }
            }
        }
        listenerResponseViewModel.setTime(ListenerType.GET, result.toString())
    }

    override fun onScan(scanData: ScanData) {
        Log.i("CODISCAN", "Scan data: ${scanData.barcodeData}")
        listenerResponseViewModel.setTime(ListenerType.SCAN, scanData.barcodeData)
    }

    override fun onSetConfig(status: Int, message: String) {
        Log.i("CODISCAN", "Set config status $status: $message")
        listenerResponseViewModel.setTime(ListenerType.SET, "$status: $message")
    }

    override fun onResume() {
        super.onResume()
        codiscan.bindService(this){
            this.registerListeners()
            codiscan.deviceManager.triggerPairingObject()
        }
        this.registerListeners()
        codiscan.deviceManager.triggerPairingObject()
    }

    override fun onDestroy() {
        this.deregisterListeners()
        this.codiscan.unbindService(this)
        super.onDestroy()
    }

    /** Register [MainActivity] to receive listener events for the CODiScan SDK. */
    private fun registerListeners(){
        codiscan.deviceManager.registerPairingCodeListener(this)
        codiscan.deviceManager.registerBatteryStatusListener(this)
        codiscan.deviceManager.registerConnectListener(this)
        codiscan.deviceManager.registerDeviceDetailsListener(this)
        codiscan.deviceManager.registerDisconnectListener(this)
        codiscan.deviceManager.registerScanListener(this)
        codiscan.configurationManager.registerGetListener(this)
        codiscan.configurationManager.registerSetListener(this)
    }

    /** Remove [MainActivity] from receiving the listener events of the CODiScan SDK. */
    fun deregisterListeners(){
        codiscan.deviceManager.removePairingCodeListener(this)
        codiscan.deviceManager.removeBatteryStatusListener(this)
        codiscan.deviceManager.removeConnectListener(this)
        codiscan.deviceManager.removeDeviceDetailsListener(this)
        codiscan.deviceManager.removeDisconnectListener(this)
        codiscan.deviceManager.removeScanListener(this)
        codiscan.configurationManager.removeGetListener(this)
        codiscan.configurationManager.removeSetListener(this)
    }

    /** Displays a list of [ListenerStatusCard]s displaying status of listener responses from the CODiScan Service. */
    @Composable
    fun TestScreen(){
        val battery by listenerResponseViewModel.batteryStatusTime.observeAsState()
        val connect by listenerResponseViewModel.connectTime.observeAsState()
        val device by listenerResponseViewModel.deviceDetailsTime.observeAsState()
        val disconnect by listenerResponseViewModel.disconnectTime.observeAsState()
        val get by listenerResponseViewModel.getConfigTime.observeAsState()
        val pairing by listenerResponseViewModel.pairingCodeTime.observeAsState()
        val scan by listenerResponseViewModel.scanTime.observeAsState()
        val set by listenerResponseViewModel.setConfigTime.observeAsState()
        Column(modifier = Modifier.fillMaxSize(1f).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ListenerStatusCard(title = "Battery", status = battery)
            ListenerStatusCard(title = "Connect", status = connect)
            ListenerStatusCard(title = "Device Details", status = device)
            ListenerStatusCard(title = "Disconnect", status = disconnect)
            ListenerStatusCard(title = "Get Config", status = get)
            ListenerStatusCard(title = "Pairing", status = pairing)
            ListenerStatusCard(title = "Scan Data", status = scan)
            ListenerStatusCard(title = "Set Config", status = set)
        }
    }

    /** Card that contains status of an individual listener response from the CODiScan service. */
    @Composable
    fun ListenerStatusCard(title: String, status: String?){
        val received: Color = if(status == null) Color.Red else Color.Green
        val brush = Brush.horizontalGradient(listOf(Color.Red, Color.Blue))
        Box(modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(50.dp)
            .border(width = 4.dp, brush = brush, shape = RoundedCornerShape(8.dp))
            .padding(4.dp)
        ){
            Row(modifier = Modifier.fillMaxWidth(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start){
                    Text(text = "$title:", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp).padding(start = 4.dp))
                    Text(text = status ?: "", fontSize = 9.sp, lineHeight = 12.sp, modifier = Modifier.width(160.dp))
                }
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center){
                    Box(modifier = Modifier
                        .size(32.dp)
                        .background(received, shape = RoundedCornerShape(8.dp))
                    ){}
                }
            }
        }
    }

    /** Screen contains set/get buttons for configuring the CODiScan to block values. */
    @Composable
    fun ConfigureScreen(){
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Set Configuration Values:", modifier = Modifier.padding(start = 8.dp, top = 8.dp), fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth()){
                Button(modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(1f)
                    .padding(12.dp), onClick = { codiscan.configurationManager.set(DEFAULT_INTS, DEFAULT_STRINGS); previousSet = 0 }) {
                    Text(text = "Default", fontSize = 12.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(0.5f)
                    .padding(12.dp),
                    onClick = { codiscan.configurationManager.set(MIN_INTS, CUSTOM_STRINGS); previousSet = 1 }) {
                    Text(text = "Min", fontSize = 12.sp)
                }
                Button(modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(1f)
                    .padding(12.dp),
                    onClick = { codiscan.configurationManager.set(MAX_INTS, CUSTOM_STRINGS); previousSet = 2 }) {
                    Text(text = "Max", fontSize = 12.sp)
                }
            }
            Text(text = "Get Configuration Values:", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(1f)
                    .padding(12.dp), onClick = { codiscan.configurationManager.get(PropertyList) },
                ) {
                    Text(text = "Get All", fontSize = 12.sp)
                }
            }
        }
    }

    /** Displays the pairing code Data Matrix and trigger buttons for the CODiScan. */
    @Composable
    fun ConnectScreen(){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DataMatrix()
            Column(modifier = Modifier.height(300.dp)) {
                Text(text = "Device Triggers:", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Button(modifier = Modifier
                        .height(80.dp)
                        .padding(12.dp), onClick = { codiscan.deviceManager.triggerBatteryStatus() }) {
                        Text(text = "Battery\nStatus", fontSize = 12.sp)
                    }
                    Button(modifier = Modifier
                        .height(80.dp)
                        .padding(12.dp), onClick = { codiscan.deviceManager.triggerDeviceDetails() }) {
                        Text(text = "Device\nDetails", fontSize = 12.sp)
                    }
                    Button(modifier = Modifier
                        .height(80.dp)
                        .padding(12.dp), onClick = { codiscan.deviceManager.findMyDevice() }) {
                        Text(text = "Find My\nDevice", fontSize = 12.sp)
                    }
                }
                Button(modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth()
                    .padding(12.dp), onClick = { codiscan.deviceManager.triggerDisconnect(); codiscan.deviceManager.triggerPairingObject() }) {
                    Text(text = "Disconnect", fontSize = 12.sp)
                }
            }
        }
    }

    /** Displays a pairing code data matrix, or a QR Code icon as a placeholder if one is not received. */
    @Composable
    fun DataMatrix(){
        val image by dataMatrixViewModel.dataMatrixImage.observeAsState()
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(260.dp), contentAlignment = Alignment.Center) {
            if(image == null){
                Icon(
                    imageVector = Icons.Filled.QrCode,
                    contentDescription = "Placeholder for connect DataMatrix",
                    modifier = Modifier
                        .width(240.dp)
                        .height(240.dp)
                )
            } else {
                Image(image!!,"", modifier = Modifier
                    .width(240.dp)
                    .height(240.dp))
            }
        }
    }
}

/** Data class handles the data associated with an icon of the bottom sheet. */
data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    var notification: Boolean
)

@Serializable
object ConnectScreen

@Serializable
object ConfigureScreen

@Serializable
object TestScreen
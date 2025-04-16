package com.datalogic.codiscan.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.datalogic.codiscan.Codiscan
import com.datalogic.codiscan.constants.ALREADY_CONNECTED
import com.datalogic.codiscan.constants.DISCONNECT_ERROR
import com.datalogic.codiscan.constants.SDK_NOT_BOUND_ERROR
import com.datalogic.codiscan.constants.SUCCESS
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
import com.datalogic.codiscan.sample.ui.theme.DLBlue
import com.datalogic.codiscan.sample.ui.theme.DLGray
import com.datalogic.codiscan.sample.viewmodels.DataMatrix
import com.datalogic.codiscan.sample.viewmodels.ListenerResponse
import kotlinx.serialization.Serializable

/** Sample activity using the functionality of the [Codiscan] SDK. */
class MainActivity : ComponentActivity(), PairingCodeListener, ConnectListener,
    BatteryStatusListener, DisconnectListener, GetConfigListener, SetConfigListener,
    ScanListener, DeviceDetailsListener {
    private val tag = "CODISCAN"
    private val codiscan = Codiscan()
    private val dataMatrixViewModel: DataMatrix by viewModels()
    private val listenerResponseViewModel: ListenerResponse by viewModels()
    private var previousSet = 0
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codiscan.bindService(this){
            this.threadSafeToast("Bound to the CODiScan Service.")
            this.registerListeners()
            this.handleConnectionStatus(codiscan.deviceManager.triggerPairingObject(), false)
        }
        setContent {
            CodiscanSampleAppTheme {
                val navItems = listOf(
                    BottomNavigationItem(title = "Connect", selectedIcon = Icons.Filled.Bluetooth, unselectedIcon = Icons.Outlined.Bluetooth, notification = false),
                    BottomNavigationItem(title = "Configure", selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings, notification = false),
                    BottomNavigationItem(title = "Listeners", selectedIcon = Icons.Filled.Science, unselectedIcon = Icons.Outlined.Science, notification = false),
                    BottomNavigationItem(title = "Logs", selectedIcon = Icons.AutoMirrored.Filled.Article, unselectedIcon = Icons.AutoMirrored.Outlined.Article, notification = false)
                )
                var selectedItemIndex by rememberSaveable {
                    mutableIntStateOf(0)
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
                                                3 -> navController.navigate(LogScreen)
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
                            composable<LogScreen> {
                                LogScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPairingCode(pairingData: PairingData) {
        Log.i(tag, "Pairing code: ${pairingData.pairingCode}")
        writeLog(applicationContext, "Pairing code received: ${pairingData.pairingCode}")
        this.threadSafeToast("pairing code ${pairingData.pairingCode} received.")
        dataMatrixViewModel.setDataMatrix(pairingData.bitmap)
        listenerResponseViewModel.setTime(ListenerType.PAIRING, pairingData.pairingCode)
    }

    override fun onBatteryStatus(batteryData: BatteryData) {
        Log.i(tag, "Battery percentage: ${batteryData.batteryCharge}")
        writeLog(applicationContext, "Battery status received: ${batteryData.batteryCharge}")
        this.threadSafeToast("Battery status received.")
        listenerResponseViewModel.setTime(ListenerType.BATTERY, "Charge: ${batteryData.batteryCharge}, Temp: ${batteryData.batteryTemperature}\nStatus: ${batteryData.batteryStatus}, Profile: ${batteryData.batteryProfile}\nCycle Count: ${batteryData.batteryCycleCount}, Health: ${batteryData.batteryHealth}")
    }

    override fun onConnect() {
        Log.i(tag, "Connect event received")
        writeLog(applicationContext, "Connect event received")
        this.threadSafeToast("Connected to CODiScan device.")
        listenerResponseViewModel.setTime(ListenerType.CONNECT)
        dataMatrixViewModel.setDataMatrix(null)
    }

    override fun onDeviceDetails(deviceData: DeviceData) {
        Log.i(tag, "Device ID: ${deviceData.deviceId}")
        writeLog(applicationContext, "Device details received for device: ${deviceData.deviceId}")
        this.threadSafeToast("Device data received.")
        listenerResponseViewModel.setTime(ListenerType.DEVICE, "Device type: ${deviceData.deviceType}, Device ID: ${deviceData.deviceId},\nFW Version: ${deviceData.fwVersion}")
    }

    override fun onDisconnect() {
        Log.i(tag, "Disconnect event received")
        writeLog(applicationContext, "Disconnect event received")
        this.threadSafeToast("Disconnected from CODiScan device.")
        listenerResponseViewModel.setTime(ListenerType.DISCONNECT)
    }

    override fun onGetConfig(ints: HashMap<Int, Int>, strings: HashMap<Int, String>) {
        Log.i(tag, "Received get config status")
        writeLog(applicationContext, "Received get config status")
        this.threadSafeToast("Configuration values received.")
        var result = "Config match expected values."
        when(previousSet){
            0 -> {
                ints.entries.forEach { entry ->
                    writeLog(applicationContext, "Property ID ${entry.key} has value: ${entry.value}")
                    Log.i(tag, "Property ID ${entry.key} has value: ${entry.value}")
                    if(DEFAULT_INTS[entry.key] != entry.value){
                        result = "Config does not match expected values."
                    }
                }
                strings.entries.forEach { entry ->
                    if(DEFAULT_STRINGS[entry.key] != entry.value){
                        result = "Config does not match expected values."
                    }
                }
            }
            1 -> {
                ints.entries.forEach { entry ->
                    if(MIN_INTS[entry.key] != entry.value){
                        result = "Config does not match expected values."
                    }
                }
                strings.entries.forEach { entry ->
                    if(CUSTOM_STRINGS[entry.key] != entry.value){
                        result = "Config does not match expected values."
                    }
                }
            }
            2 -> {
                ints.entries.forEach { entry ->
                    if(MAX_INTS[entry.key] != entry.value){
                        result = "Config does not match expected values."
                    }
                }
                strings.entries.forEach { entry ->
                    if(CUSTOM_STRINGS[entry.key] != entry.value){
                        result = "Config does not match expected values."
                    }
                }
            }
            3 -> {
                ints.entries.forEach { entry ->
                    writeLog(applicationContext, "Property ID ${entry.key} has value: ${entry.value}")
                    Log.i(tag, "Property ID ${entry.key} has value: ${entry.value}")
                    listenerResponseViewModel.setTime(ListenerType.GET, "Property ID ${entry.key} has value: ${entry.value}")
                }
                strings.entries.forEach { entry ->
                    listenerResponseViewModel.setTime(ListenerType.GET, "Property ID ${entry.key} has value: ${entry.value}")
                }
                return
            }
        }
        listenerResponseViewModel.setTime(ListenerType.GET, result)
    }

    override fun onScan(scanData: ScanData) {
        Log.i(tag, "Scan data: ${scanData.barcodeData}")
        writeLog(applicationContext, "Scan data received: ${scanData.barcodeData}")
        this.threadSafeToast("Scan data received: ${scanData.barcodeData}")
        listenerResponseViewModel.setTime(ListenerType.SCAN, scanData.barcodeData)
    }

    override fun onSetConfig(status: Int, message: String) {
        Log.i(tag, "Set config status $status: $message")
        writeLog(applicationContext, "Set config status received $status: $message")
        this.threadSafeToast("Set configuration status received.")
        listenerResponseViewModel.setTime(ListenerType.SET, "$status: $message")
    }

    override fun onResume() {
        super.onResume()
        codiscan.bindService(this){
            this.registerListeners()
            this.handleConnectionStatus(codiscan.deviceManager.triggerPairingObject(), false)
        }
        this.registerListeners()
        this.handleConnectionStatus(codiscan.deviceManager.triggerPairingObject(), false)
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
    private fun deregisterListeners(){
        codiscan.deviceManager.removePairingCodeListener(this)
        codiscan.deviceManager.removeBatteryStatusListener(this)
        codiscan.deviceManager.removeConnectListener(this)
        codiscan.deviceManager.removeDeviceDetailsListener(this)
        codiscan.deviceManager.removeDisconnectListener(this)
        codiscan.deviceManager.removeScanListener(this)
        codiscan.configurationManager.removeGetListener(this)
        codiscan.configurationManager.removeSetListener(this)
    }

    /**
     * Wrap a toast in UI thread to display to the user.
     * @param message text to display in toast.
     */
    private fun threadSafeToast(message: String){
        this.runOnUiThread(kotlinx.coroutines.Runnable {
            run {
                Toast.makeText(applicationContext,message,Toast.LENGTH_LONG).show();
            }
        })
    }

    /**
     * Helper function to determine if we are connected to a CODiScan device.
     * @param status the return code from a call to a function of the [Codiscan].
     * @param success determines the meaning of [SUCCESS], if true then connected, if false, disconnected.
     */
    private fun handleConnectionStatus(status: Int, success: Boolean){
        when (status) {
            DISCONNECT_ERROR, SDK_NOT_BOUND_ERROR -> {
                listenerResponseViewModel.setConnected(false)
            }
            ALREADY_CONNECTED -> {
                listenerResponseViewModel.setConnected(true)
            }
            SUCCESS -> {
                listenerResponseViewModel.setConnected(success)
            }
        }
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
        val listenerStatusItems = listOf(
            ListenerObject("Battery", battery),
            ListenerObject("Connect", connect),
            ListenerObject("Device Details", device),
            ListenerObject("Disconnect", disconnect),
            ListenerObject("Get Config", get),
            ListenerObject("Pairing", pairing),
            ListenerObject("Scan Data", scan),
            ListenerObject("Set Config", set)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(listenerStatusItems.size) { i ->
                ListenerStatusCard(title = listenerStatusItems[i].title, status = listenerStatusItems[i].status)
            }
        }
    }

    /** Card that contains status of an individual listener response from the CODiScan service. */
    @Composable
    fun ListenerStatusCard(title: String, status: String?){
        val received: Color = if(status == null) Color.Red else Color.Green
        val brush = Brush.horizontalGradient(listOf(Color.Red, Color.Blue))
        Box(modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(60.dp)
            .border(width = 4.dp, brush = brush, shape = RoundedCornerShape(8.dp))
            .padding(4.dp)
        ){
            Row(modifier = Modifier.fillMaxWidth(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start){
                    Text(text = "$title:", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                        .width(120.dp)
                        .padding(start = 4.dp))
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
        var setID by remember { mutableStateOf(TextFieldValue("")) }
        var setValue by remember { mutableStateOf(TextFieldValue("")) }
        var getID by remember { mutableStateOf(TextFieldValue("")) }
        val setFocusRequester = FocusRequester()
        val keyboardController = LocalSoftwareKeyboardController.current

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Set Bulk Configuration Values:", modifier = Modifier.padding(start = 8.dp, top = 8.dp), fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth()){
                TestButton("Default", 1f, onClick = { handleConnectionStatus(codiscan.configurationManager.set(DEFAULT_INTS, DEFAULT_STRINGS), true); previousSet = 0 })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TestButton("Min", 0.5f, onClick = { handleConnectionStatus(codiscan.configurationManager.set(MIN_INTS, CUSTOM_STRINGS), true); previousSet = 1 })
                TestButton("Max", 1f, onClick = { handleConnectionStatus(codiscan.configurationManager.set(MAX_INTS, CUSTOM_STRINGS), true); previousSet = 2 })
            }
            Text(text = "Get Bulk Configuration Values:", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth()) {
                TestButton("Get All", 1f, onClick = { handleConnectionStatus(codiscan.configurationManager.get(PropertyList), true) })
            }
            Text(text = "Set Individual Value:", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = setID,
                    modifier = Modifier.width(125.dp),
                    label = { Text(text = "Property ID") },
                    onValueChange = { input ->
                        if(input.text.all { it.isDigit() })
                        setID = input
                    },
                    keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next, keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions( onNext = { setFocusRequester.requestFocus() })
                )
                OutlinedTextField(
                    value = setValue,
                    modifier = Modifier.width(125.dp).padding(5.dp).focusRequester(setFocusRequester),
                    label = { Text(text = "Value") },
                    onValueChange = {
                        setValue = it
                    },
                    keyboardOptions = KeyboardOptions( imeAction = ImeAction.Done, keyboardType = if(setID.text != "" && isIntegerProperty(setID.text.toInt())) KeyboardType.Number else KeyboardType.Text ),
                    keyboardActions = KeyboardActions( onDone = {
                        writeLog(applicationContext, "Button pressed to set property ID ${setID.text} to ${setValue.text}")
                        if(isIntegerProperty(setID.text.toInt())){
                            handleConnectionStatus(codiscan.configurationManager.set(hashMapOf(setID.text.toInt() to setValue.text.toInt()), hashMapOf()), true)
                        } else {
                            handleConnectionStatus(codiscan.configurationManager.set(hashMapOf(), hashMapOf(setID.text.toInt() to setValue.text)), true)
                        }
                        previousSet = 3
                    })
                )
                CompositionLocalProvider(LocalRippleTheme provides ButtonRippleTheme) {
                    IconButton(
                        onClick = {
                            writeLog(applicationContext, "Button pressed to set property ID ${setID.text} to ${setValue.text}")
                            if(isIntegerProperty(setID.text.toInt())){
                                handleConnectionStatus(codiscan.configurationManager.set(hashMapOf(setID.text.toInt() to setValue.text.toInt()), hashMapOf()), true)
                            } else {
                                handleConnectionStatus(codiscan.configurationManager.set(hashMapOf(), hashMapOf(setID.text.toInt() to setValue.text)), true)
                            }
                            previousSet = 3
                            keyboardController?.hide()
                        },
                        enabled = setID.text != "" && setValue.text != "",
                        modifier = Modifier.padding(4.dp),
                        colors = IconButtonColors(contentColor = DLBlue, containerColor = Color.White, disabledContainerColor = Color.White, disabledContentColor = DLGray)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Edit"
                        )
                    }
                }
            }
            Text(text = "Get Individual Value:", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = getID,
                    modifier = Modifier.width(255.dp),
                    label = { Text(text = "Property ID") },
                    onValueChange = { input ->
                        if(input.text.all { it.isDigit() })
                            getID = input
                    },
                    keyboardOptions = KeyboardOptions( imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions( onDone = {
                        writeLog(applicationContext, "Button pressed to get property ID ${getID.text}")
                        handleConnectionStatus(codiscan.configurationManager.get(intArrayOf(getID.text.toInt())), true)
                        previousSet = 3
                    })
                )
                CompositionLocalProvider(LocalRippleTheme provides ButtonRippleTheme) {
                    IconButton(
                        onClick = {
                            writeLog(applicationContext, "Button pressed to get property ID ${getID.text}")
                            handleConnectionStatus(codiscan.configurationManager.get(intArrayOf(getID.text.toInt())), true)
                            previousSet = 3
                            keyboardController?.hide()
                        },
                        enabled = getID.text != "",
                        modifier = Modifier.padding(4.dp),
                        colors = IconButtonColors(contentColor = DLBlue, containerColor = Color.White, disabledContainerColor = Color.White, disabledContentColor = DLGray)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Edit"
                        )
                    }
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
            ConnectStatus()
            DataMatrix()
            Column(modifier = Modifier.height(300.dp)) {
                Text(text = "Device Triggers:", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    TestButton("Battery\nStatus", onClick = {
                        handleConnectionStatus(codiscan.deviceManager.triggerBatteryStatus(), true)
                        codiscan.deviceManager.triggerBatteryStatus()
                        writeLog(applicationContext, "Button request battery status.")
                    })
                    TestButton("Device\nDetails", onClick = {
                        handleConnectionStatus(codiscan.deviceManager.triggerDeviceDetails(), true)
                        writeLog(applicationContext, "Button request device details.")
                    })
                    TestButton("Find My\nDevice", onClick = {
                        handleConnectionStatus(codiscan.deviceManager.findMyDevice(), true)
                        writeLog(applicationContext, "Button request 'Find My Device'.")
                    })
                }
                TestButton("Disconnect", 1f, onClick = {
                    handleConnectionStatus(codiscan.deviceManager.triggerDisconnect(), false)
                    writeLog(applicationContext, "Button push trigger disconnect.")
                    handleConnectionStatus(codiscan.deviceManager.triggerPairingObject(), false)
                    writeLog(applicationContext, "Button push request pairing object.")
                })
            }
        }
    }

    /** Screen to write custom log messages and clear the log. */
    @Composable
    fun LogScreen(){
        var text by remember { mutableStateOf(TextFieldValue("")) }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    label = { Text(text = "Enter custom log message") },
                    onValueChange = {
                        text = it
                    },
                    keyboardOptions = KeyboardOptions( imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions( onDone = { writeLog(applicationContext, text.text); text = TextFieldValue() })
                )
                CompositionLocalProvider(LocalRippleTheme provides ButtonRippleTheme) {
                    IconButton(
                        onClick = { writeLog(applicationContext, text.text); text = TextFieldValue() },
                        enabled = text.text != "",
                        modifier = Modifier.padding(4.dp),
                        colors = IconButtonColors(contentColor = DLBlue, containerColor = Color.White, disabledContainerColor = Color.White, disabledContentColor = DLGray)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit"
                        )
                    }
                }
            }
            TestButton("Clear Log") {  clearLog(applicationContext) }
        }
    }

    /** View that shows to the user if the mobile device is currently connected to a CODiScan. */
    @Composable
    fun ConnectStatus() {
        val connected by listenerResponseViewModel.connected.observeAsState()
        val received: Color = if(connected == false) Color.Red else Color.Green
        Row {
            Text("Connection Status:", Modifier.padding(12.dp))
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center){
                Box(modifier = Modifier
                    .size(32.dp)
                    .background(received, shape = RoundedCornerShape(8.dp))
                ){}
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
                Text(text = "No pairing barcode.", fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp))
            } else {
                Image(image!!,"", modifier = Modifier
                    .width(240.dp)
                    .height(240.dp))
            }
        }
    }

    /** Button to test a feature of the CODiScan. */
    @Composable
    fun TestButton(
        title: String,
        width: Float = 0f,
        onClick: () -> Unit
    ){
        CompositionLocalProvider(LocalRippleTheme provides ButtonRippleTheme) {
            val modifier = if(width == 0f) {
                Modifier
                    .height(80.dp)
                    .padding(12.dp)
            } else {
                Modifier
                    .height(80.dp)
                    .padding(12.dp)
                    .fillMaxWidth(width)
            }
            Button(
                modifier = modifier,
                onClick = onClick
            ) {
                Text(text = title, fontSize = 12.sp)
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

/** Listener object displayed on the Listener page. */
data class ListenerObject(val title: String, val status: String?)

@Serializable
object ConnectScreen

@Serializable
object ConfigureScreen

@Serializable
object TestScreen

@Serializable
object LogScreen

/** Helper [ButtonRippleTheme] to apply  noticeable ripple to buttons. */
object ButtonRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor(): Color {
        return DLGray
    }

    @Composable
    override fun rippleAlpha(): RippleAlpha {
        return RippleAlpha(0.25f, 0.25f, 0.25f, 0.9f)
    }
}

/** Copy of helper function from the CODiScan SDK to determine which properties are integer. */
fun isIntegerProperty(id: Int): Boolean {
    return (id in 1..183) || (id in 186 .. 189) || (id in 276..299)
}
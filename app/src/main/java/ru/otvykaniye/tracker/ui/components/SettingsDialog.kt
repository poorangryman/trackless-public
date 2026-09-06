package ru.otvykaniye.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.otvykaniye.tracker.TracklessViewModel
import ru.otvykaniye.tracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(viewModel: TracklessViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val activeProfile = state.profiles[state.activeKind] ?: return
    val lang = state.language
    
    var language by remember { mutableStateOf(lang) }
    var dailyLimit by remember { mutableStateOf(activeProfile.dailyLimit.toString()) }
    var wishlistTitle by remember { mutableStateOf(activeProfile.wishlistTitle) }
    var wishlistCost by remember { mutableStateOf(activeProfile.wishlistCost.toString()) }
    var nicotineFormat by remember { mutableStateOf(activeProfile.nicotineFormat) }
    var nicotineDeclaredAmount by remember { mutableStateOf(activeProfile.nicotineDeclaredAmount.toString()) }
    var pouchWeight by remember { mutableStateOf(activeProfile.pouchWeight.toString()) }
    var baseline by remember { mutableStateOf(activeProfile.baseline.toString()) }
    var price by remember { mutableStateOf(activeProfile.price.toString()) }
    var perPack by remember { mutableStateOf(activeProfile.perPack.toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = BgDeep,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(Strings.get(lang, "settings"), color = TextPrimary, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            val updatedProfile = activeProfile.copy(
                                dailyLimit = dailyLimit.toIntOrNull() ?: activeProfile.dailyLimit,
                                wishlistTitle = wishlistTitle,
                                wishlistCost = wishlistCost.toDoubleOrNull() ?: activeProfile.wishlistCost,
                                nicotineFormat = nicotineFormat,
                                nicotineDeclaredAmount = nicotineDeclaredAmount.toDoubleOrNull() ?: activeProfile.nicotineDeclaredAmount,
                                pouchWeight = pouchWeight.toDoubleOrNull() ?: activeProfile.pouchWeight,
                                baseline = baseline.toIntOrNull() ?: activeProfile.baseline,
                                price = price.toDoubleOrNull() ?: activeProfile.price,
                                perPack = perPack.toIntOrNull() ?: activeProfile.perPack
                            )
                            val newProfiles = state.profiles.toMutableMap()
                            newProfiles[state.activeKind] = updatedProfile
                            viewModel.saveSettings(state.copy(language = language, profiles = newProfiles))
                            onDismiss()
                        }) {
                            Text(Strings.get(lang, "save"), color = Emerald, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgCard)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Language
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BgCard).padding(4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (language == "ru") BgDeep else Color.Transparent).clickable { language = "ru" }.padding(12.dp), contentAlignment = Alignment.Center) {
                        Text("Русский", color = if (language == "ru") Emerald else TextDim, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (language == "en") BgDeep else Color.Transparent).clickable { language = "en" }.padding(12.dp), contentAlignment = Alignment.Center) {
                        Text("English", color = if (language == "en") Emerald else TextDim, fontWeight = FontWeight.Bold)
                    }
                }

                SettingsGroup(Strings.get(lang, "tracker_limits")) {
                    SettingsField(Strings.get(lang, "daily_limit_pcs"), dailyLimit) { dailyLimit = it }
                }
                
                if (state.activeKind == "snus") {
                    SettingsGroup(Strings.get(lang, "nicotine_calculation")) {
                        // Nicotine Format Selector
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(Strings.get(lang, "nic_format"), color = TextPrimary, fontSize = 16.sp)
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(BgDeep).clickable {
                                nicotineFormat = when(nicotineFormat) {
                                    "per_pouch" -> "per_pack"
                                    "per_pack" -> "per_gram"
                                    else -> "per_pouch"
                                }
                            }.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    when(nicotineFormat) {
                                        "per_pack" -> Strings.get(lang, "nic_per_pack")
                                        "per_gram" -> Strings.get(lang, "nic_per_gram")
                                        else -> Strings.get(lang, "nic_per_pouch")
                                    },
                                    color = Emerald, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        HorizontalDivider(color = BgDeep, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
                        SettingsField(Strings.get(lang, "nicotine_amount"), nicotineDeclaredAmount, isDecimal = true) { nicotineDeclaredAmount = it }
                        
                        if (nicotineFormat == "per_gram") {
                            SettingsField(Strings.get(lang, "pouch_weight"), pouchWeight, isDecimal = true) { pouchWeight = it }
                        }
                    }
                }

                SettingsGroup(Strings.get(lang, "wishlist")) {
                    SettingsField(Strings.get(lang, "wishlist_title"), wishlistTitle, isText = true) { wishlistTitle = it }
                    SettingsField(Strings.get(lang, "wishlist_cost"), wishlistCost) { wishlistCost = it }
                }

                SettingsGroup(Strings.get(lang, "savings")) {
                    SettingsField(Strings.get(lang, "baseline_per_day"), baseline) { baseline = it }
                    SettingsField(Strings.get(lang, "price_per_pack"), price, isDecimal = true) { price = it }
                    SettingsField(Strings.get(lang, "pcs_per_pack"), perPack) { perPack = it }
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = TextDim, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BgCard)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsField(label: String, value: String, isDecimal: Boolean = false, isText: Boolean = false, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextPrimary, fontSize = 16.sp)
        Box(modifier = Modifier.width(120.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Emerald, 
                    fontSize = 16.sp, 
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = if (isText) KeyboardType.Text else if (isDecimal) KeyboardType.Decimal else KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Emerald
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    HorizontalDivider(color = BgDeep, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
}
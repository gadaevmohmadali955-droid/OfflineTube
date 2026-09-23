package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ChannelEntity
import com.example.data.model.ConfigEntity
import com.example.data.model.ConfigPayload
import com.example.data.model.VideoEntity
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ConfigsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configs by viewModel.configs.collectAsState()
    val configInput by viewModel.configLinkInput.collectAsState()
    val isCreateDialogVisible by viewModel.isCreateConfigDialogVisible.collectAsState()
    val importPreview by viewModel.configImportPreview.collectAsState()
    val configError by viewModel.configErrorMessage.collectAsState()
    val configErrorTitle by viewModel.configErrorTitle.collectAsState()

    var configToDeletePermanently by remember { mutableStateOf<ConfigEntity?>(null) }
    var configToRemove by remember { mutableStateOf<ConfigEntity?>(null) }

    val offlineVideos by viewModel.offlineVideos.collectAsState()
    val savedChannels by viewModel.savedChannels.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Конфиги",
                    color = PureWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp
                )
                Text(
                    text = "Делитесь коллекциями видео и каналов по уникальным ссылкам offline.XXXXXXXXXX",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Import Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth().testTag("config_import_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Импорт конфига по ссылке",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Вставьте ссылку конфига от другого человека (например offline.xxxxxxxxxx):",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = configInput,
                        onValueChange = { viewModel.setConfigLinkInput(it) },
                        placeholder = { Text("offline.xxxxxxxxxx", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = PureWhite)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = PureWhite,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_link_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (configInput.isNotBlank()) {
                                viewModel.resolveAndPreviewConfig(configInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = BlackBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("import_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = BlackBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Импортировать конфиг", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Action: Create Config Button
        item {
            Button(
                onClick = { viewModel.showCreateConfigDialog() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = BlackBackground
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("create_config_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = BlackBackground,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Создать новый конфиг",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }

        // Section: My & Imported Configs List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ваши конфиги (${configs.size})",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        if (configs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth().testTag("empty_configs_box")
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = DarkSurface,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Пока тут ничего нету",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "У вас пока нету созданных или сохраненных конфигов. Нажмите «Создать новый конфиг» или вставьте ссылку от друга выше!",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(configs, key = { it.id }) { config ->
                ConfigItemCard(
                    config = config,
                    onCopyLink = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("TubeSync Config Link", config.link)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Ссылка ${config.link} скопирована!", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Коллекция в TubeSync: «${config.name}»!\nОткрой ссылку в приложении: ${config.link}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Поделиться конфигом через"))
                    },
                    onDelete = {
                        if (config.isCreatedByMe) {
                            configToDeletePermanently = config
                        } else {
                            configToRemove = config
                        }
                    }
                )
            }
        }
    }

    // Modal Dialog: Delete permanently by creator confirmation
    configToDeletePermanently?.let { config ->
        AlertDialog(
            onDismissRequest = { configToDeletePermanently = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = YouTubeRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Удалить конфиг навсегда?", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "Вы являетесь создателем конфига «${config.name}».\n\nПри удалении он будет полностью аннулирован, перестанет открываться по ссылке ${config.link} для всех пользователей и удалится с вашего устройства. Вы уверены?",
                    color = OffWhite,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteConfigPermanently(config.id)
                        configToDeletePermanently = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YouTubeRed,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Удалить навсегда", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { configToDeletePermanently = null }
                ) {
                    Text("Отмена", color = TextSecondary)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal Dialog: Remove imported config from my list confirmation
    configToRemove?.let { config ->
        AlertDialog(
            onDismissRequest = { configToRemove = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Убрать из списка?", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "Конфиг «${config.name}» (${config.link}) будет убран из вашего списка. Он не будет удален у других пользователей.",
                    color = OffWhite,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeImportedConfig(config.id)
                        configToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = BlackBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Убрать", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { configToRemove = null }
                ) {
                    Text("Отмена", color = TextSecondary)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal Dialog: Create Config
    if (isCreateDialogVisible) {
        CreateConfigDialog(
            availableVideos = offlineVideos,
            availableChannels = savedChannels,
            onDismiss = { viewModel.hideCreateConfigDialog() },
            onCreate = { name, vids, chans ->
                viewModel.createConfig(name, vids, chans)
            }
        )
    }

    // Modal Dialog: Import Preview Confirmation
    importPreview?.let { payload ->
        ImportPreviewDialog(
            payload = payload,
            onDismiss = { viewModel.dismissConfigPreview() },
            onConfirm = { viewModel.confirmImportConfig(payload) }
        )
    }

    // Error Alert Dialog (Deleted by creator / already added / not found)
    configError?.let { errorMsg ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfigError() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = YouTubeRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(configErrorTitle, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(errorMsg, color = OffWhite, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissConfigError() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = BlackBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Понятно", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ConfigItemCard(
    config: ConfigEntity,
    onCopyLink: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = modifier.fillMaxWidth().testTag("config_card_${config.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.name,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = if (config.isCreatedByMe) PureWhite.copy(alpha = 0.15f) else GreenSuccess.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (config.isCreatedByMe) "Создан мной" else "Импортирован",
                                color = if (config.isCreatedByMe) PureWhite else GreenSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${config.videoCount} видео • ${config.channelCount} каналов",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_config_${config.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = if (config.isCreatedByMe) "Удалить конфиг навсегда" else "Убрать из списка",
                        tint = if (config.isCreatedByMe) YouTubeRed else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Link Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, PureWhite.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = config.link,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "10 символов",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // White Buttons: Copy Link & Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCopyLink,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = BlackBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("copy_link_${config.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = BlackBackground,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Скопировать ссылку", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurface,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("share_link_${config.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Поделиться", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun CreateConfigDialog(
    availableVideos: List<VideoEntity>,
    availableChannels: List<ChannelEntity>,
    onDismiss: () -> Unit,
    onCreate: (name: String, selectedVideos: List<VideoEntity>, selectedChannels: List<ChannelEntity>) -> Unit
) {
    var configName by remember { mutableStateOf("") }
    val selectedVideoIds = remember { mutableStateListOf<String>() }
    val selectedChannelIds = remember { mutableStateListOf<String>() }

    val hasAnySaved = availableVideos.isNotEmpty() || availableChannels.isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().testTag("create_config_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Создать новый конфиг",
                    color = PureWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Сгенерирует уникальную ссылку offline.XXXXXXXXXX",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = configName,
                    onValueChange = { configName = it },
                    label = { Text("Название конфига", color = TextSecondary) },
                    placeholder = { Text("Например: Мой топ видео и каналы", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("config_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (!hasAnySaved) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "У вас пока нету сохраненных каналов или видео в приложении. Сначала добавьте видео или канал во вкладке 1!",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Выберите элементы:",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        TextButton(onClick = {
                            if (selectedVideoIds.size == availableVideos.size && selectedChannelIds.size == availableChannels.size) {
                                selectedVideoIds.clear()
                                selectedChannelIds.clear()
                            } else {
                                selectedVideoIds.clear()
                                selectedVideoIds.addAll(availableVideos.map { it.id })
                                selectedChannelIds.clear()
                                selectedChannelIds.addAll(availableChannels.map { it.id })
                            }
                        }) {
                            Text(
                                text = if (selectedVideoIds.size == availableVideos.size && selectedChannelIds.size == availableChannels.size) "Снять всё" else "Выбрать всё",
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Selection items list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(DarkSurface, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        if (availableChannels.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Каналы (${availableChannels.size}):",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            items(availableChannels, key = { "ch_${it.id}" }) { channel ->
                                val isChecked = selectedChannelIds.contains(channel.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) selectedChannelIds.remove(channel.id)
                                            else selectedChannelIds.add(channel.id)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedChannelIds.add(channel.id)
                                            else selectedChannelIds.remove(channel.id)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = PureWhite,
                                            checkmarkColor = BlackBackground,
                                            uncheckedColor = DarkBorder
                                        )
                                    )
                                    Text(
                                        text = "${channel.name} (${channel.handle})",
                                        color = PureWhite,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (availableVideos.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Видео (${availableVideos.size}):",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            items(availableVideos, key = { "vid_${it.id}" }) { video ->
                                val isChecked = selectedVideoIds.contains(video.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) selectedVideoIds.remove(video.id)
                                            else selectedVideoIds.add(video.id)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedVideoIds.add(video.id)
                                            else selectedVideoIds.remove(video.id)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = PureWhite,
                                            checkmarkColor = BlackBackground,
                                            uncheckedColor = DarkBorder
                                        )
                                    )
                                    Text(
                                        text = video.title,
                                        color = PureWhite,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Cancel & Create
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurface,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = {
                            val chosenVideos = availableVideos.filter { selectedVideoIds.contains(it.id) }
                            val chosenChannels = availableChannels.filter { selectedChannelIds.contains(it.id) }
                            onCreate(configName, chosenVideos, chosenChannels)
                        },
                        enabled = configName.isNotBlank() && (selectedVideoIds.isNotEmpty() || selectedChannelIds.isNotEmpty()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = BlackBackground,
                            disabledContainerColor = DarkBorder,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp).testTag("confirm_create_config_btn")
                    ) {
                        Text("Создать", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportPreviewDialog(
    payload: ConfigPayload,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite),
            modifier = Modifier.fillMaxWidth().testTag("import_preview_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = GreenSuccess,
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = BlackBackground,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Сохранить конфиг?",
                    color = PureWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "«${payload.name}»",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = payload.link,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "В этот конфиг входит:",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "📺 Каналы: ${payload.channels.joinToString { it.name }}",
                        color = OffWhite,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🎬 Видео: ${payload.videos.size} шт. (будут сохранены для просмотра офлайн)",
                        color = OffWhite,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurface,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = BlackBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp).testTag("confirm_import_save_button")
                    ) {
                        Text("Сохранить", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

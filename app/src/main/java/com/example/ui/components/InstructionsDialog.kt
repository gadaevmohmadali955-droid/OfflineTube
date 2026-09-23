package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

private val OrangeHighlight = Color(0xFFFF5722)

@Composable
fun InstructionsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSlide by remember { mutableIntStateOf(0) }
    val totalSlides = 5

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .testTag("instructions_dialog"),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Slide Count & Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Инструкция: Слайд ${currentSlide + 1} из $totalSlides",
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(DarkSurface, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Slide Visual & Text with animation
                    AnimatedContent(
                        targetState = currentSlide,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "instruction_slide_animation"
                    ) { slide ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (slide) {
                                0 -> SlideOneView()
                                1 -> SlideTwoView()
                                2 -> SlideThreeView()
                                3 -> SlideFourView()
                                4 -> SlideFiveView()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dots indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until totalSlides) {
                            Box(
                                modifier = Modifier
                                    .size(if (i == currentSlide) 18.dp else 8.dp, 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (i == currentSlide) PureWhite else DarkBorder)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigation Buttons (White styling)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentSlide > 0) {
                            Button(
                                onClick = { currentSlide-- },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkSurface,
                                    contentColor = PureWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Назад", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (currentSlide < totalSlides - 1) {
                            Button(
                                onClick = { currentSlide++ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = BlackBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("instruction_next_slide_button")
                            ) {
                                Text("Далее", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = BlackBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = BlackBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("instruction_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = BlackBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Понятно!", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- SLIDE 1 (Photo 1) -----------------
@Composable
private fun SlideOneView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Visual Card Mockup of YouTube Home
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F0F0F))
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column {
                // Fake YouTube App Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = YouTubeRed, shape = RoundedCornerShape(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("YouTube", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Все", color = PureWhite, fontSize = 10.sp, modifier = Modifier.background(DarkSurface, RoundedCornerShape(4.dp)).padding(4.dp))
                        Text("Видеоигры", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.background(DarkSurface, RoundedCornerShape(4.dp)).padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Video item mockup
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E1E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎮 РОБЛОКС: УКРАДИ ЯЙЦО (22:44)",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "😱 ЭТО САМОЕ ДОРОГОЕ ЯЙЦО В РОБЛОКС!",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Text(
                    text = "EroxBlox • 290 тыс. просмотров",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Шаг 1: Зайдите в YouTube",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Откройте приложение YouTube на телефоне и найдите любое видео, Shorts или канал, который хотите сохранить в TubeSync.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ----------------- SLIDE 2 (Photo 2) -----------------
@Composable
private fun SlideTwoView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Visual Card Mockup showing "Поделиться" arrow with orange highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F0F0F))
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "290 тыс. просмотров • 16 ч назад",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.ThumbUp, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                        Text("290 тыс.", color = TextSecondary, fontSize = 10.sp)
                    }

                    // Dislike button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.ThumbDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                        Text("Не нравится", color = TextSecondary, fontSize = 10.sp)
                    }

                    // SHARE BUTTON HIGHLIGHTED IN ORANGE CIRCLE (matching user's screenshot 2!)
                    Box(
                        modifier = Modifier
                            .border(3.dp, OrangeHighlight, CircleShape)
                            .padding(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Поделиться", color = PureWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Шаг 2: Нажмите «Поделиться»",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Под видео нажмите на кнопку «Поделиться» (со значком стрелочки). Она обведена оранжевым кругом.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ----------------- SLIDE 3 (Photo 3) -----------------
@Composable
private fun SlideThreeView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Visual Card Mockup showing Share bottom sheet with highlighted "Коп. ссылку"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1E1E))
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "Поделиться",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // App icons (WhatsApp, Telegram, etc.)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("🟢 WhatsApp", color = PureWhite, fontSize = 10.sp)
                    Text("🔵 Telegram", color = PureWhite, fontSize = 10.sp)
                    Text("✉️ Gmail", color = PureWhite, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DarkBorder)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // HIGHLIGHTED "КОП. ССЫЛКУ" (matching screenshot 3!)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, OrangeHighlight, RoundedCornerShape(14.dp))
                        .background(Color(0xFF2B2B2B), RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF3E3E3E),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Коп. ссылку (Копировать ссылку)",
                            color = PureWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Шаг 3: Скопируйте ссылку",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "В появившемся меню нажмите на пункт «Коп. ссылку». Ссылка на ролик сохранится в буфер обмена вашего телефона.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ----------------- SLIDE 4 (Photos 4, 5, 6) -----------------
@Composable
private fun SlideFourView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Visual Card Mockup showing Channel Description & Link
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F0F0F))
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PureWhite,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🎬", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("НАРЕЗКИ-ИСТОРИЙ | RU", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("@narezki-ru • 1 подписчик", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Highlighted Channel Description line (matching screenshot 4)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, OrangeHighlight, RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "📼 Лучшие нарезки видео. Авторы указаны. ...еще",
                        color = PureWhite,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Highlighted Channel URL (matching screenshot 5)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, OrangeHighlight, RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("Дополнительная информация:", color = TextSecondary, fontSize = 9.sp)
                        Text(
                            text = "🌐 www.youtube.com/@narezki-ru",
                            color = Color(0xFF64B5F6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Шаг 4: Для каналов — ссылка канала",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Чтобы сохранить канал: зайдите в профиль канала, нажмите на описание («Подробнее») и скопируйте ссылку вида youtube.com/@имя_канала.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ----------------- SLIDE 5 (Without Photo, as specified!) -----------------
@Composable
private fun SlideFiveView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Visual Card: TubeSync Input UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, PureWhite.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Category Pills
                Row(
                    modifier = Modifier
                        .background(DarkSurface, RoundedCornerShape(8.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "✓ Видео",
                        color = BlackBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.background(PureWhite, RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "Канал",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input box illustration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, PureWhite, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "https://youtube.com/watch?v=... 📋",
                        color = PureWhite,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // White action button
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = BlackBackground, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Найти и загрузить", color = BlackBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Шаг 5: Вставьте ссылку в TubeSync",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Вставьте ссылку на видео в раздел «Видео», а ссылку на канал — в раздел «Канал». Нажмите «Найти» для просмотра, офлайн-сохранения или добавления в Конфиги!",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

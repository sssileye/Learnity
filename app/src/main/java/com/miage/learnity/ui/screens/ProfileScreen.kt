package com.miage.learnity.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miage.learnity.R


private val IconBg = Color(0xfff0f1f3)
private val PrimaryText = Color(0xff1b1c1e)
private val SecondaryText = Color(0xff8a8e95)
private val MidSheet = Color(0xfff3f4f6)

@Composable
fun ProfileScreen(
    isDiscoveryMode: Boolean, // ⭐ Nouveau paramètre
    onModeChange: (Boolean) -> Unit, // ⭐ Nouveau callback
    onNotification: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidSheet)
    ) {
        Image(
            painter = painterResource(id = R.drawable.arc_pic),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 400.dp)
            .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
            .background(Color.White)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 230.dp)
            .verticalScroll(scroll)
    ) {
        Surface(
            shape = CircleShape,
            shadowElevation = 6.dp,
            color = Color.White,
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.clip(CircleShape)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Axel Hure",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "axelhure@gmail.com",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = SecondaryText
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 2.dp)
        )

        Spacer(Modifier.height(32.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            // ⭐ AJOUT DE L'INTERRUPTEUR DE MODE
            QuizModeToggleRow(
                isDiscoveryMode = isDiscoveryMode,
                onToggle = { onModeChange(!isDiscoveryMode) }
            )

            MenuItemRow("Notification", R.drawable.btn_1, onNotification)
            MenuItemRow("Logout", R.drawable.btn_6, onLogout)
            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * ⭐ NOUVEAU COMPOSANT : Interrupteur sous forme de bouton
 */
@Composable
private fun QuizModeToggleRow(
    isDiscoveryMode: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            tonalElevation = 6.dp,
            color = if (isDiscoveryMode) Color(0xFF673AB7).copy(alpha = 0.1f) else IconBg,
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = if (isDiscoveryMode) R.drawable.ic_settings_1 else R.drawable.ic_settings_1),
                    contentDescription = null,
                    tint = if (isDiscoveryMode) Color(0xFF673AB7) else SecondaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Mode Quiz du jour",
                fontSize = 18.sp,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            )
            Text(
                text = if (isDiscoveryMode) "Module : Découverte" else "Module : Révision",
                fontSize = 13.sp,
                color = SecondaryText
            )
        }

        // L'interrupteur visuel (Switch)
        Switch(
            checked = isDiscoveryMode,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF673AB7),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = IconBg
            )
        )
    }
}

@Composable
private fun MenuItemRow(
    title: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            tonalElevation = 6.dp,
            color = IconBg,
            modifier = Modifier.size(50.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            painter = painterResource(R.drawable.arrow),
            contentDescription = null,
            tint = SecondaryText
        )
    }
}
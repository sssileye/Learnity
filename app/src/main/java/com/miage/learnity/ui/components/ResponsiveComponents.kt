package com.miage.learnity.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miage.learnity.ui.utils.*

@Composable
fun ResponsivePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val dimensions = rememberResponsiveDimensions()

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(dimensions.buttonHeight),
        shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF635BFF),
            disabledContainerColor = Color.LightGray
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(dimensions.iconSizeMedium)
            )
        } else {
            Text(
                text = text,
                fontSize = dimensions.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ResponsiveSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val dimensions = rememberResponsiveDimensions()

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(dimensions.buttonHeight),
        shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF635BFF)
        ),
        border = BorderStroke(2.dp, Color(0xFF635BFF))
    ) {
        Text(
            text = text,
            fontSize = dimensions.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResponsiveSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Color(0xFF635BFF)
) {
    val dimensions = rememberResponsiveDimensions()

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(dimensions.buttonHeightSmall),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        )
    ) {
        Text(
            text = text,
            fontSize = dimensions.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResponsiveCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = rememberResponsiveDimensions()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensions.cardElevation
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensions.cardPadding),
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
            content = content
        )
    }
}

@Composable
fun ResponsiveOutlinedCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF635BFF),
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = rememberResponsiveDimensions()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensions.cardPadding),
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
            content = content
        )
    }
}

@Composable
fun ResponsiveTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true
) {
    val dimensions = rememberResponsiveDimensions()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        isError = isError,
        supportingText = {
            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    fontSize = dimensions.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (supportingText != null) {
                Text(
                    text = supportingText,
                    fontSize = dimensions.bodySmall,
                    color = Color.Gray
                )
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        textStyle = LocalTextStyle.current.copy(
            fontSize = dimensions.bodyLarge
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF635BFF),
            focusedLabelColor = Color(0xFF635BFF)
        )
    )
}

@Composable
fun ResponsiveColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: androidx.compose.ui.Alignment.Horizontal =
        androidx.compose.ui.Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = rememberResponsiveDimensions()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.screenPaddingHorizontal)
            .responsiveMaxWidth(dimensions),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
        content = content
    )
}

@Composable
fun ResponsiveRow(
    modifier: Modifier = Modifier,
    verticalAlignment: androidx.compose.ui.Alignment.Vertical =
        androidx.compose.ui.Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    val dimensions = rememberResponsiveDimensions()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
        content = content
    )
}

@Composable
fun ResponsiveLoadingState(
    message: String = "Chargement..."
) {
    val dimensions = rememberResponsiveDimensions()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF635BFF),
                modifier = Modifier.size(dimensions.iconSizeLarge)
            )
            Text(
                text = message,
                fontSize = dimensions.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ResponsiveErrorState(
    message: String,
    onRetry: () -> Unit
) {
    val dimensions = rememberResponsiveDimensions()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
            modifier = Modifier.padding(dimensions.screenPaddingHorizontal)
        ) {
            Text(
                text = "⚠️",
                fontSize = dimensions.displayLarge
            )

            Text(
                text = "Erreur",
                fontSize = dimensions.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                fontSize = dimensions.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensions.itemSpacing))

            ResponsivePrimaryButton(
                text = "Réessayer",
                onClick = onRetry,
                modifier = Modifier.widthIn(max = 200.dp)
            )
        }
    }
}

@Composable
fun ResponsiveEmptyState(
    emoji: String = "📭",
    title: String,
    description: String
) {
    val dimensions = rememberResponsiveDimensions()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
            modifier = Modifier.padding(dimensions.screenPaddingHorizontal)
        ) {
            Text(
                text = emoji,
                fontSize = dimensions.displayLarge
            )

            Text(
                text = title,
                fontSize = dimensions.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                fontSize = dimensions.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun ResponsiveBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF635BFF)
) {
    val dimensions = rememberResponsiveDimensions()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
        color = containerColor
    ) {
        Text(
            text = text,
            fontSize = dimensions.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(
                horizontal = dimensions.itemSpacing,
                vertical = dimensions.itemSpacing / 2
            )
        )
    }
}

@Composable
fun ResponsiveChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = rememberResponsiveDimensions()

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = dimensions.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(dimensions.cornerRadiusLarge)
    )
}


@Composable
fun ResponsiveDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val dimensions = rememberResponsiveDimensions()

    HorizontalDivider(
        modifier = modifier.padding(vertical = dimensions.itemSpacing / 2),
        color = color,
        thickness = 1.dp
    )
}

@Composable
fun ResponsiveSmallSpacer() {
    val dimensions = rememberResponsiveDimensions()
    Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
}

@Composable
fun ResponsiveNormalSpacer() {
    val dimensions = rememberResponsiveDimensions()
    Spacer(modifier = Modifier.height(dimensions.itemSpacing))
}

@Composable
fun ResponsiveLargeSpacer() {
    val dimensions = rememberResponsiveDimensions()
    Spacer(modifier = Modifier.height(dimensions.itemSpacing * 2))
}

@Composable
fun ResponsiveExtraLargeSpacer() {
    val dimensions = rememberResponsiveDimensions()
    Spacer(modifier = Modifier.height(dimensions.itemSpacing * 4))
}


@Composable
fun ExampleFormScreen() {
    ResponsiveColumn(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text("Inscription", fontSize = rememberResponsiveDimensions().titleLarge)

        ResponsiveNormalSpacer()

        ResponsiveTextField(
            value = "",
            onValueChange = {},
            label = "Email"
        )

        ResponsiveTextField(
            value = "",
            onValueChange = {},
            label = "Mot de passe"
        )

        ResponsiveLargeSpacer()

        ResponsivePrimaryButton(
            text = "S'inscrire",
            onClick = {}
        )

        ResponsiveSecondaryButton(
            text = "Annuler",
            onClick = {}
        )
    }
}


@Composable
fun ExampleListScreen() {
    ResponsiveColumn {
        Text("Mes cours", fontSize = rememberResponsiveDimensions().titleLarge)

        ResponsiveCard {
            Text("Cours 1", fontWeight = FontWeight.Bold)
            Text("Description du cours")

            ResponsiveRow {
                ResponsiveSmallButton(
                    text = "Ouvrir",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                ResponsiveSmallButton(
                    text = "Quiz",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFF4CAF50)
                )
            }
        }

        ResponsiveCard {
            Text("Cours 2", fontWeight = FontWeight.Bold)
            Text("Description du cours")
        }
    }
}
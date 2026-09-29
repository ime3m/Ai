package com.example.ui.components.futuristic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FuturisticTheme
import com.example.ui.theme.FuturisticTokens

/**
 * Categories available in the Futuristic main navigation bar.
 * Matches regional AI voice focus:
 * - Voice (Active live speech interaction)
 * - Languages (Supported regional & world languages catalog)
 * - Accents (Vernacular accents, slang strength & nuances)
 * - Translate (Slang & colloquial regional translator)
 * - Conversation (Interactive persistent conversation thread)
 */
enum class FuturisticCategory(
    val title: String,
    val icon: ImageVector,
    val tag: String
) {
    VOICE("Voice", Icons.Default.Mic, "cat_pill_voice"),
    LANGUAGES("Languages", Icons.Default.Public, "cat_pill_languages"),
    ACCENTS("Accents", Icons.Default.Tune, "cat_pill_accents"),
    TRANSLATE("Translate", Icons.Default.Translate, "cat_pill_translate"),
    CONVERSATION("Conversation", Icons.AutoMirrored.Filled.Chat, "cat_pill_conversation")
}

/**
 * Premium Reusable Category Pill Component.
 *
 * Visual characteristics:
 * - 48–52dp height (default 50dp, accessible touch target)
 * - Fully rounded (pill shape)
 * - Horizontal padding 18–24dp (default 20dp)
 * - Very subtle border (1dp border, smoothly animated)
 * - No heavy shadows (subtle 2dp ambient elevation when selected, 0dp when unselected)
 * - Selected state feels slightly elevated and refined
 * - Unselected state is understated and clean
 * - Full Dark Mode support via FuturisticTheme semantic color roles
 *
 * Smooth Animation (no bounce):
 * - 240ms duration with FastOutSlowInEasing
 * - Synchronously animates background, border, text color, and icon tint
 */
@Composable
fun CategoryPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: Dp = 50.dp,
    horizontalPadding: Dp = 20.dp,
    testTag: String = "category_pill_$label"
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Smooth 240ms transition spec (FastOutSlowInEasing, strict non-bouncing)
    val colorAnimSpec = tween<Color>(durationMillis = 240, easing = FastOutSlowInEasing)

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) FuturisticTheme.surfaceElevated else FuturisticTheme.surface,
        animationSpec = colorAnimSpec,
        label = "pill_bg_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) FuturisticTheme.primaryText else FuturisticTheme.secondaryText,
        animationSpec = colorAnimSpec,
        label = "pill_text_color"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) FuturisticTheme.border.copy(alpha = 0.9f) else FuturisticTheme.border.copy(alpha = 0.4f),
        animationSpec = colorAnimSpec,
        label = "pill_border_color"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) FuturisticTheme.accent else FuturisticTheme.secondaryText,
        animationSpec = colorAnimSpec,
        label = "pill_icon_color"
    )

    // Subtle elevation: 2.5dp for selected, 0dp for unselected (no heavy shadow)
    val elevation = if (selected) 2.5.dp else 0.dp

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = FuturisticTokens.CornerRadius.pillShape,
                spotColor = FuturisticTheme.shadow.copy(alpha = 0.25f),
                ambientColor = FuturisticTheme.shadow.copy(alpha = 0.12f)
            )
            .clip(FuturisticTokens.CornerRadius.pillShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = FuturisticTokens.CornerRadius.pillShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = FuturisticTheme.primaryText.copy(alpha = 0.08f)),
                onClick = onClick
            )
            .height(height)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .padding(horizontal = horizontalPadding)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = label,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                letterSpacing = 0.1.sp
            )
        }
    }
}

/**
 * Backward-compatible overload for CategoryPill with 'isSelected' named parameter.
 */
@Composable
fun CategoryPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "category_pill_$label"
) {
    CategoryPill(
        label = label,
        selected = isSelected,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        testTag = testTag
    )
}

/**
 * Horizontally Scrollable Category Pill Navigation Bar.
 * - Horizontal scrolling with no scrollbar indicators
 * - Proper 48dp+ touch target sizes
 * - Generous 10dp inter-pill spacing and 20dp screen margin
 */
@Composable
fun FuturisticCategoryBar(
    categories: List<FuturisticCategory>,
    selectedCategory: FuturisticCategory,
    onCategorySelected: (FuturisticCategory) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(contentPadding)
            .testTag("futuristic_category_bar"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            CategoryPill(
                label = category.title,
                icon = category.icon,
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                testTag = category.tag
            )
        }
    }
}

package com.tiquionophist.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tiquionophist.ui.Dimens
import com.tiquionophist.ui.GlobalState
import com.tiquionophist.ui.ThemeColors
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun NotificationContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier) {
        content()

        GlobalState.currentNotification?.let { notification ->
            // auto-dismiss notification after its duration elapses, if it has one
            notification.duration?.let { duration ->
                LaunchedEffect(notification) {
                    delay(duration.inWholeMilliseconds)

                    if (GlobalState.currentNotification == notification) {
                        GlobalState.currentNotification = null
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(Dimens.NOTIFICATION_MARGIN)
                    .align(Alignment.TopStart)
                    .width(Dimens.NOTIFICATION_WIDTH)
            ) {
                TooltipSurface {
                    Column(Modifier.fillMaxWidth()) {
                        if (notification.progress != null) {
                            Box(
                                Modifier
                                    .fillMaxWidth(fraction = notification.progress.toFloat())
                                    .height(Dimens.SPACING_2)
                                    .background(ThemeColors.current.selected)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .clickable { GlobalState.currentNotification = null }
                                .padding(Dimens.SPACING_3),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_2),
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2)
                            ) {
                                notification.iconRes?.let { iconRes ->
                                    Icon(
                                        modifier = Modifier.size(Dimens.NOTIFICATION_ICON_SIZE),
                                        painter = painterResource(iconRes),
                                        contentDescription = null,
                                        tint = (notification.iconTint ?: LocalContentColor.current)
                                            .copy(alpha = LocalContentAlpha.current)
                                    )
                                }

                                Text(
                                    text = notification.title,
                                    fontSize = Dimens.Dialog.TITLE_FONT_SIZE
                                )
                            }

                            Text(notification.message)
                        }
                    }
                }
            }
        }
    }
}

package com.tiquionophist.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import com.tiquionophist.ui.Dimens
import com.tiquionophist.ui.GlobalState
import kotlinx.coroutines.delay

@Composable
fun NotificationContainer(content: @Composable () -> Unit) {
    Box {
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

            Box(modifier = Modifier.padding(Dimens.NOTIFICATION_MARGIN).align(Alignment.TopStart)) {
                Surface(
                    shape = RoundedCornerShape(Dimens.CORNER_ROUNDING),
                    modifier = Modifier.shadow(Dimens.SHADOW_ELEVATION),
                ) {
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
                            notification.iconFilename?.let { iconFilename ->
                                Icon(
                                    painter = painterResource("icons/$iconFilename.svg"),
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

package com.aurax.operator.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aurax.operator.core.theme.AuraColors
@Composable fun OperatorIndicator(blocked:Boolean=false,acting:Boolean=false,onAbort:()->Unit={}){val c=when{blocked->AuraColors.Error;acting->AuraColors.Warning;else->AuraColors.Success};Box(Modifier.size(22.dp).background(c,CircleShape).clickable{onAbort()})}
package com.aurax.operator.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable fun GlassCard(modifier:Modifier=Modifier,content:@Composable()->Unit){Box(modifier.background(Color.White.copy(alpha=.06f),RoundedCornerShape(20.dp)).border(1.dp,Color.White.copy(alpha=.1f),RoundedCornerShape(20.dp)).padding(16.dp)){content()}}
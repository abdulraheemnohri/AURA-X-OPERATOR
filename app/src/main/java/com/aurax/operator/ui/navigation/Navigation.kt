package com.aurax.operator.ui.navigation
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.aurax.operator.ui.screens.*
@Composable fun AuraNavigation(){var selected by remember{mutableIntStateOf(0)};Scaffold(bottomBar={NavigationBar{listOf("Home","Chat","Operator","Tasks","Settings").forEachIndexed{i,label->NavigationBarItem(selected==i,{selected=i},icon={},label={Text(label)})}}}){pad->Box(Modifier.padding(pad).fillMaxSize()){when(selected){0->HomeScreen{selected=1};1->ChatScreen();2->OperatorScreen();3->TaskScreen();else->SettingsScreen()}}}}
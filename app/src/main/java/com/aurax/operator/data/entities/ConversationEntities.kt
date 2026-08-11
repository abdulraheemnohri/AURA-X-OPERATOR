package com.aurax.operator.data.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="conversations") data class ConversationEntity(@PrimaryKey(autoGenerate=true)val id:Long=0,val title:String,val createdAt:Long=System.currentTimeMillis())
@Entity(tableName="messages") data class MessageEntity(@PrimaryKey(autoGenerate=true)val id:Long=0,val conversationId:Long,val role:String,val content:String,val timestamp:Long=System.currentTimeMillis())
@Entity(tableName="memories") data class MemoryEntity(@PrimaryKey(autoGenerate=true)val id:Long=0,val key:String,val value:String,val timestamp:Long=System.currentTimeMillis())
@Entity(tableName="tasks") data class TaskEntity(@PrimaryKey(autoGenerate=true)val id:Long=0,val input:String,val status:String,val log:String="",val createdAt:Long=System.currentTimeMillis())
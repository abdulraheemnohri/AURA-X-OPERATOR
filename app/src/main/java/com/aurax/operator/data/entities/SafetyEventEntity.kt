package com.aurax.operator.data.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="safety_events") data class SafetyEventEntity(@PrimaryKey(autoGenerate=true)val id:Long=0,val type:String,val reason:String,val packageName:String?,val action:String?,val timestamp:Long=System.currentTimeMillis())
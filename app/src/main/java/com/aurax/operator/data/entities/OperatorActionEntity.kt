package com.aurax.operator.data.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="operator_actions") data class OperatorActionEntity(@PrimaryKey(autoGenerate=true)val id:Long=0,val taskId:Long?,val packageName:String,val action:String,val target:String?,val allowed:Boolean,val timestamp:Long=System.currentTimeMillis())
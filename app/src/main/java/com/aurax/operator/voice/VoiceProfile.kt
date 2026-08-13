package com.aurax.operator.voice

data class VoiceProfile(val id:String,val name:String,val modelPath:String,val speed:Float=1f,val pitch:Float=1f,val volume:Float=1f,val language:String="en",val emotion:String="neutral")

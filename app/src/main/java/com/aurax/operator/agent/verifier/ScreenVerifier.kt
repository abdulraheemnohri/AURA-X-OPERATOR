package com.aurax.operator.agent.verifier
import com.aurax.operator.operator.ScreenContext
class ScreenVerifier{fun verify(context:ScreenContext?,expectedPackage:String):Boolean=context?.packageName==expectedPackage&&context.isPrivateBrowsing.not()&&context.hasPasswordField.not()}
package com.aurax.operator.operator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
enum class OperatorIndicatorState{OBSERVING,ACTING,BLOCKED,ABORTED}
object OperatorRuntime{@Volatile var aborted=false;private set;private val _indicator=MutableStateFlow(OperatorIndicatorState.OBSERVING);val indicator:StateFlow<OperatorIndicatorState> = _indicator.asStateFlow();fun begin(){aborted=false;_indicator.value=OperatorIndicatorState.OBSERVING};fun acting(){if(!aborted)_indicator.value=OperatorIndicatorState.ACTING};fun blocked(){_indicator.value=OperatorIndicatorState.BLOCKED};fun abort(){aborted=true;_indicator.value=OperatorIndicatorState.ABORTED};fun ensureNotAborted(){check(!aborted){"AURA-X operation aborted by user"}}}
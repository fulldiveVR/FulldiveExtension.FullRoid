package com.swordfish.lemuroid.common.coroutines

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.flow.MutableStateFlow

class MutableStateProperty<T>(
    private val mutableState: MutableStateFlow<T>
) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = mutableState.value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = run {
        mutableState.value = value
    }
}

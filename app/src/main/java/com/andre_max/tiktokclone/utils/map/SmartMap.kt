package com.andre_max.tiktokclone.utils.map

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class SmartMap<K, V>(private val skipCurrentValueCall: Boolean = false) :
    MutableLiveData<SmartMap<K, V>>(), Iterable<Map.Entry<K, V>> {

    private var map: HashMap<K, V> = HashMap()

    var action: SmartAction? = null
        private set

    var actionKey: K? = null
        private set

    var actionValue: V? = null
        private set

    var actionMap: Map<out K, V>? = null
        private set

    var resultValue: V? = null
        private set

    override fun observe(owner: LifecycleOwner, observer: Observer<in SmartMap<K, V>>) {
        super.observe(owner) { observer.onChanged(this) }
    }

    override fun observeForever(observer: Observer<in SmartMap<K, V>>) {
        super.observeForever { observer.onChanged(this) }
    }

    private fun signalChanged(
        action: SmartAction,
        actionKey: K? = null,
        actionValue: V? = null,
        actionMap: Map<out K, V>? = null,
        resultValue: V? = null
    ) {
        if (skipCurrentValueCall && !hasActiveObservers())
            return

        this.action = action
        this.actionKey = actionKey
        this.actionValue = actionValue
        this.actionMap = actionMap
        this.resultValue = resultValue
        value = value
    }

    val size: Int
        get() = map.size

    fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    fun clear() {
        map.clear()
        signalChanged(action = SmartAction.Clear)
    }

    // We'll get the position if commentGroup == commentGroup fails
    fun remove(key: K): V? {
        val value = map.remove(key)
        if (value != null)
            signalChanged(action = SmartAction.Remove, actionKey = key, resultValue = value)
        return value
    }

    fun containsKey(key: K): Boolean {
        return map.containsKey(key)
    }

    fun containsValue(value: V): Boolean {
        return map.containsValue(value)
    }

    operator fun get(key: K): V? {
        return map[key]
    }

    val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = map.entries

    val keys: MutableSet<K>
        get() = map.keys

    val values: MutableCollection<V>
        get() = map.values

    fun put(key: K, value: V): V? {
        val resultValue = map.put(key, value)
        signalChanged(
            action = SmartAction.Put,
            actionKey = key,
            actionValue = value,
            resultValue = resultValue
        )
        return resultValue
    }

    operator fun set(key: K, value: V): V? {
        return put(key, value)
    }

    fun putAll(from: Map<out K, V>) {
        map.putAll(from)
        signalChanged(action = SmartAction.PutAll, actionMap = from)
    }

    fun indexOf(key: K) = map.keys.indexOf(key)

    override fun iterator(): Iterator<Map.Entry<K, V>> {
        return map.iterator()
    }
}
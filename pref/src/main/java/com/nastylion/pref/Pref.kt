@file:Suppress("unused")

package com.nastylion.pref

import android.content.SharedPreferences
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

/**
 * Lets convert a string to a shared preference with default value
 * example: val rated = "rated".asPref(false)
 * assign new value: rated += true
 */
inline fun <reified T : Any> String.asPref(defaultValue: T, asyncInit: Boolean = true): Pref<T> =
    Pref(this, defaultValue, asyncInit)

/**
 * generate live data that will be updated when shared Pref value changes
 */
inline fun <reified T : Any> Pref<T>.asLiveData(): MutableLiveData<T?> = MutableLiveData<T?>().apply {
    //update live data value based on shared pref value
    value = get()

    //update live data based onChanged shared pref value (will be called async)
    valueChangedAsync = { if (this.value != it) postValue(it) }

    //update shared pref based on current live data value if value is not null
    observeForever { liveDataPrefValue -> liveDataPrefValue?.let { if (it != get()) set(it) } }
}

/**
 * Wait for loaded value and run block
 */
inline fun <reified T : Any> Pref<T>.singleChange(crossinline block: ((value: T) -> Unit)) {
    valueChangedAsync = {
        valueChangedAsync = null
        block.invoke(it)
    }
}

/**
 * sets new value to the shared preference
 */
operator fun <T : Any> Pref<T>.plusAssign(value: T) = set(value)

//return literal default values in case there are not set
fun <T : Any> Pref<T>.boolean(): Boolean = get() as Boolean? ?: false
fun <T : Any> Pref<T>.int(): Int = get() as Int? ?: 0
fun <T : Any> Pref<T>.string(): String = get() as String? ?: ""
fun <T : Any> Pref<T>.long(): Long = get() as Long? ?: 0L
fun <T : Any> Pref<T>.float(): Float = get() as Float? ?: 0f

/**
 * Holds shared preference (autoupdates when values changes in shared preference)
 *
 * Init Example:
 * Pref.init(PreferenceManager.getDefaultSharedPreferences(this)) { key: String, value: Any ->
 *  //Will be called in UI Thread
 *  FirebaseAnalytics.getInstance(this@Application).setUserProperty(key, "$value")
 * }
 *
 */
class Pref<T : Any>(
    private val name: String,
    private var defaultValue: T,
    asyncInit: Boolean = true,
    @AnyThread var valueChangedAsync: ((value: T) -> Unit)? = null
) {

    /**
     * holds current value
     */
    private var value: T? = null
    /**
     * last read job, store so we can stop it in case there is another read event
     */
    private var lastRead: Job? = null
    /**
     * last write job, store so we can stop it in case there is another write event
     */
    private var lastWrite: Job? = null

    /**
     * listens for asPref changes and reloads preference if key matches
     */
    var listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        //if its about this object, load it from the sharedPreferences
        if (key == name) getValueFromPreferences()
    }

    /**
     * keep sharedPreferences and update listener static
     */
    companion object {
        /**
         * scope for writes and reads of preferences
         */
        private val uiScope = CoroutineScope(Dispatchers.Main)

        /**
         * function that is called when a value has been loaded from the settings
         */
        private var valueChangedMainThread: ((key: String, value: Any?) -> Unit)? = null

        /**
         * holds the sharedPreferences storage link
         */
        private lateinit var sharedPreferences: SharedPreferences

        /**
         * Supply shared preference and optional lambda to be called on main thread everytime a shared pref changes
         */
        fun init(_sharedPreferences: SharedPreferences, @MainThread _listener: ((key: String, value: Any?) -> Unit)? = null) {
            sharedPreferences = _sharedPreferences
            valueChangedMainThread = _listener
        }
    }

    /**
     * register for preference updates
     */
    init {
        //init update listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        //init value sync or async
        if (asyncInit) getValueFromPreferences()
        else getValueFromPreferencesSync()
    }

    /**
     * get value from asPref based on type
     */
    private fun getValueFromPreferences() {
        //cancel old setter
        lastRead?.cancel()
        //load new value
        lastRead = uiScope.launch {
            getValueFromPreferencesAsync()
            //user change Listener
            valueChangedMainThread?.invoke(name, value as Any?)
        }
    }

    /**
     * sets values in sharedPreferences based on type
     */
    fun set(value: T) {
        //stop last write
        val oldWrite = lastWrite
        //start write
        lastWrite = uiScope.launch {
            oldWrite?.cancelAndJoin()
            setValueToPreferencesAsync(value)
        }
        //set current values, don't wait for async listener
        this@Pref.value = value
    }

    suspend fun setAsync(value: T) {
        setValueToPreferencesAsync(value)
        //set current values, don't wait for async listener
        this@Pref.value = value
    }

    suspend fun getAsync() = getValueFromPreferencesAsync()

    private suspend fun getValueFromPreferencesAsync(): T? = withContext(Dispatchers.IO) {
        getValueFromPreferencesSync()

        //update value listener
        value?.apply { valueChangedAsync?.invoke(this) }
    }

    private suspend fun setValueToPreferencesAsync(value: T) =
        withContext(Dispatchers.IO) { setValueToPreferencesSync(value) }

    /**
     * write actual value to shared pref
     */
    private fun setValueToPreferencesSync(value: T) {
        sharedPreferences.edit {
            when (value) {
                is String -> putString(name, value as String?)
                is Boolean -> putBoolean(name, value as Boolean)
                is Int -> putInt(name, value as Int)
                is Float -> putFloat(name, value as Float)
                is Long -> putLong(name, value as Long)
            }
        }
    }

    /**
     * Read actual value from shared pref, uses [defaultValue] in case its not set
     */
    @Suppress("UNCHECKED_CAST")
    private fun getValueFromPreferencesSync() {
        value = when (defaultValue) {
            is String -> sharedPreferences.getString(name, defaultValue as String?) as T
            is Boolean -> sharedPreferences.getBoolean(name, defaultValue as Boolean) as T
            is Int -> sharedPreferences.getInt(name, defaultValue as Int) as T
            is Float -> sharedPreferences.getFloat(name, defaultValue as Float) as T
            is Long -> sharedPreferences.getLong(name, defaultValue as Long) as T
            else -> value
        }
    }

    /**
     * returns current value
     */
    fun getRaw() = value

    /**
     * returns current value
     */
    fun get() = value ?: defaultValue

    /**
     * name of object
     */
    fun getName() = name

    /**
     * returns current value as String
     */
    override fun toString(): String {
        return "$value"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pref<*>

        if (name != other.name) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }
}



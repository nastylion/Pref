# Pref
Shared Preferences access made easy in Kotlin
> **be aware:** my first public library, my first time using Kotlin

###### About 
This library provides an easy access to **Shared Preferences** on Android.
Read and Writes are *asynchrously* executed by **Kotlin coroutines**.
Extension function provide a converstion to a **LiveData**
All done by a single line of code

## Example

###### Demo 
Decareing a shared prference key with default value as a variable. In this case anyName will be updated automatically everytime the shared preference changes. It holds the default value `false` and is stored with 'sharedPreferenceKey'
Use second parameter with false if you want to initialize synchronously the preference value.
```
object Settings {
 //name of the string defines the key where the value is stored in the shared preferences
 val switchValue = "switch".asPref(false) // false is the default value, defines the type of the Pref
 
 //sync delcaration example false executes the initial shared preference read synchronously
 val anyName = "sharedPreferenceKey".asPref("defaultValue",false)
}
```

###### Read & Writes 
Anywhere in your code you can simple access with `Settings.anyName.get()` the value of the shared preference.
Use `Settings.anyName.set(true)` or `Settings.anyName += true` to update a value (will be done in a workerthread)

If you do the init asynchronously there is a helper lambda `singleChange` that is executed once when the value is loaded
```
private val appStarts = "appStarts".asPref(0).run { singleChange { set(it + 1) } }
```

###### Live Data
Using a shared preference `Pref<T>` as `LiveData<T>` is very easy
```
class MyViewModel: ViewModel() {
 //Holds a MutableLiveData<Boolean>
 val switchValue = Settings.switchValue.asLiveData()
}

//using Two-Way Binding your UI stores changes automatically in shared preferences
<androidx.appcompat.widget.SwitchCompat
                android:id="@+id/switch"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:checked="@={viewModel.switchValue}"/>
```

## Initialisation 
Init Pref library in your Application class
```
class MyApplication : Application() {
  override fun onCreate() {
        super.onCreate()
        Pref.init(PreferenceManager.getDefaultSharedPreferences(this))
  }
}
```
Init Pref using a Lambda to receive all Shared Pref changes on main thread
> Can be used to store all current user preferences in Firebase Analytics for example
```
Pref.init(PreferenceManager.getDefaultSharedPreferences(this)) { key: String, value: Any? ->
    Timber.d("Pref: $key: $value (${Thread.currentThread().name})")
    FirebaseAnalytics.getInstance(this@Application).setUserProperty(key, "$value")
}
```

###### Gradle 
This library depends on 'kotlinx.coroutines.*' and 'androidx.core'
```
implementation "androidx.core:core-ktx:XXX"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:XXX"
```



# Pref
Shared Preferences access made easy in Kotlin
> **be aware:** my first public library, my first time using Kotlin

###### About 
This library provides an easy access to **Shared Preferences** on Android.
Read and Writes are *asynchronous* executed by **Kotlin coroutines**.
Extension function provide a converstion to a **LiveData**.
All done in a single line of code

## Example
Decareing a shared preference key with default value as a variable. 

In this case `anyName` will be updated automatically everytime the shared preference changes. It holds the default value `false` and is stored with the key:'sharedPreferenceKey'
Use the second parameter with false if you want to initialize the preference value synchronously.
String is extended with `.asPref(t:T)` to create a `Pref<T>` object, will make your Settings object look clean.
```
object Settings {
 //name of the string defines the key where the value is stored in the shared preferences
 val booleanValue = "sharedPreferenceKey".asPref(false) // false = default value & defines the type
 
 //sync delcaration example false executes the initial shared preference read synchronously
 val anyName = "stringExample".asPref("defaultValue", false)
}
```

###### Read & Writes 
Anywhere in your code you can simple access with `Settings.anyName.get()` the value of the shared preference.
Use `Settings.anyName.set(true)` or `Settings.anyName += true` to update a value (will be done in a workerthread)

If you do the init asynchronously there is a helper lambda `singleChange` that is executed once when the value is loaded
```
//everytime the app starts a counter will be incremented and stored back in shared preference
"appStarts".asPref(0).run { singleChange { set(it + 1) } }
```

###### Live Data
Using a shared preference `Pref<T>` as `LiveData<T>` is made very easy. `Pref<T>.asLiveData()` returns a `MutableLiveData<T>` that is updated when the shared preference value changes. If you change the value of the LiveData object the shared preference will be updated asynchronously.

This can be very sexy when you are using it in combindation with Android Two-Way-Binding of you UI elements.
With a single line of code you load and store UI updates in shared preference
```
class MyViewModel: ViewModel() {
 //Holds a MutableLiveData<Boolean> that is set as soon as the shared pref is read on a workerthread
 val switchValue = "switch".asPref(false).asLiveData()
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
implementation 'androidx.appcompat:appcompat:1.0.2'
implementation "androidx.core:core-ktx:1.1.0-alpha05"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1"
```

Add library to your project with gradle
```
allprojects {
 repositories {
  ...
  maven { url 'https://jitpack.io' }
 }
}
dependencies {
  implementation 'com.github.nastylion:Pref:0.0.1'
}
```




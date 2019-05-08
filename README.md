# Pref
Shared Preferences access made easy in Kotlin
> **be aware:** my first public library, my first time using Kotlin

###### About 
This library provides an easy access to **Shared Preferences** on Android.
Read and Writes as *asynchrously* executed by using **Kotlin courotines**.
Extension function provide a converstion to a **LiveData** 

## Example
###### Init 
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




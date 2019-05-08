# Pref
Shared Preferences access made easy in Kotlin
> (**be aware:** my first public library, my first time using Kotlin)

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



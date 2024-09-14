

package com.duckduckgo.app

import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.di.AppComponent
import com.duckduckgo.app.global.DuckDuckGoApplication
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// from https://stackoverflow.com/a/44991770/73479
@UiThread
fun <T> LiveData<T>.blockingObserve(): T? {
    var value: T? = null
    val latch = CountDownLatch(1)
    val innerObserver = Observer<T> {
        value = it
        latch.countDown()
    }
    observeForever(innerObserver)
    latch.await(2, TimeUnit.SECONDS)
    return value
}

fun getApp(): DuckDuckGoApplication {
    return InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as DuckDuckGoApplication
}

fun getDaggerComponent(): AppComponent {
    return getApp().daggerAppComponent as AppComponent
}

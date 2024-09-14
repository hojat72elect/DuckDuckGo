

package com.duckduckgo.autofill.sync

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.autofill.store.AutofillDatabase

fun inMemoryAutofillDatabase(): AutofillDatabase {
    return Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AutofillDatabase::class.java)
        .allowMainThreadQueries()
        .build()
}

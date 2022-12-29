package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    //Executes each task synchronously using Architecture Components
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Create the database
    private lateinit var database: RemindersDatabase

    // initialize the database before tests
    @Before
    fun initDb() {
        // Use inMemoryDatabase for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    // close the database after tests
    @After
    fun closeDb() = database.close()

    // Test insert to th db and get it
    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        // Given - save a reminder
        val reminder = ReminderDTO(
            title = "Title",
            description = "Description",
            location = "Location",
            latitude = 12.12,
            longitude = 13.13
        )
        // Save reminder
        database.reminderDao().saveReminder(reminder)

        // When - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // Then - the loaded data contains the expected values
        assertThat<ReminderDTO>(loaded as ReminderDTO, CoreMatchers.notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    // Test reminder not found
    @Test
    fun deleteReminderFromDB_retrieveNullValue() = runBlockingTest{
        // Given - save a reminder
        val reminder = ReminderDTO(
            title = "Title",
            description = "Description",
            location = "Location",
            latitude = 12.12,
            longitude = 13.13
        )

        // Save reminder
        database.reminderDao().saveReminder(reminder)

        // Delete reminder
        database.reminderDao().deleteAllReminders()

        // When - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // Then loaded is null value
        assertThat(loaded, `is` (nullValue()))

    }

}
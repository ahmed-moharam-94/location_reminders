package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Create a database object
    private lateinit var database: RemindersDatabase

    // Create a remindersLocalRepository object
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    // Initialize the db and the repository
    @Before
    fun setup() {
        // Initialize the database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        // Initialize the repository
        // Use Dispatchers.Main because we allowed mainThreadQueries
        remindersLocalRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main // because we allowed mainThreadQueries
        )
    }

    // Clean the database
    @After
    fun cleanUp() {
        database.close()
    }

    // Test save and get a reminder
    @Test
    fun saveReminder_retrieveReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO(
            "Title",
            "Description",
            "Location",
            12.45,
            13.44
        )
        // Save reminder using repository
        remindersLocalRepository.saveReminder(reminder)

        // WHEN - reminder retrieved by id
        val result = remindersLocalRepository.getReminder(reminder.id) as Result.Success

        // THEN - same reminder is returned
        assertThat(reminder, `is` (not(nullValue())))
        assertThat(result, `is`(Result.Success(reminder)))
        assertThat(result.data, `is`(reminder))
        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.title, `is`("Title"))
        assertThat(result.data.description, `is`("Description"))
        assertThat(result.data.location, `is`("Location"))
        assertThat(result.data.latitude, `is`(12.45))
        assertThat(result.data.longitude, `is`(13.44))
    }

    // Test delete reminder return null value
    @Test
    fun deleteAllReminders_retrieveReminderById() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO(
            "Title",
            "Description",
            "Location",
            12.45,
            13.44
        )
        // Save reminder using repository
        remindersLocalRepository.saveReminder(reminder)
        // Delete all reminders
        remindersLocalRepository.deleteAllReminders()

        // WHEN - get the reminder
        val result = remindersLocalRepository.getReminder(reminder.id) as Result.Error

        // THEN - return error
        assertThat(result, `is`(Result.Error("Reminder not found!")))

    }


}
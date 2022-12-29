package com.udacity.project4.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    // GIVEN a fresh object of saveReminderViewModel
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Create a FakeDataSource object
    private lateinit var fakeDataSource: FakeDataSource

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Use the mainCoroutineRule to setup coroutine before and after tests
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    // Initialize the viewModel before every tests
    @Before
    fun setupTheViewModel() {
        // Initialize firebase
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())

        // Initialize the fakeDataSource
        fakeDataSource = FakeDataSource()

        // Initialize the viewModel and inject the FakeDataSource
        saveReminderViewModel = SaveReminderViewModel(
            // Use ApplicationProvider from androidX.test package to get the application context
            ApplicationProvider.getApplicationContext(),
            // Inject the FakeDataSource
            fakeDataSource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }


    // Test loading
    @Test
    fun saveValidReminder_checkLoading() = mainCoroutineRule.runBlockingTest {
        // Create reminderDataItem
        val reminder = ReminderDataItem(
            title = "Title1",
            description = "Description1",
            location = "Pyramids",
            longitude = 123.5,
            latitude = 432.43
        )

        // pause the dispatcher
        mainCoroutineRule.pauseDispatcher()

        // When save the reminder
        saveReminderViewModel.saveReminder(reminder)

        // Then - show loading is true
        assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true)
        )

        // resume the dispatcher
        mainCoroutineRule.resumeDispatcher()

        // Then - progress indicator is not showing after save the reminder
        assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false)
        )
    }

    // Test to check error exception
    @Test
    fun saveInvalidReminder_showSnackBar() = mainCoroutineRule.runBlockingTest {
        // Invalid reminders
        val noTitleReminder = ReminderDataItem(
            title = null,
            description = "Description1",
            location = "Pyramids",
            longitude = 123.5,
            latitude = 432.43
        )

        val noLocationReminder = ReminderDataItem(
            title = "Title",
            description = "Description",
            location = null,
            longitude = 123.5,
            latitude = 432.43
        )

        // WHEN save reminders
        saveReminderViewModel.validateEnteredData(noTitleReminder)

        // THEN show a snackBar
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(notNullValue())
        )

        // WHEN save reminders
        saveReminderViewModel.validateEnteredData(noLocationReminder)

        // THEN show a snackBar
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(notNullValue())
        )

    }

}
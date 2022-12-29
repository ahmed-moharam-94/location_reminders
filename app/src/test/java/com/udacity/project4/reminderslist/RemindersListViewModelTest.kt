package com.udacity.project4.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersListViewModelTest {

    // GIVEN a fresh object of remindersListViewModel
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Create a FakeDataSource object
    private lateinit var fakeDataSource: FakeDataSource

    // Use the mainCoroutineRule to setup coroutine before and after tests
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Initialize the viewModel before every tests
    @Before
    fun setupTheViewModel() {
        // Initialize firebase
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())

        // Initialize the fakeDataSource
        fakeDataSource = FakeDataSource()

        // Initialize the viewModel and inject the FakeDataSource
        remindersListViewModel = RemindersListViewModel(
            // Use ApplicationProvider from androidX.test package to get the application context
            ApplicationProvider.getApplicationContext(),
            // Inject the FakeDataSource
            fakeDataSource
        )
    }

    // Stop koin
    @After
    fun tearDown() {
        stopKoin()
    }

    // Test save reminders
    @Test
    fun saveReminders_remindersListIsNotEmpty() = mainCoroutineRule.runBlockingTest {
        // Create reminders objects
        val reminder1 = ReminderDTO(
            title = "Title1",
            description = "Description1",
            location = "Pyramids",
            longitude = 123.5,
            latitude = 432.43
        )
        val reminder2 = ReminderDTO(
            title = "Title2",
            description = "Description2",
            location = "River",
            longitude = 234.1,
            latitude = 142.39
        )

        // WHEN save reminders to the reminders list
        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)

        remindersListViewModel.loadReminders()

        // THEN reminders list is not empty
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), `is`(not(emptyList())))
    }

    // Test load all reminders from data source and update UI
    @Test
    fun loadingReminders_checkLoading() = mainCoroutineRule.runBlockingTest {
        // Create reminders objects
        val reminder1 = ReminderDTO(
            title = "Title1",
            description = "Description1",
            location = "Pyramids",
            longitude = 123.5,
            latitude = 432.43
        )
        val reminder2 = ReminderDTO(
            title = "Title2",
            description = "Description2",
            location = "River",
            longitude = 234.1,
            latitude = 142.39
        )
        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)

        // pause the dispatcher
        mainCoroutineRule.pauseDispatcher()

        // When - loading the reminders
        remindersListViewModel.loadReminders()

        // Then - show loading
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true)
        )

        // resume the dispatcher
        mainCoroutineRule.resumeDispatcher()


        // Then - progress indicator is not showing after loading complete
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false)
        )
    }

    // Test delete all reminders
    @Test
    fun deleteAllReminders_RemindersListIsEmpty() = mainCoroutineRule.runBlockingTest {
        // Create reminders objects
        val reminder1 = ReminderDTO(
            title = "Title1",
            description = "Description1",
            location = "Pyramids",
            longitude = 123.5,
            latitude = 432.43
        )
        val reminder2 = ReminderDTO(
            title = "Title2",
            description = "Description2",
            location = "River",
            longitude = 234.1,
            latitude = 142.39
        )

        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)

        // WHEN delete all reminders
        fakeDataSource.deleteAllReminders()

        remindersListViewModel.loadReminders()

        // THEN reminders list is empty and show a snackBar with empty reminder list message
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), `is`(emptyList()))
    }

    // Test to check error exception
    @Test
    fun shouldReturnError_showSnackBar() = mainCoroutineRule.runBlockingTest {
        // WHEN the fakeDataSource will return error
        fakeDataSource.setShouldReturnError(true)

        remindersListViewModel.loadReminders()

        // THEN show a snackBar and show Can't Get Reminder
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`(notNullValue()))
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`(equalTo("Can't Get Reminders")))
    }

}
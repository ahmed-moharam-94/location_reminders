package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.util.monitorFragment
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Create a reminder data source
    private lateinit var reminderDataSource: ReminderDataSource

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    // Register resource idling
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    // Unregister resource idling
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    // Initialize data source
    @Before
    fun setupDataSource() {
        // Stop koin
        stopKoin()
        val myModule = module {
            viewModel {
                // Pass in the viewModel associated with the fragment
                RemindersListViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }

            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }

        }

        // Start koin
        startKoin {
            modules(listOf(myModule))
        }

        // Initialize data source
        reminderDataSource = GlobalContext.get().koin.get()

        // Delete all reminders
        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }

    // Stop koin
    @After
    fun tearDown() {
        stopKoin()
    }

    // Test add reminder will navigate to add reminder
    @Test
    fun clickOnAddReminderFAB_NavigateToAddReminder() {

        // GIVEN - On the reminder list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // create a navController use Mockito
        val navController = Mockito.mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(scenario)

        // add the navController to scenario
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - click on the FAB button
        onView(withId(R.id.addReminderFAB))
            .perform(click())

        // THEN - Verify that we navigate to the add screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    // Test loaded reminders show in the screen
    @Test
    fun loadedReminders_ShowOnTheScreen()  {
        // Create 2 reminders
        val reminder1 = ReminderDTO("Title1", "Description1",
            "Location1", 43.43, 12.21
        )
        val reminder2 = ReminderDTO("Title2", "Description2",
            "Location2", 73.12, 90.21
        )

        // Save the reminders
        runBlocking {
            reminderDataSource.saveReminder(reminder1)
            reminderDataSource.saveReminder(reminder2)
        }

        // GIVEN - On the reminder list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(scenario)


        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // THEN - two reminders will show on the screen
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder1.description)).check(matches(isDisplayed()))
        onView(withText(reminder1.location)).check(matches(isDisplayed()))

        onView(withText(reminder2.title)).check(matches(isDisplayed()))
        onView(withText(reminder2.description)).check(matches(isDisplayed()))
        onView(withText(reminder2.location)).check(matches(isDisplayed()))

    }

    // Test no reminders will display no reminders text
    @Test
    fun noRemindersToDisplay_ShowsNoDataTextView() {
        // GIVEN - On the reminder list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(scenario)


        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // THEN - No Data textView will be displayed
        onView(withId(R.id.noDataTextView)).check(matches((isDisplayed())))
        onView(withText(R.string.no_data)).check(matches((isDisplayed())))

    }


}
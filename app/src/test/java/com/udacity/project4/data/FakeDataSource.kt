package com.udacity.project4.data

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

// Create a FakeDataSource and implement the ReminderDataSource interface
// Pass in the constructor a mutable list of reminders data class and initialize it to empty list
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    // Create a var to represent if will return error or success
    // First Initialize the value to false by default will not return error
    private var shouldReturnError = false
    // Second create a method to change makeItReturnError to true to test when it will return error
    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        // Check if it will return error
        return if (shouldReturnError) {
            Result.Error("Can't Get Reminders")
        } else {
            Result.Success(ArrayList(reminders))
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    // Get a specific reminder by id
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        // Check if it will return error
        if (shouldReturnError) {
            return Result.Error("Can't Get The Reminder")

        } else {
            // Loop over the reminders list to find reminder.id == id
                reminders.forEach { reminder ->
                    // If the reminder found return Result.Success
                    if (reminder.id == id) {
                        return Result.Success(reminder)
                    }
            }
            // If the reminder not found return Result.Error()
            return Result.Error("Can't Get The Reminder")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}
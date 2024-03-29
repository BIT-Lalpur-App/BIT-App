/*
 *  Created by aiyu
 *  Copyright (c) 2021 . All rights reserved.
 *  BIT App
 *
 */

package com.atech.bit.ui.screens.attendance.addedit

sealed interface AddEditEvent {
    data class OnSubjectChange(val subject: String) : AddEditEvent
    data class OnPresentChange(val present: Int) : AddEditEvent
    data class OnTotalChange(val total: Int) : AddEditEvent
    data class OnTeacherNameChange(val teacherName: String) : AddEditEvent
    data class OnSaveClick(val action: () -> Unit = {}) : AddEditEvent

}
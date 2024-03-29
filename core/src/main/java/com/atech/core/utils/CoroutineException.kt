/*
 *  Created by aiyu
 *  Copyright (c) 2021 . All rights reserved.
 *  BIT App
 *
 */

package com.atech.core.utils

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler


val handler = CoroutineExceptionHandler { _, throwable ->
    Log.e(TAGS.BIT_COROUTINE.name, "Exception : $throwable ")
}
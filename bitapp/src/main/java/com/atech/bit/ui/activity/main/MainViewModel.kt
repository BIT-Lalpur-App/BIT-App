package com.atech.bit.ui.activity.main

import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.Keep
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atech.bit.BuildConfig
import com.atech.bit.ui.screens.force.ForceScreenModel
import com.atech.bit.ui.screens.home.compose.EventAlertModel
import com.atech.bit.ui.screens.home.compose.eventAlertModel
import com.atech.bit.utils.getTheme
import com.atech.bit.utils.saveTheme
import com.atech.core.datasource.firebase.auth.UserData
import com.atech.core.datasource.firebase.auth.UserModel
import com.atech.core.datasource.firebase.remote.RemoteConfigHelper
import com.atech.core.datasource.firebase.remote.model.CourseDetails
import com.atech.core.datasource.firebase.remote.model.defaultCourseSem
import com.atech.core.datasource.room.attendance.AttendanceDao
import com.atech.core.usecase.AuthUseCases
import com.atech.core.utils.BitAppScope
import com.atech.core.utils.RemoteConfigKeys
import com.atech.core.utils.SharePrefKeys
import com.atech.core.utils.TAGS
import com.atech.core.utils.fromJSON
import com.atech.core.utils.toJSON
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val conf: RemoteConfigHelper,
    private val pref: SharedPreferences,
    private val authUseCases: AuthUseCases,
    private val attendanceDao: AttendanceDao,
    @BitAppScope private val scope: CoroutineScope
) : ViewModel() {
    private val _courseDetail = mutableStateOf(defaultCourseSem)
    val courseDetail: State<CourseDetails> get() = _courseDetail

    val hasSetUpDone = (authUseCases.hasLogIn.invoke() || pref.getBoolean(
        SharePrefKeys.SetUpDone.name, false
    )) || pref.getBoolean(
        SharePrefKeys.PermanentSkipLogin.name, false
    )
    var action: () -> Unit = {}

    fun checkHasLogIn() = authUseCases.hasLogIn.invoke()

    private val _useModel = mutableStateOf(UserModel())
    val useModel: State<UserModel> get() = _useModel

    private val _userData = mutableStateOf(UserData())
    val userData: State<UserData> get() = _userData

    private val _profileLink = mutableStateOf<String?>(null)
    val profileLink: State<String?> get() = _profileLink

    private val _isShowAlertDialog = mutableStateOf(false)
    val isShowAlertDialog: State<Boolean> get() = _isShowAlertDialog

    private val _dialogModel = mutableStateOf(eventAlertModel)
    val dialogModel: State<EventAlertModel> get() = _dialogModel

    private val _forceScreenModel = mutableStateOf(ForceScreenModel())
    val forceScreenModel: State<ForceScreenModel> get() = _forceScreenModel

    private val _isForceScreenEnable = mutableStateOf(false)
    val isForceScreenEnable: State<Boolean> get() = _isForceScreenEnable

    fun onDismissRequest() {
        _isShowAlertDialog.value = false
    }

    init {
        if (authUseCases.hasLogIn.invoke()) {
            authUseCases.getUserDataFromAuth().let { (model, _) ->
                if (model != null) {
                    _profileLink.value = model.profilePic
                }
            }
        }
    }


    fun fetchRemoteConfigDetails(
        action: (
            dao: AttendanceDao, auth: AuthUseCases, pref: SharedPreferences, maxTime: Int, scope: CoroutineScope
        ) -> Unit
    ) {
        conf.fetchData(failure = {
            Log.e(TAGS.BIT_REMOTE.name, "fetchRemoteConfigDetails: ${it.message}")
        }) {
            conf.getString(RemoteConfigKeys.CourseDetails.value).let {
                _courseDetail.value = fromJSON(it, CourseDetails::class.java) ?: defaultCourseSem
                pref.edit().putString(SharePrefKeys.CourseDetails.name, it).apply()
            }
            conf.getString(RemoteConfigKeys.KeyToggleSyllabusSource.value).let {
                pref.edit().putString(SharePrefKeys.KeyToggleSyllabusSource.name, it).apply()
            }
            conf.getString(RemoteConfigKeys.AppAlertDialog.value).let { fetchData ->
                pref.getString(SharePrefKeys.AppAlertDialog.name, "")?.apply {
                    val times = pref.getInt(SharePrefKeys.ShowTimes.name, 0)
                    if (this.isBlank()) {
                        pref.edit().putString(SharePrefKeys.AppAlertDialog.name, fetchData).apply()
                        return@let
                    }
                    try {
                        val savedJson = fromJSON(
                            pref.getString(SharePrefKeys.AppAlertDialog.name, "")!!,
                            EventAlertModel::class.java
                        )
                        val newJson = fromJSON(fetchData, EventAlertModel::class.java)
                        if (savedJson?.version == newJson?.version && savedJson?.maxTimesToShow!! > times && newJson?.isShow == true) {
                            _isShowAlertDialog.value = true
                            _dialogModel.value = newJson
                            pref.edit().putInt(SharePrefKeys.ShowTimes.name, times + 1).apply()
                        }
                        if (savedJson?.version != newJson?.version) {
                            pref.edit().putInt(SharePrefKeys.ShowTimes.name, 0).apply()
                        }
                        if (savedJson?.version != newJson?.version && newJson?.isShow == true && newJson.maxTimesToShow > times) {
                            _dialogModel.value = newJson
                            _isShowAlertDialog.value = true
                            pref.edit().putInt(SharePrefKeys.ShowTimes.name, times + 1).apply()
                        }
                        if (newJson?.maxTimesToShow == times && newJson.isShow && newJson.version != savedJson?.version) {
                            pref.edit().putString(SharePrefKeys.AppAlertDialog.name, fetchData)
                                .apply()
                        }
                    } catch (e: Exception) {
                        Log.e(TAGS.BIT_ERROR.name, "fetchRemoteConfigDetails: $e")
                        _isShowAlertDialog.value = false
                    }
                }
            }
            conf.getString(RemoteConfigKeys.ForceScreen.value).let { json ->
                try {
                    fromJSON(json, ForceScreenModel::class.java)?.let { model ->
                        _forceScreenModel.value = model
                        _isForceScreenEnable.value =
                            model.isEnable && model.minVersion >= BuildConfig.VERSION_CODE
                    }
                } catch (e: Exception) {
                    Log.e(TAGS.BIT_ERROR.name, "fetchRemoteConfigDetails: $e")
                }
                action.invoke(
                    attendanceDao,
                    authUseCases,
                    pref,
                    conf.getLong(RemoteConfigKeys.MAX_TIMES_UPLOAD.name).toInt(),
                    scope
                )
            }
        }
    }

    private val _isSearchActive = mutableStateOf(false)
    val isSearchActive: State<Boolean> get() = _isSearchActive

    private val _toggleDrawerState = mutableStateOf(null as DrawerValue?)
    val toggleDrawerState: State<DrawerValue?> get() = _toggleDrawerState
    private val _themeState = mutableStateOf(getTheme(pref))
    val themeState: State<ThemeState> get() = _themeState

    private val _isChatScreenEnable = mutableStateOf(
        pref.getBoolean(SharePrefKeys.IsChatScreenEnable.name, false)
    )
    val isChatScreenEnable: State<Boolean> get() = _isChatScreenEnable


    fun onEvent(event: SharedEvents) {
        when (event) {
            SharedEvents.ToggleSearchActive -> _isSearchActive.value = !_isSearchActive.value
            is SharedEvents.ToggleDrawer -> _toggleDrawerState.value = event.state
            SharedEvents.PreformSignOut -> {
                authUseCases.signOut.invoke {
                    _useModel.value = UserModel()
                    _userData.value = UserData()
                    _profileLink.value = null
                }
            }

            SharedEvents.FetchUserDetails -> {
                if (authUseCases.hasLogIn.invoke()) {
                    authUseCases.getUserDataFromAuth().let { (model, _) ->
                        if (model != null) {
                            _profileLink.value = model.profilePic
                        }
                    }
                }
            }

            SharedEvents.OpenLogInScreen -> action.invoke()
            SharedEvents.IsChatScreenEnable -> {
                _isChatScreenEnable.value = !_isChatScreenEnable.value
                pref.edit().putBoolean(
                    SharePrefKeys.IsChatScreenEnable.name, _isChatScreenEnable.value
                ).apply()
            }
        }
    }

    fun onThemeChange(event: ThemeEvent) {
        when (event) {
            is ThemeEvent.ChangeTheme -> {
                _themeState.value = event.state
                saveTheme(_themeState.value, pref)
            }
        }
    }

    fun fetchAccountDetail() = viewModelScope.launch {
        authUseCases.getUserSavedData().let {
            it.first?.let { data ->
                _userData.value = data
            }
        }
        authUseCases.getUserFromDatabase().let { (model, _) ->
            if (model != null) {
                _useModel.value = model
            }
        }
    }

    fun deleteUser(
        action: suspend () -> Unit
    ) = viewModelScope.launch {
        authUseCases.deleteUser.invoke().let {
            if (it == null) {
                pref.edit().putBoolean(SharePrefKeys.PermanentSkipLogin.name, false).apply()
                action.invoke()
            }
        }
    }

    sealed interface SharedEvents {
        data object ToggleSearchActive : SharedEvents
        data class ToggleDrawer(val state: DrawerValue?) : SharedEvents
        data object PreformSignOut : SharedEvents

        data object FetchUserDetails : SharedEvents

        data object OpenLogInScreen : SharedEvents
        data object IsChatScreenEnable : SharedEvents
    }

    //    -------------------------------- Permissions -------------------------------------------------
    private val _isNotificationPrefItemVisible = mutableStateOf(true)
    val isNotificationPrefItemVisible: State<Boolean> get() = _isNotificationPrefItemVisible
    fun onNotificationPrefItemVisibleChange(value: Boolean) {
        _isNotificationPrefItemVisible.value = value
    }

    //    _________________________________ App Notification ________________________________________________
    @Keep
    data class AppNotificationEnable(
        val notice: Boolean = true, val event: Boolean = true, val app: Boolean = true
    )

    private val _appNotification = mutableStateOf(
        fromJSON(
            pref.getString(
                SharePrefKeys.AppNotificationSettings.name, ""
            )!!, AppNotificationEnable::class.java
        ) ?: AppNotificationEnable()
    )
    val appNotification: State<AppNotificationEnable> get() = _appNotification
    fun setAppNotification(value: AppNotificationEnable) {
        _appNotification.value = value
        pref.edit().putString(
            SharePrefKeys.AppNotificationSettings.name, toJSON(value)
        ).apply()
    }
}
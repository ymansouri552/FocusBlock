package com.focusblock.ui.onboarding

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.focusblock.domain.usecase.SystemPermissionChecker
import com.focusblock.domain.usecase.SystemPermissionsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val permissionChecker: SystemPermissionChecker
) : ViewModel() {

    private val _permissions = MutableStateFlow(permissionChecker.checkPermissions())
    val permissions: StateFlow<SystemPermissionsState> = _permissions.asStateFlow()

    fun refreshPermissions() {
        _permissions.value = permissionChecker.checkPermissions()
    }

    fun accessibilitySettingsIntent(): Intent = permissionChecker.getAccessibilitySettingsIntent()

    fun deviceAdminRequestIntent(): Intent = permissionChecker.getDeviceAdminRequestIntent()
}

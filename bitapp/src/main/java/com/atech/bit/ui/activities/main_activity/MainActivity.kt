package com.atech.bit.ui.activities.main_activity

import android.graphics.Color
import android.os.Bundle
import android.viewbinding.library.activity.viewBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.atech.bit.BuildConfig
import com.atech.bit.R
import com.atech.bit.databinding.ActivityMainBinding
import com.atech.bit.utils.DrawerLocker
import com.atech.bit.utils.onDestinationChange
import com.atech.bit.utils.openBugLink
import com.atech.bit.utils.openReleaseNotes
import com.atech.theme.ParentActivity
import com.atech.theme.changeBottomNav
import com.atech.theme.changeStatusBarToolbarColorImageView
import com.atech.theme.currentNavigationFragment
import com.atech.theme.exitTransition
import com.atech.theme.isDark
import com.atech.theme.openCustomChromeTab
import com.atech.theme.openLinks
import com.atech.theme.setStatusBarUiTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ParentActivity, DrawerLocker {
    private val binding: ActivityMainBinding by viewBinding()
    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
    }
    private val navController by lazy {
        navHostFragment.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.apply {
            bottomNavigationSetup()
            handleDrawer()
        }
        handleDestinationChange()
    }

    private fun ActivityMainBinding.bottomNavigationSetup() {
        bottomNavigation.setupWithNavController(navController)
        bottomNavigation.setOnItemReselectedListener { }
    }

    private fun ActivityMainBinding.handleDrawer() = this.navigationView.apply {
        setupWithNavController(navController)
        setNavigationItemSelectedListener { menu ->
            setDrawerState(false)
            when (menu.itemId) {
                R.id.nav_connect -> resources.getString(com.atech.theme.R.string.instaLink)
                    .openLinks(this@MainActivity, com.atech.theme.R.string.no_intent_available)

                R.id.nav_mail -> this@MainActivity.openBugLink()
                R.id.nav_erp -> this@MainActivity.openCustomChromeTab(resources.getString(com.atech.theme.R.string.erp_link))
                R.id.nav_issue -> this@MainActivity.openCustomChromeTab(resources.getString(com.atech.theme.R.string.issue_link))
                R.id.nav_github -> this@MainActivity.openCustomChromeTab(resources.getString(com.atech.theme.R.string.github_link))
                R.id.nav_whats_new -> this@MainActivity.openReleaseNotes()
                else -> NavigationUI.onNavDestinationSelected(menu, navController)
            }
            true
        }
    }

    private fun handleDestinationChange() {
        navController.onDestinationChange { destination ->
            when (destination.id) {
                in drawerFragments() -> getCurrentFragment().apply {
                    setDrawerEnabled(true)
                }

                else -> getCurrentFragment().apply {
                    setDrawerEnabled(false)
                }
            }
            //                Navigation bar visibility
            when (destination.id) {
                in baseFragments() -> {
                    setBottomNavigationVisibility(true)
                }

                else -> {
                    changeBottomNav(
                        android.viewbinding.library.R.attr.colorSurface
                    )
                    setBottomNavigationVisibility(false)
                }
            }
            when (destination.id) {
                in baseFragments() + fragmentWithBottomNavColor() -> changeBottomNav(
                    com.atech.theme.R.attr.bottomBar
                )

                else -> changeBottomNav(
                    android.viewbinding.library.R.attr.colorSurface
                )
            }
            when (destination.id) {
                in navigationViewFragments() -> changeStatusBarToolbarColorImageView(MaterialColors.getColor(
                    this, com.atech.theme.R.attr.bottomBar, Color.WHITE
                ).also {
                    setStatusBarUiTheme(this, !isDark())
                })

                in bottomSheetFragment() ->
                    changeStatusBarToolbarColorImageView(MaterialColors.getColor(
                        this, com.atech.theme.R.attr.bottomSheetBackground, Color.WHITE
                    ).also {
                        setStatusBarUiTheme(this, !isDark())
                    })

                else -> changeStatusBarToolbarColorImageView(MaterialColors.getColor(
                    this, android.viewbinding.library.R.attr.colorSurface, Color.WHITE
                ).also {
                    setStatusBarUiTheme(this, !isDark())
                })
            }
        }
    }

    override fun setBottomNavigationVisibility(isVisible: Boolean) {
        binding.bottomNavigation.isVisible = isVisible
    }

    override fun getVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    override fun getNavigationFragmentId(): Int =
        R.id.fragment

    override fun getBottomNavigationFragment(): BottomNavigationView =
        binding.bottomNavigation

    private fun getCurrentFragment(): Fragment? = supportFragmentManager.currentNavigationFragment
    private fun setExitTransition() = getCurrentFragment()?.exitTransition()

    private fun baseFragments() = listOf(
        R.id.homeFragment, com.atech.course.R.id.courseFragment
    )

    private fun fragmentWithBottomNavColor() =
        listOf(
            com.atech.attendance.R.id.attendanceFragment,
            R.id.holidayFragment,
            R.id.societyFragment,
            R.id.societyDetailFragment,
            R.id.eventFragment,
            R.id.eventDetailFragment,
            R.id.noticeFragment,
            R.id.noticeDetailFragment,
            R.id.libraryFragment,
            R.id.addEditLibraryFragment,
            R.id.cgpaCalculatorFragment
        )

    private fun drawerFragments() = listOf(
        R.id.homeFragment,
        com.atech.course.R.id.courseFragment,
        com.atech.attendance.R.id.attendanceFragment
    )

    private fun navigationViewFragments() = listOf(
        R.id.holidayFragment,
        R.id.societyFragment,
        R.id.societyDetailFragment,
        R.id.administrationFragment,
        com.atech.course.R.id.semChooseFragment,
        com.atech.course.R.id.viewSyllabusFragment,
        R.id.eventFragment,
        R.id.eventDetailFragment,
        R.id.noticeFragment,
        R.id.noticeDetailFragment,
        R.id.libraryFragment,
        R.id.addEditLibraryFragment,
        R.id.cgpaCalculatorFragment,
        R.id.viewSyllabusFragment2
    )

    private fun bottomSheetFragment() = listOf(
        com.atech.attendance.R.id.addEditAttendanceBottomSheet,
        com.atech.attendance.R.id.detailViewBottomSheet,
        com.atech.attendance.R.id.archiveBottomSheet,
        com.atech.attendance.R.id.attendanceMenuBottomSheet,
        com.atech.attendance.R.id.addFromSyllabusBottomSheet
    )

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun setDrawerState(isOpen: Boolean) {
        if (isOpen) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun toggleDrawer() {
        binding.drawerLayout.apply {
            if (isDrawerOpen(GravityCompat.START)) {
                closeDrawer(GravityCompat.START)
            } else {
                openDrawer(GravityCompat.START)
            }
        }
    }

    override fun getDrawerLayout(): DrawerLayout {
        return binding.drawerLayout
    }

    override fun setDrawerEnabled(enabled: Boolean) {
        val lockMode =
            if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        binding.drawerLayout.setDrawerLockMode(lockMode)
    }
}
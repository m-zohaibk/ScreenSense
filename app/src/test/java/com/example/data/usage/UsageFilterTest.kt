package com.example.data.usage

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageFilterTest {

    @Test
    fun testMotoLauncherIdentified() {
        assertTrue(UsageStatsHelper.isLauncherApp("com.motorola.launcher3"))
        assertTrue(UsageStatsHelper.isLauncherApp("com.motorola.launcher"))
        assertTrue(UsageStatsHelper.isLauncherApp("com.motorola.motolauncher"))
        assertTrue(UsageStatsHelper.isLauncherApp("com.google.android.apps.nexuslauncher"))
        assertTrue(UsageStatsHelper.isLauncherApp("com.sec.android.app.launcher"))
        assertTrue(UsageStatsHelper.isLauncherApp("com.teslacoilsw.launcher"))
        assertTrue(UsageStatsHelper.isLauncherApp("com.android.launcher3"))
        assertTrue(UsageStatsHelper.isLauncherApp("com.miui.home"))
        assertTrue(UsageStatsHelper.isLauncherApp("com.oppo.launcher"))
        assertTrue(UsageStatsHelper.isLauncherApp("com.oneplus.launcher"))
    }

    @Test
    fun testUserAppsNotIdentifiedAsLauncher() {
        assertFalse(UsageStatsHelper.isLauncherApp("com.google.android.youtube"))
        assertFalse(UsageStatsHelper.isLauncherApp("com.android.chrome"))
        assertFalse(UsageStatsHelper.isLauncherApp("com.whatsapp"))
        assertFalse(UsageStatsHelper.isLauncherApp("com.instagram.android"))
        assertFalse(UsageStatsHelper.isLauncherApp("com.spotify.music"))
        assertFalse(UsageStatsHelper.isLauncherApp("com.notion.android"))
        assertFalse(UsageStatsHelper.isLauncherApp("com.duolingo"))
        assertFalse(UsageStatsHelper.isLauncherApp("com.netflix.mediaclient"))
        assertFalse(UsageStatsHelper.isLauncherApp("com.reddit.frontpage"))
        assertFalse(UsageStatsHelper.isLauncherApp("com.facebook.katana"))
    }
}

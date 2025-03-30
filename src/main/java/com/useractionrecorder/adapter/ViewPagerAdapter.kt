package com.useractionrecorder.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.useractionrecorder.ui.RecordingFragment
import com.useractionrecorder.ui.SchedulingFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RecordingFragment()
            1 -> SchedulingFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}
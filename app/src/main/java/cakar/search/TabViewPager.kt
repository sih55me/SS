package cakar.search

import android.app.ActionBar
import android.app.ActionBar.Tab
import android.app.FragmentTransaction


class TabAction(val select : ((Tab?, FragmentTransaction?) -> Unit), val unselect : ((Tab?,FragmentTransaction?) -> Unit)):
    ActionBar.TabListener {

    constructor(select: (Tab?,FragmentTransaction?) -> Unit) : this(select, {_,_->})
    override fun onTabSelected(
        tab: Tab?,
        ft: FragmentTransaction?
    ) {
        select(tab, ft)

    }

    override fun onTabUnselected(
        tab: Tab?,
        ft: FragmentTransaction?
    ) {
        unselect(tab, ft)
    }

    override fun onTabReselected(
        tab: Tab?,
        ft: FragmentTransaction?
    ) {
        
    }
}
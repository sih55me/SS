package cakar.search.ste.f

import android.app.Fragment
import android.app.LoaderManager
import android.app.SearchManager
import android.app.SearchableInfo
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import cakar.search.R
import cakar.search.SearchProvider
import cakar.search.databinding.HistoryprefBinding
import cakar.search.ste.Settings

class HistorySettings: Fragment() {

    val sm by lazy { activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager }

    val bin by lazy{ HistoryprefBinding.inflate(layoutInflater)}
    val adp by lazy {
        SimpleCursorAdapter(
            activity,
            android.R.layout.simple_list_item_1,
            null,
            arrayOf("display1"),
            intArrayOf(android.R.id.text1),
            CursorAdapter.FLAG_AUTO_REQUERY
        )
    }

    val loadH = object : LoaderManager.LoaderCallbacks<Cursor>{
        override fun onCreateLoader(
            p0: Int,
            p1: Bundle?
        ): Loader<Cursor?>? {
            val limit = 50

            val query = ""


            val authority = "content://${SearchProvider.AUTHORITY}/suggestions"
            println(authority)
            val uri= Uri.parse(authority)



            // get the query selection, may be null
            val selection = " ?"
            // inject query, either as selection args or inline
            var selArgs: Array<String> = arrayOf<String>(query)





            // finally, make the query
            return CursorLoader(activity,uri, null, null, null, null)
        }

        override fun onLoadFinished(
            p0: Loader<Cursor?>?,
            p1: Cursor?
        ) {
            print("LOAD DONE!!!-${p1?.count}")

            adp.changeCursor(p1)
            adp.notifyDataSetChanged()
        }

        override fun onLoaderReset(p0: Loader<Cursor?>?) {
            adp.changeCursor(null)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return bin.root
    }


    override fun onDestroy() {
        bin.menu.menu.clear()
        loaderManager.destroyLoader(0)
        super.onDestroy()
    }






    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val d = activity.contentResolver
        val cl = arrayOf("display1")

        loaderManager.initLoader<Cursor>(0,null, loadH)

        bin.preview.adapter = adp

        bin.menu.menu.apply {
            addSubMenu("Clear").also {
                it.item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setIcon(R.drawable.delete)
                it.add("Are you sure?").isEnabled = false
                it.add("Yes, Clear my history").setIcon(R.drawable.check).setOnMenuItemClickListener {
                    val suggestions = SearchRecentSuggestions(
                        activity,
                        SearchProvider.AUTHORITY, SearchProvider.MODE
                    )
                    suggestions.clearHistory()
                    loaderManager.restartLoader<Cursor>(0,null, loadH)
                    Toast.makeText(activity, "History cleared", Toast.LENGTH_SHORT).show()
                    true
                }
                it.add("No, Cancel").setIcon(R.drawable.close)
            }
            add("Reload")?.setOnMenuItemClickListener {
                loaderManager.restartLoader<Cursor>(0,null, loadH)
                true
            }
        }

    }


}
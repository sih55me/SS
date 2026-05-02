package cakar.search

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceCategory
import android.preference.PreferenceScreen
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.Toast
import cakar.search.com.DownAsse
import cakar.search.com.ProjectComponent
import cakar.search.databinding.SfxPlayerBinding
import cakar.search.filetype.Project
import cakar.search.wtbcore.PreviewImgPage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception

class AssetPage: PreferenceActivity() {

    internal var conjson = ""
    internal var pj = ProjectComponent()

    private val o :(ProjectComponent)->Unit={ content ->
        runOnUiThread {
            (preferenceScreen?.findPreference("sprites") as PreferenceCategory).removeAll()
            content.sprites.forEach { sprite ->
                println("Sprite: ${sprite.name} (stage: ${sprite.isStage})")
                sprite.costumes.forEach { println("  Costume: ${it.first}") }
                sprite.sounds.forEach { println("  Sound: ${it.first}") }
                preferenceScreen?.findPreference("version_project")?.summary = content.type.toString()
                (preferenceScreen?.findPreference("sprites") as PreferenceCategory).addPreference(spritePage(sprite, content.type))

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setWindowAnimations(android.R.style.Animation_Activity)
        when(resources.configuration.orientation){
            Configuration.ORIENTATION_LANDSCAPE -> {
                window!!.setLayout(window!!.windowManager.defaultDisplay.width/2, window!!.windowManager.defaultDisplay.height)
                window!!.setGravity(Gravity.END)
            }
            else -> {

            }

        }

        actionBar?.setDisplayHomeAsUpEnabled(true)
        val itemdata = intent.getParcelableExtra<Project>("item")
        if (itemdata == null){
            Toast.makeText(this, "No input project", 0).show()
        }

        actionBar?.setTitle("Asset from")
        actionBar?.setSubtitle(itemdata?.title)

        if (preferenceScreen == null){
            addPreferencesFromResource(R.xml.assetpage)
        }

        if (savedInstanceState?.getString("conjson").isNullOrEmpty()){
            ProjectComponent.fetchProjectContent(
                projectId = itemdata?.id.toString().orEmpty(),
                token = itemdata?.uninfo["project_token"].toString(),
                onSuccess = o,
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }else{
            ProjectComponent.parseProj(
                savedInstanceState.getString("conjson").orEmpty(),
                pj,
                o
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putString("conjson", conjson)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("Refresh")?.setOnMenuItemClickListener {
            val itemdata = intent.getParcelableExtra<Project>("item")
            ProjectComponent.fetchProjectContent(
                projectId = itemdata?.id.toString().orEmpty(),
                token = itemdata?.uninfo["project_token"].toString(),
                onSuccess = o,
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
            true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    fun spritePage(sp: ProjectComponent.Sprite, type:Int): PreferenceScreen  = preferenceManager.createPreferenceScreen(this).also {
        it!!
        it.title = sp.name
        it.addPreference(Preference(this).also { p->
            p.title = "Position (X,Y)"
            p.summary = "(${sp.x}, ${sp.y})"
        })
        it.addPreference(Preference(this).also { p->
            p.title = "Direction"
            p.summary = "${sp.direction} degree"
        })
        it.addPreference(Preference(this).also { p->
            p.title = "Costume"
            p.summary = "Contains ${sp.costumes.size}"
            p.onPreferenceClickListener = {
                val adap = ArrayAdapter(this, android.R.layout.simple_list_item_1, sp.costumes.map { it.first })
                AlertDialog.Builder(this).setTitle("Costume").setSingleChoiceItems(adap, 0){d,ind->
                    val u = when(type){
                        3 -> DownAsse.urlFromV3(sp.costumes[ind])
                        2 -> DownAsse.urlFromV2(sp.costumes[ind])
                        else -> ""
                    }
                    val pr = ProgressDialog(this)
                    pr.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    val imgl = object : Target{
                        override fun onBitmapLoaded(
                            p0: Bitmap?,
                            p1: Picasso.LoadedFrom?
                        ) {
                            pr.dismiss()
                            if(p0 == null){
                                return
                            }
                            val ph = PreviewImgPage(this@AssetPage, PreviewImgPage.Get(p0, sp.costumes[ind].first))
                            ph.show()
                        }

                        override fun onBitmapFailed(
                            p0: Exception?,
                            p1: Drawable?
                        ) {
                            pr.setMessage("FAILURE!\ne:${p0?.message}")
                            p0?.printStackTrace()
                        }

                        override fun onPrepareLoad(p0: Drawable?) {
                            pr.setMessage("Loading the image..")
                        }

                    }
                    pr.isIndeterminate = true
                    pr.setMessage("Progress start...")
                    pr.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel)){_,_->

                    }
                    pr.setOnDismissListener {
                        Picasso.get().cancelTag("loadSpriteImg")
                    }
                    pr.show()
                    if(!u.contains("svg")){
                        Picasso.get().load(u).tag("loadSpriteImg").into(imgl)
                    }else{
                        pr.setMessage("SVG preview in maintenance")
                    }
                }.setPositiveButton("Back", null).show()
                true
            }
        })
        it.addPreference(Preference(this).also { p->
            p.title = "Sound"
            p.summary = "Contains ${sp.sounds.size}"
            p.onPreferenceClickListener = {
                val adap = ArrayAdapter(this, android.R.layout.simple_list_item_1, sp.sounds.map { it.first })
                AlertDialog.Builder(this).setTitle("Sound").setSingleChoiceItems(adap, 0){d,ind->
                    println("Index sfx = $ind")
                    val u = when(type){
                        3 -> DownAsse.urlFromV3(sp.sounds[ind])
                        2 -> DownAsse.urlFromV2(sp.sounds[ind])
                        else -> ""
                    }
                    val pr = Dialog(this)
                    pr.setTitle(sp.sounds[ind].first)
                    val mcontroller = SfxPlayerBinding.inflate(layoutInflater)
                    val handler = Handler(Looper.getMainLooper())
                    val mMediaPlayer = MediaPlayer()
                    val mainlooplayer = object : Runnable {
                        override fun run() {
                            if(pr.window!!.decorView.alpha == 1F){
                                mcontroller.progress.progress = mMediaPlayer.currentPosition
                            }
                            mcontroller.progress.max = mMediaPlayer.duration
                            mcontroller.timeCurrent.text = "${ mMediaPlayer.currentPosition / 1000 }"
                            mcontroller.time.text = "${ mMediaPlayer.duration / 1000 }"

                            if(mMediaPlayer.isPlaying){
                                mcontroller.pause.text = "Pause"
                            }else{
                                mcontroller.pause.text = "Play"
                            }
                            handler.postDelayed(this, 500)
                        }
                    }

                    mcontroller.pause.setOnClickListener {
                        if(mMediaPlayer.isPlaying){
                            mMediaPlayer.pause()
                        }else{
                            mMediaPlayer.start()
                        }
                    }
                    pr.setContentView(mcontroller.root)
                    mMediaPlayer.apply {
                        setDataSource(u)
                        prepare()
                        setOnPreparedListener {
                            mcontroller.progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                                override fun onProgressChanged(
                                    p0: SeekBar?,
                                    p1: Int,
                                    p2: Boolean
                                ) {

                                }

                                override fun onStartTrackingTouch(p0: SeekBar?) {
                                    pr.window!!.decorView.alpha = 0.7F
                                }

                                override fun onStopTrackingTouch(p0: SeekBar?) {
                                    mMediaPlayer.seekTo(p0?.progress?:0)
                                    pr.window!!.decorView.alpha = 1F
                                }

                            })
                            handler.postDelayed(mainlooplayer, 500)
                        }
                        setAudioStreamType(AudioManager.STREAM_MUSIC)
                    }
                    pr.setOnDismissListener {
                        handler.removeCallbacks(mainlooplayer)
                        mMediaPlayer.reset()
                        mMediaPlayer.setOnPreparedListener(null)
                        mMediaPlayer.release()
                    }
                    pr.show()
                }.setPositiveButton("Back", null).show()
                true
            }
        })
    }

}
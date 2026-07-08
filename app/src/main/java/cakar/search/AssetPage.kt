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
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceCategory
import android.preference.PreferenceScreen
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import cakar.search.MainActivity.Companion.downloadSmthUri
import cakar.search.com.DownAsse
import cakar.search.com.ProjectComponent
import cakar.search.databinding.SfxPlayerBinding
import cakar.search.filetype.Project
import cakar.search.wtbcore.PreviewImgPage
import coil3.ImageLoader
import coil3.asDrawable
import coil3.decode.Decoder
import coil3.imageLoader
import coil3.load
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.request.target
import coil3.svg.SvgDecoder
import coil3.util.CoilUtils
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception
import java.util.Stack

class AssetPage: PreferenceActivity() {

    internal var conjson = ""
    internal var pj = ProjectComponent()

    val req: Stack<Disposable> = Stack()


    var loadingPopup : ActionMode? = null
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


    val itemdata get()= intent.getParcelableExtra<Project>("item")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        actionBar?.setDisplayHomeAsUpEnabled(true)

        if (itemdata == null){
            Toast.makeText(this, "No input project", 0).show()
        }

        actionBar?.setTitle("Asset from")
        actionBar?.setSubtitle(itemdata?.title)

        if (preferenceScreen == null){
            addPreferencesFromResource(R.xml.assetpage)
        }

        Thread{
            if (savedInstanceState?.getString("conjson").isNullOrEmpty()) {
                ProjectComponent.fetchProjectContent(
                    projectId = itemdata?.id.toString().orEmpty(),
                    token = itemdata?.uninfo["project_token"].toString(),
                    onSuccess = o,
                    onError = { error ->
                        runOnUiThread{
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                ProjectComponent.parseProj(
                    savedInstanceState.getString("conjson").orEmpty(),
                    pj,
                    o
                )
            }
        }.start()
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
        menu?.add("Download [sb3]")?.setOnMenuItemClickListener {
            downloadSmthUri(Uri.parse("https://projects.scratch.mit.edu/${itemdata?.id}?token=${itemdata?.uninfo["project_token"].toString()}"), "Dowloading scratch project"){

            }
            true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
    val lolod get()  = object: ActionMode.Callback{
        val p = ProgressBar(this@AssetPage, null, 0, android.R.style.Widget_Holo_ProgressBar_Horizontal)
        override fun onCreateActionMode(
            p0: ActionMode?,
            p1: Menu?
        ): Boolean {
            p.isIndeterminate = true
            if(p.parent ==null){
                p0?.customView = p
            }

            return true
        }

        override fun onPrepareActionMode(
            p0: ActionMode?,
            p1: Menu?
        )=false

        override fun onActionItemClicked(
            p0: ActionMode?,
            p1: MenuItem?
        )=false

        override fun onDestroyActionMode(p0: ActionMode?) {
            if(req.isEmpty().not()){
                req.get(req.size - 1).dispose()
            }
        }

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



                    val request = ImageRequest.Builder(this)
                        .data(u)
                        .target(
                            onStart = {
                                loadingPopup = (d as Dialog).window!!.decorView.startActionMode(lolod)

                            },
                            onError = {
                                loadingPopup?.customView = null
                                loadingPopup?.setTitle("ERROR!!")
                            },
                            onSuccess = {
                                loadingPopup?.finish()
                                loadingPopup = null
                                val ph = PreviewImgPage(this@AssetPage, PreviewImgPage.Get(it.asDrawable(resources), sp.costumes[ind].first))
                                ph.show()
                            }
                        )
                        .build()
                    ImageLoader.Builder(this).components {
                        if(u.contains("svg")){
                            add(SvgDecoder.Factory())
                        }
                    }.build().enqueue(request).let { l ->
                        req.push(l)
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
                                mcontroller.pause.setImageResource(R.drawable.play)
                                mcontroller.pause.contentDescription = "Pause"
                            }else{
                                mcontroller.pause.setImageResource(R.drawable.pause)
                                mcontroller.pause.contentDescription = "Play"
                            }
                            window.decorView.postDelayed(this, 500)
                        }
                    }


                    mcontroller.opt.setOnCreateContextMenuListener { menu, view, info ->
                        menu.add("Download").setOnMenuItemClickListener {
                            downloadSmthUri(Uri.parse(u), "DOWNLOAD AUDIO ASSET"){

                            }
                            true
                        }
                    }
                    mcontroller.opt.setOnClickListener {
                        it.showContextMenu()
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
                        prepareAsync()
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
                            window.decorView.postDelayed(mainlooplayer, 500)
                        }
                        setAudioStreamType(AudioManager.STREAM_MUSIC)
                    }
                    pr.setOnDismissListener {
                        window.decorView.removeCallbacks(mainlooplayer)
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
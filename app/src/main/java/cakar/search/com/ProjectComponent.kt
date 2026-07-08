package cakar.search.com

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import okio.IOException
import java.io.File
import java.util.logging.Level

class ProjectComponent {

    private val mSprites = mutableListOf<Sprite>()

    val sprites get()=mSprites.toList()
    //normal of scratch version project is 3
    var type = 3
    var originstory = ""
    private set


    data class ScratchScript(
        val topBlockId: String,
        val blocks: List<Block>
    )
    data class Block(
        val id: String,
        val opcode: String,  // "motion.move", "control.repeat", dll
        val fields: Map<String, Any> = emptyMap(),
        val inputs: Map<String, Any> = emptyMap(),
        val next: String? = null,
        var parent: String? = null,
        var isTopLevel: Boolean
    )



    companion object{
        fun fromJSON(s: String): ProjectComponent{
            val ins = ProjectComponent()
            ins.originstory = s
            try{
                val ob = JSONObject(s)
                val  j = Gson()
                j
                val target = ob.getJSONArray("targets")
                for (i in 0 until target.length()) {
                    val item = target.getJSONObject(i) ?: continue
                    ins.mSprites.add(continueItem(item))
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
            return ins
        }

        fun parseProj(b: String, pc: ProjectComponent, onSuccess: (ProjectComponent) -> Unit,){
            try {
                val json = JSONObject(b)
                if(json.has("targets")){
                    //scratch 3 project
                    val targets = json.getJSONArray("targets")
                    val sprites = pc.mSprites
                    pc.type = 3

                    try{
                        for (i in 0 until targets.length()) {
                            val target = targets.getJSONObject(i)
                            val sp = Sprite(
                                name = target.getString("name"),
                                isStage = target.getBoolean("isStage"),
                            )
                            parseScripts(target)
                            sp.x= target.optDouble("x", 0.0).toFloat()
                            sp.y= target.optDouble("y", 0.0).toFloat()
                            sp.direction = target.optDouble("direction", 90.0).toInt()
                            sp.size= target.optDouble("size", 100.0)
                            sp.visible   = target.optBoolean("visible", true)
                            // Parse costumes
                            val costumesArr = target.getJSONArray("costumes")
                            for (j in 0 until costumesArr.length()) {
                                val c = costumesArr.getJSONObject(j)
                                sp.costumes.add(
                                    Triple(
                                        c.getString("name"),
                                        c.getString("assetId"),
                                        c.getString("dataFormat")
                                    )
                                )
                            }

                            // Parse sounds
                            val soundsArr = target.getJSONArray("sounds")
                            for (j in 0 until soundsArr.length()) {
                                val s = soundsArr.getJSONObject(j)
                                sp.sounds.add(
                                    Triple(
                                        s.getString("name"),
                                        s.getString("assetId"),
                                        s.getString("dataFormat")
                                    )
                                )
                            }

                            sprites.add(sp)


                        }
                    }catch (_: Exception){

                    }
                }
                else if (json.has("objName")){
                    //scratch 2 project
                    val sprites = pc.mSprites
                    pc.type = 2

                    fun parseSprite2(obj: JSONObject, isStage: Boolean): Sprite {
                        // Parse costumes
                        val s2p = Sprite(
                            name     = obj.getString("objName"),
                            isStage  = isStage,
                        )
                        val costumesArr = obj.optJSONArray("costumes")
                        val costumes = s2p.costumes
                        if (costumesArr != null) {
                            for (i in 0 until costumesArr.length()) {
                                val c = costumesArr.getJSONObject(i)
                                costumes.add(Triple(
                                    c.getString("costumeName"),
                                    c.getString("baseLayerMD5"),
                                    ""
                                ))
                            }
                        }

                        // Parse sounds
                        val soundsArr = obj.optJSONArray("sounds")
                        val sounds = s2p.sounds
                        if (soundsArr != null) {
                            for (i in 0 until soundsArr.length()) {
                                val s = soundsArr.getJSONObject(i)
                                sounds.add(Triple(
                                    s.getString("soundName"),
                                    s.getString("md5"),
                                    ""
                                ))
                            }
                        }

                        return s2p
                    }

                    // Parse Stage
                    sprites.add(parseSprite2(json, isStage = true))

                    // Parse children (sprites)
                    val children = json.optJSONArray("children") ?: return
                    for (i in 0 until children.length()) {
                        val child = children.getJSONObject(i)

                        // Skip jika bukan sprite (misal: comment)
                        if (!child.has("objName")) continue

                        sprites.add(parseSprite2(child, isStage = false))
                    }


                }
                onSuccess(pc)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        fun fetchProjectContent(
            projectId: String,
            token: String,
            onSuccess: (ProjectComponent) -> Unit,
            onError: (String) -> Unit,
            onJson:(String)-> Unit = {}
        ) {
            val pc = ProjectComponent()
            println("https://projects.scratch.mit.edu/$projectId?token=${token}")
            val request = Request.Builder()
                .url("https://projects.scratch.mit.edu/$projectId?token=${token}")
                .addHeader("Accept", "application/json")
                .build()
            val client = OkHttpClient()
            client.newCall(request).execute().use {response->
                println("Content-Encoding: ${response.header("Content-Encoding")}")
                println("Content-Type: ${response.header("Content-Type")}")
                val body = response.body
                if (body == null){
                    onError("Respond close by system")
                }
                //prevent error
                body!!
                var b = body.source().readString(Charsets.UTF_8).also {
                    Log.i("IPR", it)
                }

                if(
                    (response.header("Content-Encoding") == "null") and
                    (response.header("Content-Type") == "binary/octet-stream")
                ){
                    //scratch 1 project(?)
                    onError("This project is too old")
                    return

                }


                if (!response.isSuccessful) {
                    onError("Error: ${response.code} \nM:${response.message}")
                    return
                }
                onJson(b)
                parseProj(b, pc, onSuccess)


            }
        }


        internal fun parseScripts(target: JSONObject): List<ScratchScript> {
            val blocksJson = target.getJSONObject("blocks")
            val allBlocks  = mutableMapOf<String, Block>()

            // Parse semua block
            blocksJson.keys().forEach { id ->
                val b = blocksJson.getJSONObject(id)

                // Parse inputs
                val inputsMap = mutableMapOf<String, Any>()
                val inputsJson = b.optJSONObject("inputs")
                inputsJson?.keys()?.forEach { key ->
                    inputsMap[key] = inputsJson.get(key)
                }

                // Parse fields
                val fieldsMap = mutableMapOf<String, Any>()
                val fieldsJson = b.optJSONObject("fields")
                fieldsJson?.keys()?.forEach { key ->
                    fieldsMap[key] = fieldsJson.get(key)
                }

                allBlocks[id] = Block(
                    id         = id,
                    opcode     = b.getString("opcode"),
                    next       = b.optString("next").takeIf { it.isNotEmpty() },
                    parent     = b.optString("parent").takeIf { it.isNotEmpty() },
                    isTopLevel = b.getBoolean("topLevel"),
                    inputs     = inputsMap,
                    fields     = fieldsMap
                )
            }

            // Kelompokkan jadi script (mulai dari topLevel block)
            val scripts = mutableListOf<ScratchScript>()

            allBlocks.values.filter { it.isTopLevel }.forEach { topBlock ->
                val chain = mutableListOf<Block>()
                var current: Block? = topBlock

                while (current != null) {
                    chain.add(current)
                    current = current.next?.let { allBlocks[it] }
                }

                scripts.add(ScratchScript(
                    topBlockId = topBlock.id,
                    blocks     = chain
                ))
            }

            return scripts
        }
        private fun continueItem(item: JSONObject): Sprite{
            try{
                return Sprite(item.getBoolean("isStage"), item.getString("name")).apply {
                    try{
                        if(!item.getBoolean("isStage")) {
                            position = Pair(item.getInt("x").toFloat(), item.getInt("y").toFloat())
                            direction = item.getInt("direction")
                        }
                    }catch (_: Exception){
                        //its stage
                    }
                }
            }catch (e: Exception){
                throw Error("Something wrong with ${::javaClass.name}", e)
            }
        }
    }
    data class Sprite  constructor(
        val isStage: Boolean,
        val name: String
    ){
        var originstory = ""
        var currentCostume = 0
        internal set

        var position = Pair(0F,0F)
        internal set


        var x
        set(value) {
            position = Pair(value,position.second)
        }
        get() = position.first

        var y
            set(value) {
                position = Pair(position.first, value)
            }
            get() = position.second


        var direction = 90
        internal set

        var size = 100.0
            internal set

        val variables = mutableMapOf<String, Any>()
        val lists = mutableMapOf<String, MutableList<Any>>()

        var visible = false

        /**order:
         *
         * name|md5ext|dataFormat
         */
        val costumes = mutableListOf<Triple<String, String, String>>()
        /**order:
         *
         * name|md5ext|dataFormat
         */
        val sounds = mutableListOf<Triple<String, String, String>>()





        var currentMessage: String = ""
        var messageExpiry: Long = 0L
        var parent: String? = null

    }

}
package cakar.search

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

class ProjectComponent {

    private val mSprites = mutableListOf<Sprite>()

    val sprites get()=mSprites.toList()

    var originstory = ""
    private set

    companion object{
        fun fromJSON(s: String): ProjectComponent{
            val ins = ProjectComponent()
            ins.originstory = s
            try{
                val ob = JSONObject(s)
                val target = ob.getJSONArray("target")
                for (i in 0 until target.length()) {
                    val item = target.getJSONObject(i) ?: continue
                    ins.mSprites.add(continueItem(item))
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
            return ins
        }

        private fun continueItem(item: JSONObject): Sprite{
            try{
                return Sprite(item.getBoolean("isStage"), item.getString("name")).apply {
                    try{
                        position = Pair(item.getInt("x"), item.getInt("y"))
                        direction = item.getInt("direction")
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

        var position = Pair(0,0)
        internal set

        var direction = 90
        internal set

        /**order:
         *
         * name|dataFormat|md5ext
         */
        internal val costumes = mutableListOf<Triple<String, String, String>>()
        /**order:
         *
         * name|dataFormat|md5ext
         */
        internal val sounds = mutableListOf<Triple<String, String, String>>()


        val asset get() = mutableMapOf(
            Pair("costumes", costumes.toList()),
            Pair("sounds", sounds.toList())

        )

    }

}
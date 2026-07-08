package cakar.search

import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class PersistentCookieJar(
    private val prefs: SharedPreferences
) : CookieJar {

    companion object {
        private const val KEY = "cookies"
    }

    private val cookies = mutableListOf<Cookie>()

    init {
        load()
    }

    override fun saveFromResponse(
        url: HttpUrl,
        newCookies: List<Cookie>
    ) {


        newCookies.forEach { newCookie ->

            cookies.removeAll {
                it.name == newCookie.name &&
                        it.domain == newCookie.domain &&
                        it.path == newCookie.path
            }
            cookies.add(newCookie)
        }
        save()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()

        return cookies.filter {
            it.expiresAt > now && it.matches(url)
        }
    }
    fun getCookie(
        host: String,
        name: String
    ): Cookie? {

        return cookies
            ?.firstOrNull {
                it.name == name
            }

    }

    fun clear() {

        cookies.clear()

        prefs.edit()
            .remove(KEY)
            .apply()

    }

    private fun save() {

        val root = JSONObject()


        val array = JSONArray()

        cookies.forEach {

            array.put(
                JSONObject()
                    .put("name", it.name)
                    .put("value", it.value)
                    .put("domain", it.domain)
                    .put("path", it.path)
                    .put("expiresAt", it.expiresAt)
                    .put("secure", it.secure)
                    .put("httpOnly", it.httpOnly)
                    .put("hostOnly", it.hostOnly)
            )

        }

        prefs.edit()
            .putString(KEY, array.toString())
            .apply()

    }

    private fun load() {
        val json = prefs.getString(KEY, null) ?: return

        val array = JSONArray(json)

        cookies.clear()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val builder = Cookie.Builder()
                .name(obj.getString("name"))
                .value(obj.getString("value"))
                .path(obj.getString("path"))
                .expiresAt(obj.getLong("expiresAt"))

            if (obj.getBoolean("hostOnly")) {
                builder.hostOnlyDomain(obj.getString("domain"))
            } else {
                builder.domain(obj.getString("domain"))
            }

            if (obj.getBoolean("secure"))
                builder.secure()

            if (obj.getBoolean("httpOnly"))
                builder.httpOnly()

            cookies += builder.build()
        }
    }


    fun dumpCookies() {
        cookies.forEach {
            println(
                "${it.name}=${it.value} | domain=${it.domain} | path=${it.path}"
            )
        }
    }

}
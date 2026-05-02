package cakar.search.com

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.io.File

object DownAsse {
    private val client = OkHttpClient()

    // ── URL Builder ────────────────────────────────────────

    fun urlFromV3(costume: Triple<String, String, String>): String =
        "https://assets.scratch.mit.edu/internalapi/asset/${costume.second}.${costume.third}/get/"

    fun urlFromV2(costume: Triple<String, String, String>): String =
        "https://assets.scratch.mit.edu/internalapi/asset/${costume.second}/get/"



    // ── Download ke File ───────────────────────────────────

    fun downloadAsset(
        url: String,
        destFile: File,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e.message ?: "Download failed")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError("Error: ${response.code}")
                    return
                }

                response.body!!.byteStream().use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                onSuccess(destFile)
            }
        })
    }
}
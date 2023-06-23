package my.ktbot.retrofit

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author bin
 * @version 1.0.0
 * @since 2023/6/23
 */
interface UniquekerApi {
    private object App {
        const val appid = "1637"
        const val appkey = "90c563c89c08c8ac4fea54c1e8bd9c0c"
    }

    @GET("/connect.php")
    suspend fun login(
            @Query("act") act: String = "login",
            @Query("appid") appid: String = App.appid,
            @Query("appkey") appkey: String = App.appkey,
            @Query("type") type: String = "qq",
            @Query("redirect_uri") redirectUri: String = "http://127.0.0.1:8081/login.html"
    ): ResponseBody

    @GET("/connect.php")
    suspend fun callback(
            @Query("act") act: String = "callback",
            @Query("appid") appid: String = App.appid,
            @Query("appkey") appkey: String = App.appkey,
            @Query("type") type: String = "qq",
            @Query("code") code: String
    ): ResponseBody

    companion object : UniquekerApi by run({
        val okHttpClient = OkHttpClient.Builder()
                .build()

        val retrofit = Retrofit.Builder()
                .callFactory(okHttpClient)
                .baseUrl("https://uniqueker.top/")
                .build()
        return@run retrofit.create(UniquekerApi::class.java)
    })
}

fun main() {
    runBlocking {
        val string = UniquekerApi.callback(code = "539020251EEA7C5F0B073A6A0B0491C4").string()
        println(string)
    }
}

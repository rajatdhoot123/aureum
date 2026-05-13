package kwiktwik.ratewatch.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AppIdInterceptor(private val packageName: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("X-App-Id", packageName)
            .build()
        return chain.proceed(request)
    }
}
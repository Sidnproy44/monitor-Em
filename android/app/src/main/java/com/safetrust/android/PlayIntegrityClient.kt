package com.safetrust.android

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import java.util.concurrent.TimeUnit

class PlayIntegrityClient(
    context: Context,
    private val cloudProjectNumber: Long
) {
    private val appContext = context.applicationContext

    fun requestToken(requestHash: String): String {
        require(requestHash.isNotBlank()) { "request hash is required" }
        require(cloudProjectNumber > 0L) { "Play Integrity cloud project number is required" }

        val manager = IntegrityManagerFactory.createStandard(appContext)
        val provider = Tasks.await(
            manager.prepareIntegrityToken(
                StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                    .setCloudProjectNumber(cloudProjectNumber)
                    .build()
            ),
            60,
            TimeUnit.SECONDS
        )

        val response = Tasks.await(
            provider.request(buildTokenRequest(requestHash)),
            60,
            TimeUnit.SECONDS
        )

        return response.token()
    }

    companion object {
        fun buildTokenRequest(requestHash: String): StandardIntegrityManager.StandardIntegrityTokenRequest {
            require(requestHash.isNotBlank()) { "request hash is required" }
            return StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                .setRequestHash(requestHash)
                .build()
        }
    }
}

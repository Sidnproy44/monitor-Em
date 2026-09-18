package com.safetrust.android

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64

object Crypto {
    const val ALGORITHM = "Ed25519"
    private const val KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "safetrust-device-ed25519-v1"
    private val ED25519_SPKI_PREFIX = byteArrayOf(0x30,0x2a,0x30,0x05,0x06,0x03,0x2b,0x65,0x70,0x03,0x21,0x00)

    fun ensureKeyPair(): KeyPair {
        require(Build.VERSION.SDK_INT >= 33) { "Android 13 (API 33) or newer is required for Ed25519 Keystore support" }
        val store = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        if (store.containsAlias(ALIAS)) {
            val privateKey = store.getKey(ALIAS, null) as PrivateKey
            val publicKey = store.getCertificate(ALIAS).publicKey
            return KeyPair(publicKey, privateKey)
        }
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, KEYSTORE)
        generator.initialize(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN)
            .setAlgorithmParameterSpec(ECGenParameterSpec("ed25519"))
            .setDigests(KeyProperties.DIGEST_NONE)
            .build())
        return generator.generateKeyPair()
    }

    fun publicKeyBase64Url(keyPair: KeyPair): String {
        val encoded = keyPair.public.encoded
        require(encoded.size == ED25519_SPKI_PREFIX.size + 32) { "Unexpected Ed25519 public-key encoding" }
        require(encoded.copyOfRange(0, ED25519_SPKI_PREFIX.size).contentEquals(ED25519_SPKI_PREFIX)) { "Unexpected Ed25519 public-key prefix" }
        return base64Url(encoded.copyOfRange(ED25519_SPKI_PREFIX.size, encoded.size))
    }

    fun sign(keyPair: KeyPair, message: String): String {
        val signer = Signature.getInstance(ALGORITHM)
        signer.initSign(keyPair.private)
        signer.update(message.toByteArray(StandardCharsets.UTF_8))
        val signature = signer.sign()
        require(signature.size == 64) { "Unexpected Ed25519 signature length" }
        return base64Url(signature)
    }

    fun challengeMessage(deviceId: String, challengeId: String, nonce: String): String =
        "SafeTrust.DeviceAuth.v1\n$deviceId\n$challengeId\n$nonce"

    fun base64Url(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

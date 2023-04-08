package ya.makhonko

import kotlinx.serialization.Serializable
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Serializable
data class Block(
    val index: String,
    val prev_hash: String,
    val hash: String,
    val data: String,
    val nonce: String
) {
    companion object {
        private val TO_CHECK_VALID = BigInteger.valueOf(1000000)

        private fun sumBlock(
            index: BigInteger,
            prev_hash: BigInteger,
            data: String,
            nonce: BigInteger
        ): BigInteger {
            var result = index.add(prev_hash).add(BigInteger(data.toByteArray(StandardCharsets.UTF_8))).add(nonce)
            return result
        }

        fun getHash(
            index: BigInteger,
            prev_hash: BigInteger,
            data: String,
            nonce: BigInteger
        ): BigInteger {
            val sumBlock = sumBlock(index, prev_hash, data, nonce)
            val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(sumBlock.toByteArray())
            return BigInteger(hash)
        }

        fun getHash(block: Block): BigInteger {
            val sumBlock = sumBlock(BigInteger(block.index), BigInteger(block.prev_hash), block.data, BigInteger(block.nonce))
            val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(sumBlock.toByteArray())
            return BigInteger(hash)
        }

        fun BigInteger.isValid(): Boolean = this.mod(TO_CHECK_VALID) == BigInteger.ZERO
    }
}
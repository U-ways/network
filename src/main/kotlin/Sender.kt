import Frame.Companion.EMPTY_FRAME
import Type.D
import Type.F
import java.util.*
import kotlin.math.ceil

/**
 * This class implements the sender.
 * Create and initialize a new MessageSender.
 *
 * @property mtu Int maximum transfer unit (the length of a frame must not exceed the MTU)
 * @property stdin Scanner Source of the messages
 */
class Sender(
    private val mtu: Int,
    private val stdin: Scanner = Scanner(System.`in`)
) : Runner {
    /**
     * Read a line from standard input layer and break it into frames
     * that are output on standard output, one frame per line.
     * Report any errors on standard error.
     */
    override fun start() {
        check(mtu >= EMPTY_FRAME.length) { "MTU size is too small to transmit a message. (minimum MTU size allowed: ${EMPTY_FRAME.length})" }
        check(mtu <= Frame.MTU) { "MTU size is not supported. (maximum MTU size allowed: ${Frame.MTU})" }

        stdin.nextLine().apply {
            if (this == null) error("No message received.")
            if (mtu == 10 && isNotEmpty()) error("MTU size is too small to transmit current message.")
            else when {
                EMPTY_FRAME.length + length <= mtu -> println(Frame(this))
                else -> {
                    val numberOfFrames = ceil(length / (mtu.toDouble() - EMPTY_FRAME.length)).toInt()

                    (0 until numberOfFrames).forEach { n ->
                        val lastFrame = n == numberOfFrames - 1
                        val start = n.times(mtu.minus(EMPTY_FRAME.length))
                        val end = start.plus(
                            if (lastFrame && length % mtu != 0)
                                length - ((numberOfFrames - 1) * mtu.minus(EMPTY_FRAME.length))
                            else
                                mtu.minus(EMPTY_FRAME.length)
                        )
                        println(Frame(substring(start, end), if (lastFrame) F else D))
                    }
                }
            }
        }
    }
}

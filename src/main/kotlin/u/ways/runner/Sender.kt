package u.ways.runner

import u.ways.frame.Frame
import u.ways.frame.Frame.Companion.FRAME_OVERHEAD
import u.ways.frame.Type.D
import u.ways.frame.Type.F
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
        check(mtu >= FRAME_OVERHEAD) { "MTU size is too small to transmit a message. (minimum MTU size allowed: ${FRAME_OVERHEAD})" }
        check(mtu <= Frame.MTU) { "MTU size is not supported. (maximum MTU size allowed: ${Frame.MTU})" }

        stdin.nextLine().apply {
            if (this == null) error("No message received.")
            if (mtu == 10 && isNotEmpty()) error("MTU size is too small to transmit current message.")
            else when {
                FRAME_OVERHEAD + length <= mtu -> println(Frame(this))
                else -> {
                    val sb = StringBuilder(length)
                    val frames = (0 until ceil(length / (mtu.toDouble() - FRAME_OVERHEAD)).toInt())

                    frames
                        .minus(frames.last)
                        .forEach { n ->
                            val start = n.times(mtu.minus(FRAME_OVERHEAD))
                            val end = start.plus(mtu.minus(FRAME_OVERHEAD))
                            sb.append(Frame(substring(start, end), D))
                            sb.append(System.lineSeparator())
                        }
                        .also {
                            val start = frames.last.times(mtu.minus(FRAME_OVERHEAD))
                            val end = start.plus(
                                if (length % mtu != 0) length - ((frames.last) * mtu.minus(FRAME_OVERHEAD))
                                else mtu.minus(FRAME_OVERHEAD)
                            )
                            println(sb.append(Frame(substring(start, end), F)))
                        }
                }
            }
        }
    }
}

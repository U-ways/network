import Frame.Segment.MAX_SEGMENT_LENGTH
import Type.D
import Type.F
import java.lang.System.err
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
        check(mtu < 100) { "MTU size should be between 0 and $MAX_SEGMENT_LENGTH\n" }

        stdin.nextLine().apply {
            when {
                this == null -> err.println("ERROR: No message received.")
                length <= mtu -> println(Frame(this))
                else -> {
                    val numberOfFrames = ceil(length / mtu.toDouble()).toInt()
                    (0 until numberOfFrames).forEach { n ->
                        val lastFrame = n == numberOfFrames - 1
                        val start = n * mtu
                        val end = start.plus(if (lastFrame && length % mtu != 0) length % mtu else mtu)
                        println(Frame(substring(start, end), if (lastFrame) F else D))
                    }
                }
            }
        }
    }
}

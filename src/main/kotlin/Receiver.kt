import Extractor.extract
import Frame.Companion.EMPTY_FRAME
import java.util.*

/**
 * This class implements the receiver.
 * Create and initialize a new MessageReceiver.
 *
 * @property mtu Int maximum transfer unit (MTU)
 * @property stdin Scanner Source of the messages
 */
class Receiver(
    private val mtu: Int,
    private val stdin: Scanner = Scanner(System.`in`)
) : Runner {
    /**
     * Receive a single message on stdin, one frame per line
     * and output the recreated message on stdout.
     * Report any errors on stderr.
     */
    override fun start() {
        check(mtu >= EMPTY_FRAME.length) { "MTU size is too small to receive a message. (minimum MTU size allowed: ${EMPTY_FRAME.length})" }
        check(mtu <= Frame.MTU) { "MTU size is not supported. (maximum MTU size allowed: ${Frame.MTU})" }

        println(
            StringBuilder().apply {
                var endOfMessage: Boolean

                do extract(stdin.nextLine(), mtu)
                    .let { (segment, isLastFrame) ->
                        append(segment)
                        endOfMessage = isLastFrame
                    }
                while (!endOfMessage)
            }.toString()
        )
    }
}
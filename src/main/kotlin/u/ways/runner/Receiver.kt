package u.ways.runner

import u.ways.frame.Extractor.extract
import u.ways.frame.Frame
import u.ways.frame.Frame.Companion.FRAME_OVERHEAD
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
        check(mtu >= FRAME_OVERHEAD) { "MTU size is too small to receive a message. (minimum MTU size allowed: ${FRAME_OVERHEAD})" }
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
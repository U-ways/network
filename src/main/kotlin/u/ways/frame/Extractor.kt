package u.ways.frame

import u.ways.frame.Frame.Checksum.calculate
import u.ways.frame.Frame.Delimiter.END
import u.ways.frame.Frame.Delimiter.FIELD
import u.ways.frame.Frame.Delimiter.START
import u.ways.frame.Type.*

object Extractor {
    fun extract(frame: String, mtu: Int): Pair<String, Boolean> {
        check(frame.length <= mtu) { "Frame length of [${frame.length}] is larger than the provided mtu of [$mtu]." }

        check(frame.first() == START) { "Illegal start of Frame. Expected [$START], actual [${frame.first()}]." }
        check(frame[1] == "$F"[0] || frame[1] == "$D"[0]) { "Illegal Frame Type. Expected [$F or $D], actual [${frame[1]}]." }
        check(frame[2] == FIELD) { "Illegal first field delimiter. Expected [$FIELD], actual [${frame[2]}]." }
        check(frame[5] == FIELD) { "Illegal second field delimiter. Expected [$FIELD], actual [${frame[5]}]." }
        check(frame.last() == END) { "Illegal end of Frame. Expected [$END], actual [${frame.last()}]." }

        val length = frame
            .substring(3..4)
            .toIntOrNull(10)
            ?: error("Corrupt segment length. Expected an integer number, actual [${frame.substring(3..4)}].")

        check(frame[length + 6] == FIELD) { "Illegal third field delimiter. Expected [$FIELD], actual [${frame[length + 1]}]." }

        val message = frame.substring(6..length + 5)
        val checksum = frame.substring(length + 7..length + 8)
        val verification = calculate(valueOf(frame[1].toString()), message)

        check(verification == checksum) { "Incorrect checksum. Expected [$verification], actual [$checksum])." }

        return Pair(message, frame[1] == "$F"[0])
    }
}

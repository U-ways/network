import Frame.Checksum.calculate
import Frame.Delimiter.END
import Frame.Delimiter.FIELD
import Frame.Delimiter.START
import Type.*

object Extractor {
    fun extract(frame: String, mtu: Int): Pair<String, Boolean> {
        /** MTU limit *************************************************************************************************/

        check(frame.length <= mtu) { "Frame length of [${frame.length}] is larger provided mtu of [$mtu]" }

        /** START delimiter *******************************************************************************************/

        check(frame[0] == START) { "Illegal Frame Start [${frame[0]}]" }

        /** FRAME type ************************************************************************************************/

        check(frame[1] == "$F"[0] || frame[1] == "$D"[0]) { "Illegal Frame Type [${frame[1]}]" }

        /** FIELD delimiter 1 *****************************************************************************************/

        check(frame[2] == FIELD) { "Illegal Field Delimiter [${frame[2]}]" }

        /** SEGMENT length ********************************************************************************************/

        val length = frame
            .substring(3..4)
            .toIntOrNull(10)
            ?: error("Corrupt segment length: [${frame.substring(3..4)}].")

        /** FIELD delimiter 2 *****************************************************************************************/

        check(frame[5] == FIELD) { "Illegal Field Delimiter [${frame[5]}]" }

        /** MESSAGE segment *******************************************************************************************/

        val message = frame.substring(6..length + 5)

        /** FIELD delimiter 3 *****************************************************************************************/

        check(frame[length + 6] == FIELD) { "Illegal Frame Delimiter [${frame[length + 1]}]" }

        /** CHECKSUM hexadecimal **************************************************************************************/

        val checksum = frame.substring(length + 7..length + 8)
        val verification = calculate(valueOf(frame[1].toString()), message)

        check(verification == checksum) { "Incorrect checksum. (expected [$verification], actual [$checksum])" }

        /** END delimiter *********************************************************************************************/

        check(frame.last() == END) { "Illegal Frame End [${frame.last()}]" }

        /** RESULT ****************************************************************************************************/

        return Pair(message, frame[1] == "$F"[0])
    }
}

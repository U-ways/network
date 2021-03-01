import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Specification
 * The data link layer (Sender) splits long messages into a number of segments.
 * Each message segment is packaged as a separate frame comprising several fields, as follows:
 *
 * |         | Frame     | Frame | Frame     | Segment | Frame     | Message | Field     |          | Frame     |
 * | Frame   | delimiter | type  | delimiter | length  | delimiter | segment | delimiter | Checksum | delimiter |
 * |         | (start)   |       |           |         |           |         |           |          | (end)     |
 * |---------|-----------|-------|-----------|---------|-----------|---------|-----------|----------|-----------|
 * | Example |    [      |   F   |     ~     |   05    |     ~     |  hello  |     ~     |    39    |     ]     |
 *
 * Notes:
 * - This frame format is designed to be read easily by humans, hence it differs in some ways from a real network protocol.
 *
 * - The segment length and checksum fields always contain two digits.
 *   Segment length values less than 10 have a leading zero and checksum values less than 16 have a leading zero (e.g. 0c).
 *
 * - A segment length of zero (00) means there is no message text and the message segment field is empty
 *   (i.e. there's nothing between the two field delimiters, which must still be present).
 *
 * - The segment length must not exceed 99, irrespective of the MTU (see below).
 *
 * - The message segment can contain any sequence of characters supported by the Java String class,
 *   including those matching the delimiters ('[', ']', '~') and unprintable control codes.
 *
 * - Character escaping/stuffing is not required for this protocol. An important element of message
 *   handling is that a message will not contain either a \n or \r character. This is because the Sender
 *   is to be written on the assumption that a single line of input contains the complete message to be sent.
 *   You do not have to test for this assumption in your implementation - it will not be tested with multi-line messages.
 *
 * - The checksum is calculated from the hexadecimal value of the arithmetic sum of all preceding characters in the frame
 *   except for the starting frame delimiter (i.e. from the frame type through to the final field delimiter, inclusive).
 *   Only the last two hexadecimal digits are recorded (e.g. if the decimal arithmetic sum is 1234, the checksum is d2)
 *   because decimal 1234 is hexadecimal 4d2.
 *
 * - The total length of the frame including all delimiters must not exceed the MTU (maximum transfer unit).
 *   The value of the MTU is passed to Sender and Receiver as a command line argument pair in the form
 *
 *   --mtu N
 * where N is a non-negative decimal value.
 *
 * Note:
 *   That there is an effective limit to the size of the MTU because there is a limit of 99 characters on the length
 *   of the message segment. If an MTU is specified to the Sender that is larger than the effective limit then this
 *   should not be reported as an error. Instead, simply treat the MTU value is being the same as its effective limit
 *   and process the message normally.
 *
 * - Any deviation from the specified format should be treated as an error.
 *   This includes frames whose length exceeds the MTU.
 */
class FrameTest {
    @Test
    fun `given message larger than 99, when frame is created, then IllegalStateException should be thrown`() {
        val msg = "N".repeat(100)
        assertThrows<IllegalStateException>("Should throw an Exception") { Frame(msg) }
    }

    @Test
    fun `given empty message, when frame access segment length, then length of 00 should be returned`() {
        Frame("").segment shouldBeEqualTo "00"
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "hello                                     | 05",
            "Who Framed                                | 10",
            "The Byte C                                | 10",
            "This is a longer message and will requir  | 40",
        ],
        delimiter = '|'
    )
    fun `given frame with x msg, when frame access segment length, then frame should return correct length`(
        msg: String, expected: String
    ) {
        Frame(msg.trim()).segment shouldBeEqualTo expected.trim()
    }

    @Test
    fun `given empty message, when frame access checksum value, then value of 00 should be returned`() {
        Frame("").checksum shouldBeEqualTo "20"
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "hello                                     | F | 39",
            "Who Framed                                | D | bc",
            "The Byte C                                | D | 57",
            "This is a longer message and will requir  | D | c6",
        ],
        delimiter = '|'
    )
    fun `given frame with x msg, when frame access checksum value, then frame should return correct length`(
        msg: String, type: String, expected: String
    ) {
        Frame(msg.trim(), Type.valueOf(type)).checksum shouldBeEqualTo expected.trim()
    }

    @Test
    fun `given frame with msg 'hello', when frame invokes toString, then frame should return expected value`() {
        Frame("hello").toString() shouldBeEqualTo "[F~05~hello~39]"
    }

    @Test
    fun `given frame with msg 'Who Framed', when frame invokes toString, then frame should return expected value`() {
        Frame("Who Framed", Type.D).toString() shouldBeEqualTo "[D~10~Who Framed~bc]"
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "hello                                    | F | [F~05~hello~39]",
            "Who Framed                               | D | [D~10~Who Framed~bc]",
            "The Byte C                               | D | [D~10~The Byte C~57]",
            "This is a longer message and will requir | D | [D~40~This is a longer message and will requir~c6]",
        ],
        delimiter = '|'
    )
    fun `given frame with msg, when frame invokes toString, then frame should return expected value`(
        msg: String, type: String, expected: String
    ) {
        Frame(msg.trim(), Type.valueOf(type)).toString() shouldBeEqualTo expected.trim()
    }

}
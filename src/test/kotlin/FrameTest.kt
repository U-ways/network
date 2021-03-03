import Frame.Checksum
import Frame.Segment
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class FrameTest {
    @Test
    fun `given empty message, when frame access segment length, then length of 00 should be returned`() {
        Segment.length(Frame("").message) shouldBeEqualTo "00"
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
        Segment.length(Frame(msg.trim()).message) shouldBeEqualTo expected.trim()
    }

    @Test
    fun `given empty message, when frame access checksum value, then value of 20 should be returned`() {
        val frame = Frame("")
        Checksum.calculate(frame.type, frame.message) shouldBeEqualTo "20"
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
        val frame = Frame(msg.trim(), Type.valueOf(type))
        Checksum.calculate(frame.type, frame.message) shouldBeEqualTo expected.trim()
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
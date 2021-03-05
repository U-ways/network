package u.ways.runner

import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import u.ways.frame.Frame
import u.ways.frame.Frame.Checksum.calculate
import u.ways.frame.Frame.Companion.FRAME_OVERHEAD
import u.ways.frame.Type.F
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*
import java.util.stream.Stream


class ReceiverTest {
    private lateinit var scanner: Scanner
    private lateinit var outputStreamCaptor: ByteArrayOutputStream

    // Reassign Stdout stream to outputStreamCaptor:
    @BeforeEach
    fun setUp() {
        scanner = mockkClass(Scanner::class)
        outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
    }

    private fun getOutput(): String = outputStreamCaptor.toString().trim()

    @Test
    fun `given MTU smaller than FRAME_OVERHEAD, when receiver starts, it should throw IllegalStateException`() {
        assertThrows<IllegalStateException>() { Receiver(FRAME_OVERHEAD - 1, scanner).start() }
    }

    @Test
    fun `given MTU larger than supported size, when sender starts, it should throw IllegalStateException`() {
        val supportedSize = Frame.MTU

        assertThrows<IllegalStateException>() { Receiver(supportedSize + 1, scanner).start() }
    }

    @Test
    fun `given final frame type, when receiver extracts frame, it should stop checking standard input`() {
        val frame = "[F~05~hello~39]"

        every { scanner.nextLine() } returns frame

        Receiver(20, scanner).start()

        verify(exactly = 1) { scanner.nextLine() }
    }

    @Test
    fun `given frame larger than MTU, when receiver extracts frame, it should throw IllegalStateException`() {
        val mtu = 10
        val frame = "[F~05~hello~39]"

        every { scanner.nextLine() } returns frame

        val exception = assertThrows<IllegalStateException>() {
            Receiver(mtu, scanner).start()
        }

        exception.message shouldBeEqualTo "Frame length of [${frame.length}] is larger provided mtu of [$mtu]"
    }

    @Test
    fun `given frame without start delimiter, when receiver extracts frame, it should throw IllegalStateException`() {
        val frame = "F~05~hello~39]"

        every { scanner.nextLine() } returns frame

        val exception = assertThrows<IllegalStateException>() {
            Receiver(20, scanner).start()
        }

        exception.message shouldBeEqualTo "Illegal Frame Start [${frame[0]}]"
    }

    @Test
    fun `given frame with incorrect frame type, when receiver extracts frame, it should throw IllegalStateException`() {
        val frame = "[Q~05~hello~39]"

        every { scanner.nextLine() } returns frame

        val exception = assertThrows<IllegalStateException>() {
            Receiver(20, scanner).start()
        }

        exception.message shouldBeEqualTo "Illegal Frame Type [${frame[1]}]"
    }

    @Test
    fun `given frame with incorrect field delimiter 1, when receiver extracts frame, it should throw IllegalStateException`() {
        val frame = "[F05~hello~39]"

        every { scanner.nextLine() } returns frame

        val exception = assertThrows<IllegalStateException>() {
            Receiver(20, scanner).start()
        }

        exception.message shouldBeEqualTo "Illegal Field Delimiter [${frame[2]}]"
    }

    @Test
    fun `given frame with un-parsable segment length, when receiver extracts frame, it should throw IllegalStateException`() {
        val frame = "[F~XX~hello~39]"

        every { scanner.nextLine() } returns frame

        val exception = assertThrows<IllegalStateException>() {
            Receiver(20, scanner).start()
        }

        exception.message shouldBeEqualTo "Corrupt segment length: [${frame.substring(3..4)}]."
    }

    @Test
    fun `given frame with incorrect field delimiter 2, when receiver extracts frame, it should throw IllegalStateException`() {
        val frame = "[F~05hello~39]"

        every { scanner.nextLine() } returns frame

        val exception = assertThrows<IllegalStateException>() {
            Receiver(20, scanner).start()
        }

        exception.message shouldBeEqualTo "Illegal Field Delimiter [${frame[5]}]"
    }

    @Test
    fun `given frame with incorrect field delimiter 3, when receiver extracts frame, it should throw IllegalStateException`() {
        val frame = "[F~05~hello~FF]"
        val checksum = "FF"
        val verification = calculate(F, "hello")

        every { scanner.nextLine() } returns frame

        val exception = assertThrows<IllegalStateException>() {
            Receiver(20, scanner).start()
        }

        exception.message shouldBeEqualTo "Incorrect checksum. (expected [$verification], actual [$checksum])"
    }

    @Test
    fun `given frame with incorrect end delimiter, when receiver extracts frame, it should throw IllegalStateException`() {
        val frame = "[F~05~hello~39"

        every { scanner.nextLine() } returns frame

        val exception = assertThrows<IllegalStateException>() {
            Receiver(20, scanner).start()
        }

        exception.message shouldBeEqualTo "Illegal Frame End [${frame.last()}]"
    }

    @ParameterizedTest
    @MethodSource("messageReceiverArgs")
    fun `given receiver with X MTU, when receiver extracts Y frame, then receiver should stdout Z msg`(
        mtu: Int, frame: String, message: String
    ) {
        every { scanner.nextLine() } returnsMany frame.split("\n")

        Receiver(mtu, scanner).start()

        getOutput() shouldBeEqualTo message
    }

    @Test
    fun `given receiver with 20 MTU, when receiver extracts valid frame, then receiver should stdout correct message`() {
        val message = "Hi"
        val frame = "[F~02~Hi~d3]"

        every { scanner.nextLine() } returns frame

        Receiver(20, scanner).start()

        getOutput() shouldBeEqualTo message
    }

    @Test
    fun `given empty frame, when receiver extracts empty frame, then receiver should stdout empty line`() {
        val emptyMessage = ""
        val emptyFrame = "[F~00~~20]"

        every { scanner.nextLine() } returns emptyFrame

        Receiver(20, scanner).start()

        getOutput() shouldBeEqualTo emptyMessage
    }

    @Test
    fun `given frame within a frame, when receiver extracts frame, then receiver should stdout correct message`() {
        val frame = "[F~12~[F~02~Hi~d3]~45]"
        val message = "[F~02~Hi~d3]"

        every { scanner.nextLine() } returns frame

        Receiver(50, scanner).start()

        getOutput() shouldBeEqualTo message
    }

    companion object {
        @JvmStatic
        private fun messageReceiverArgs() = Stream.of(
            of(
                50,
                "[D~40~This is a longer message and will requir~c6]\n[F~21~e more than one frame~b3]",
                "This is a longer message and will require more than one frame"
            ),
            of(
                50,
                "[D~40~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~d2]\n[D~40~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~d2]\n[D~40~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~d2]\n[F~31~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~26]",
                "¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾"
            )
        )
    }
}
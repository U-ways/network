import Frame.Companion.EMPTY_FRAME
import Frame.Companion.FRAME_OVERHEAD
import io.mockk.every
import io.mockk.mockkClass
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*
import java.util.UUID.randomUUID
import java.util.stream.Stream


class SenderTest {
    private val standardOut = System.out
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
    fun `given MTU smaller than FRAME_OVERHEAD, when sender starts, it should throw IllegalStateException`() {
        assertThrows<IllegalStateException>() { Sender(FRAME_OVERHEAD - 1, scanner).start() }
    }

    @Test
    fun `given MTU larger than supported size, when sender starts, it should throw IllegalStateException`() {
        val supportedSize = Frame.MTU

        assertThrows<IllegalStateException>() { Sender(supportedSize + 1, scanner).start() }
    }

    @Test
    fun `given MTU size of minimum frame size, when sender sends an empty message, it should return an empty frame`() {
        val emptyMsg = ""

        every { scanner.nextLine() } returns emptyMsg

        Sender(10, scanner).start()

        getOutput() shouldBeEqualTo EMPTY_FRAME
    }

    @Test
    fun `given MTU size of minimum frame size, when sender sends a non-empty message, it should throw IllegalStateException`() {
        every { scanner.nextLine() } returns "${randomUUID()}".substring(0..15)

        assertThrows<IllegalStateException>() { Sender(10, scanner).start() }
    }

    @Test
    fun `given a null message, when sender sends nul message, it should throw IllegalStateException`() {
        val emptyMsg = null

        every { scanner.nextLine() } returns emptyMsg

        assertThrows<IllegalStateException>() { Sender(10, scanner).start() }
    }

    @Test
    fun `given sender, when message sender sends msg as input message, then frame should contain hello`() {
        val msg = randomUUID().toString().substring(0, 5)

        every { scanner.nextLine() } returns msg

        Sender(20, scanner).start()

        getOutput() shouldContain msg
    }

    @Test
    fun `given sender with 20 MTU, when message sender sends hi as input message, then sender should stdout correct frame`() {
        val msg = "Hi"
        val expected = "[F~02~Hi~d3]"

        every { scanner.nextLine() } returns msg

        Sender(20, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @Test
    fun `given sender with 20 MTU, when message sender sends an empty message, then sender should segment the frame length with 00`() {
        val msg = ""
        val expected = "[F~00~~20]"

        every { scanner.nextLine() } returns msg

        Sender(20, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @Test
    fun `given sender with 20 MTU, when sender sends a message fits in one frame, then sender should not segment the frame`() {
        val msg = "hello"
        val expected = "[F~05~hello~39]"

        every { scanner.nextLine() } returns msg

        Sender(20, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @Test
    fun `given sender with 20 MTU, when message sender sends a that doesn't fit in one frame, then sender should segment the frame`() {
        val msg = "The Byte Count of Monte Cristo"
        val expected = "[D~10~The Byte C~57]\n" + "[D~10~ount of Mo~b6]\n" + "[F~10~nte Cristo~fc]"

        every { scanner.nextLine() } returns msg

        Sender(20, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @Test
    fun `given sender with 20 MTU, when message sender sends a that doesn't fit in one frame with odd length, then sender should segment the frame correctly`() {
        val msg = "Who Framed Roger Rabbit"
        val expected = "[D~10~Who Framed~bc]\n" + "[D~10~ Roger Rab~73]\n" + "[F~03~bit~62]"

        every { scanner.nextLine() } returns msg

        Sender(20, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @Test
    fun `given frame as a message, when sender sends frame as input message, then sender should stdout correct frame`() {
        val msg = "[F~02~Hi~d3]"
        val expected = "[F~12~[F~02~Hi~d3]~45]"

        every { scanner.nextLine() } returns msg

        Sender(50, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @ParameterizedTest
    @MethodSource("messageSenderArgs")
    fun `given sender with X MTU, when sender sends Y message, then sender should stdout Z frame`(
        mtu: Int, message: String, frame: String
    ) {
        every { scanner.nextLine() } returns message

        Sender(mtu, scanner).start()

        getOutput() shouldBeEqualTo frame
    }

    // restore Stdout to its original state when test terminates:
    @AfterEach
    fun tearDown() {
        System.setOut(standardOut)
    }

    companion object {
        @JvmStatic
        private fun messageSenderArgs() = Stream.of(
            of(
                50,
                "This is a longer message and will require more than one frame",
                "[D~40~This is a longer message and will requir~c6]\n[F~21~e more than one frame~b3]"
            ),
            of(
                50,
                "¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾",
                "[D~40~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~d2]\n[D~40~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~d2]\n[D~40~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~d2]\n[F~31~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~26]"
            )
        )
    }
}
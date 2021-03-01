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
    fun `given MTU larger than 99, when sender starts, it should throw IllegalStateException`() {
        assertThrows<IllegalStateException>("Should throw IllegalStateException") { Sender(100, scanner).start() }
    }

    @Test
    fun `given MessageSender, when message sender sends msg as input message, then frame should contain hello`() {
        val msg = randomUUID().toString().substring(0, 5)

        every { scanner.nextLine() } returns msg

        Sender(20, scanner).start()

        getOutput() shouldContain msg
    }

    @Test
    fun `given MessageSender with 20 MTU, when message sender sends hi as input message, then sender should stdout F~02~Hi~d3`() {
        val msg = "Hi"
        val expected = "[F~02~Hi~d3]"

        every { scanner.nextLine() } returns msg

        Sender(20, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @Test
    fun `given MessageSender with 20 MTU, when message sender sends an empty message, then sender should segment the frame length with 0`() {
        val msg = ""
        val expected = "[F~00~~20]"

        every { scanner.nextLine() } returns msg

        Sender(20, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @Test
    fun `given MessageSender with 20 MTU, when message sender sends a that fits in one frame, then sender should not segment the frame`() {
        val msg = "hello"
        val expected = "[F~05~hello~39]"

        every { scanner.nextLine() } returns msg

        Sender(20, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @Test
    fun `given MessageSender with 10 MTU, when message sender sends a that doesn't fit in one frame, then sender should segment the frame`() {
        val msg = "The Byte Count of Monte Cristo"
        val expected = "[D~10~The Byte C~57]\n" + "[D~10~ount of Mo~b6]\n" + "[F~10~nte Cristo~fc]"

        every { scanner.nextLine() } returns msg

        Sender(10, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @Test
    fun `given MessageSender with 10 MTU, when message sender sends a that doesn't fit in one frame with odd length, then sender should segment the frame correctly`() {
        val msg = "Who Framed Roger Rabbit"
        val expected = "[D~10~Who Framed~bc]\n" + "[D~10~ Roger Rab~73]\n" + "[F~03~bit~62]"

        every { scanner.nextLine() } returns msg

        Sender(10, scanner).start()

        getOutput() shouldBeEqualTo expected
    }

    @ParameterizedTest
    @MethodSource("messageSenderArgs")
    fun `given MessageSender with X MTU, when message sender sends Y msg, then sender should stdout Z frame`(
        mtu: Int, msg: String, expected: String
    ) {
        every { scanner.nextLine() } returns msg

        Sender(mtu, scanner).start()

        getOutput() shouldBeEqualTo expected
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
                40,
                "This is a longer message and will require more than one frame",
                "[D~40~This is a longer message and will requir~c6]\n[F~21~e more than one frame~b3]"
            ),
            of(
                40,
                "¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾",
                "[D~40~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~d2]\n[D~40~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~d2]\n[D~40~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~d2]\n[F~31~¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾¾~26]"
            )
        )
    }
}
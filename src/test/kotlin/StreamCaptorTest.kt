import io.mockk.every
import io.mockk.mockkClass
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*

/**
 * This is a test support to verify my stdout and scanner value capturing techniques that
 * I use throughout my acceptance testing environment.
 *
 * @property standardOut java.io.PrintStream
 * @property scanner Scanner
 * @property outputStreamCaptor ByteArrayOutputStream
 */
class StreamCaptorTest {
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

    private fun getOutput(): String = outputStreamCaptor.toString()

    @Test
    fun `given StdOut redirection, when Println invoked, then OutputCaptor should match`() {
        val expected = "Test outputStreamCaptor with Println"

        println(expected)
        getOutput().trim() shouldBeEqualTo expected
        getOutput().trim() shouldNotBeEqualTo expected.plus("Invalid")
    }

    @Test
    fun `given a mocked StdIn Scanner, when stdin nextLine invoked, then mocked value should be returned`() {
        val expected = "Mocked value 1"

        every { scanner.nextLine() } returns expected

        scanner.nextLine() shouldBeEqualTo expected
        scanner.nextLine().plus("Invalid") shouldNotBeEqualTo expected
    }

    @Test
    fun `given a mocked StdIn Scanner, when stdin nextLine invoked more than once, then correct mocked values should be returned`() {
        val expectedFirst = "Mocked value 1"
        val expectedSecond = "Mocked value 2"

        every { scanner.nextLine() } returns expectedFirst andThen expectedSecond

        scanner.nextLine() shouldBeEqualTo expectedFirst
        scanner.nextLine() shouldBeEqualTo expectedSecond
    }

    // restore Stdout to its original state when test terminates:
    @AfterEach
    fun tearDown() {
        System.setOut(standardOut)
    }
}
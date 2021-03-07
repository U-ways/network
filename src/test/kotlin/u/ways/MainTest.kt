package u.ways

import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTest {
    private val standardError = System.err
    private lateinit var errorStreamCaptor: ByteArrayOutputStream

    @BeforeEach
    fun setUp() {
        errorStreamCaptor = ByteArrayOutputStream()
        System.setErr(PrintStream(errorStreamCaptor))
    }

    private fun getError(): String = errorStreamCaptor.toString().trim()

    @Test
    fun `given not enough number of arguments, when main is invoked, it should warn user using stderr`() {
        val singleCommand: Array<String> = emptyArray()

        assertDoesNotThrow() { Main.main(singleCommand) }

        getError() shouldContain "ERROR: Not enough command arguments passed."
    }

    @Test
    fun `given incorrect MTU value, when main is invoked, it should warn user using stderr`() {
        val incorrectMtuCommand: Array<String> = arrayOf("-R", "--mtu", "invalid")

        assertDoesNotThrow() { Main.main(incorrectMtuCommand) }

        getError() shouldContain "ERROR: MTU value given: [invalid] is not a number."
    }

    @Test
    fun `given an incorrect runner command, when main is invoked with the wrong command, it warn user using stderr and not throw any exceptions`() {
        val incorrectRunnerCommand: Array<String> = arrayOf("-invalid", "--mtu", "20")

        assertDoesNotThrow() { Main.main(incorrectRunnerCommand) }

        getError() shouldContain "ERROR: Unknown argument given: [-invalid]."
    }

    @AfterEach
    fun tearDown() {
        System.setErr(standardError)
    }
}
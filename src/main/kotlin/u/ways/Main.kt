package u.ways

import u.ways.frame.Frame
import u.ways.runner.Receiver
import u.ways.runner.Runner
import u.ways.runner.Sender
import java.lang.System.err
import java.lang.System.out

/**
 * Main class for running the Message Sender or Receiver.
 * The runner type and MTU value is received via the command-line arguments.
 */
class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val usageExample = "(Usage example: ppd -R --mtu 20)"

            try {
                check(args.size >= 3) { "Not enough commands arguments passed. $usageExample" }

                val mtu: Int = args[2]
                    .toIntOrNull()
                    .apply { check(this != null) { "MTU value given: [${args[2]}] is not a number. $usageExample" } }
                    .let { if (it!! > Frame.MTU) Frame.MTU else it }

                val runner: Runner = when (val type = args[0]) {
                    "-S" -> Sender(mtu)
                    "-R" -> Receiver(mtu)
                    else -> error("Unknown runner argument given: [$type]. $usageExample")
                }

                runner.start()
            } catch (ex: Exception) {
                err.println("ERROR: ${ex.message}")
            }

            out.flush()
            err.flush()
        }
    }
}
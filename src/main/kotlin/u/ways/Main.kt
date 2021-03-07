package u.ways

import u.ways.frame.Frame
import u.ways.runner.Receiver
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
            try {
                check(args.isNotEmpty()) { "Not enough command arguments passed. \n\n$COMMAND_HELP" }

                val runner = when (val type = args[0]) {
                    "-S" -> Sender(args.extractMtu())
                    "-R" -> Receiver(args.extractMtu())
                    "-help" -> return println(COMMAND_HELP)
                    else -> error("Unknown argument given: [$type]. $COMMAND_HELP_HINT")
                }

                runner.start()
            } catch (ex: Exception) {
                err.println("ERROR: ${ex.message}")
            }

            out.flush()
            err.flush()
        }

        private fun Array<String>.extractMtu(): Int {
            check(this.size >= 3) { "Missing runner MTU arguments. $COMMAND_HELP_HINT" }
            check(this[1] == "--mtu") { "Unknown argument given: [${this[1]}]. $COMMAND_HELP_HINT" }

            return this[2]
                .toIntOrNull()
                .also { check(it != null) { "MTU value given: [${this[2]}] is not a number. $COMMAND_HELP_HINT" } }
                .let { if (it!! > Frame.MTU) Frame.MTU else it }
        }

        private const val COMMAND_HELP_HINT = "Type `ppd -help` for command manual."

        private val COMMAND_HELP = """
            NAME
              
              ppd - penny plain data link protocol

            SYNOPSIS
              
              ppd -[S|R] --mtu [N]
              ppd -help

            DESCRIPTION
              
              send or receive messages using penny plain data link protocol
              
              -[S|R]
                     The type of runner this host is:
                       S = Message Sender
                       R = Message Receiver

              --mtu [N]
                     The maximum transfer unit (MTU)
                       A non-negative decimal value.
              
              -help  
                     prints this message
            
            USAGE EXAMPLE
            
              ppd -S --mtu 20  # Run PPD as a sender host with MTU of 20
              ppd -R --mtu 20  # Run PPD as a receiver host with MTU of 20
        """.trimIndent()
    }
}
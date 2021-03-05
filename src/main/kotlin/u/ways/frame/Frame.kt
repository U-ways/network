package u.ways.frame

import u.ways.frame.Frame.Delimiter.END
import u.ways.frame.Frame.Delimiter.FIELD
import u.ways.frame.Frame.Delimiter.START
import u.ways.frame.Frame.Segment.MAX_SEGMENT_LENGTH
import u.ways.frame.Type.F

data class Frame(
    val message: String,
    val type: Type = F,
) {
    companion object {
        val EMPTY_FRAME: String = "".let { emptyMessage ->
            START
                .plus(F.name)
                .plus(FIELD)
                .plus(Segment.length(emptyMessage))
                .plus(FIELD)
                .plus(emptyMessage)
                .plus(FIELD)
                .plus(Checksum.calculate(F, emptyMessage))
                .plus(END)
        }

        val FRAME_OVERHEAD = EMPTY_FRAME.length

        val MTU = MAX_SEGMENT_LENGTH + FRAME_OVERHEAD
    }

    object Delimiter {
        const val START = '['
        const val FIELD = '~'
        const val END = ']'
    }

    object Segment {
        const val MAX_SEGMENT_LENGTH = 99

        fun length(msg: String): String = msg.length
            .toString().padStart(2, '0')
    }

    object Checksum {
        fun calculate(type: Type, msg: String): String = FIELD
            .toInt()
            .times(3)
            .plus(Segment.length(msg).toCharArray().sumBy { it.toInt() })
            .plus(type.name.toCharArray().sumBy { it.toInt() })
            .plus(msg
                .takeUnless { it.isEmpty() }
                ?.toCharArray()
                ?.sumBy { it.toInt() }
                ?: 0)
            .toString(16)
            .takeLast(2)
            .padStart(2, '0')
    }

    override fun toString(): String = START
        .plus(type.name)
        .plus(FIELD)
        .plus(Segment.length(message))
        .plus(FIELD)
        .plus(message)
        .plus(FIELD)
        .plus(Checksum.calculate(type, message))
        .plus(END)
}
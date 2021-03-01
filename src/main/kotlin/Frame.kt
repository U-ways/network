data class Frame(
    val message: String,
    val type: Type = Type.F,
    val staDelimiter: Char = '[',
    val medDelimiter: Char = '~',
    val endDelimiter: Char = ']',
) {
    val segment: String = message.length
        .apply { check(this < 99) { "Message characters length should be between 0 and 99" } }
        .toString().padStart(2, '0')

    val checksum: String = medDelimiter
        .toInt()
        .times(3)
        .plus(segment.toCharArray().sumBy { it.toInt() })
        .plus(type.name.toCharArray().sumBy { it.toInt() })
        .plus(message
            .takeUnless { it.isEmpty() }
            ?.toCharArray()
            ?.sumBy { it.toInt() }
            ?: 0)
        .toString(16)
        .takeLast(2)
        .padStart(2, '0')

    override fun toString(): String = staDelimiter
        .plus(type.name)
        .plus(medDelimiter)
        .plus(segment)
        .plus(medDelimiter)
        .plus(message)
        .plus(medDelimiter)
        .plus(checksum)
        .plus(endDelimiter)
}
package dev.andrewbailey.diff

object DiffGenerator {

    fun <T> generateDiff(
        original: List<T>,
        updated: List<T>
    ): List<DiffOperation<T>> {
        return emptyList()
    }

}

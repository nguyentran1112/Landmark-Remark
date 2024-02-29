package tcnguyen.app.landmarkremark.data.model

data class LocationNote(
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var note: String = "",
    var owner: String = ""
) {
    fun matchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            "$owner$note",
            "$owner $note",
            "${owner.first()} ${note.first()}"
        )
        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}

package tcnguyen.app.landmarkremark.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import tcnguyen.app.landmarkremark.data.model.LocationNote
class LocationNoteRepository {
    private val db = FirebaseFirestore.getInstance()
    fun loadLocationNotes(
        flowDataNotes: MutableStateFlow<MutableList<LocationNote>>
    ) {
        var dataSets = mutableListOf<LocationNote>()
        db.collection("LocationNote").whereNotEqualTo("owner", "").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val note = document.toObject(LocationNote::class.java)
                    dataSets += note
                }
                flowDataNotes.value = (dataSets)
            }
            .addOnFailureListener {
            }
        dataSets
    }

    fun addLocationNode(locationNote: LocationNote) {
        val myUuid = UUID.randomUUID().toString()
        db.collection("LocationNote").document(myUuid).get().addOnSuccessListener {
            if (it.exists()) {
                db.collection("LocationNote").document(UUID.randomUUID().toString())
                    .set(locationNote)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener {
                    }
            } else {
                db.collection("LocationNote").document(myUuid).set(locationNote)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener {
                    }
            }
        }
    }
}

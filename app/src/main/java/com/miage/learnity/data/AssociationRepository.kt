import com.google.firebase.firestore.FirebaseFirestore
import com.miage.learnity.data.Association

class AssociationRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getAssociations(onSuccess: (List<Association>) -> Unit) {
        db.collection("associations")
            .get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { doc ->
                    // On extrait les 3 textes de Firebase
                    Association(
                        name = doc.getString("name") ?: "",
                        websiteUrl = doc.getString("websiteUrl") ?: "",
                        logoName = doc.getString("logoName") ?: ""
                    )
                }
                onSuccess(list)
            }
    }
}
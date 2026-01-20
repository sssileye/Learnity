package com.miage.learnity.data

import com.google.firebase.firestore.FirebaseFirestore

class AssociationRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getAssociations(onSuccess: (List<Association>) -> Unit) {
        db.collection("associations")
            .get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { doc ->
                    Association(
                        name = doc.getString("name") ?: "",
                        websiteUrl = doc.getString("websiteUrl") ?: "",
                        description = doc.getString("description") ?: "",
                        logoName = doc.getString("logoName") ?: ""
                    )
                }
                onSuccess(list)
            }
    }
}
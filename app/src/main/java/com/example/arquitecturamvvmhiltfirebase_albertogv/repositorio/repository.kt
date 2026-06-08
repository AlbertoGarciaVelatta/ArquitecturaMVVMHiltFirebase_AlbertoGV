package com.example.arquitecturamvvmhiltfirebase_albertogv.repositorio


import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.arquitecturamvvmhiltfirebase_albertogv.Datos.Incidencia
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidenciaRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val collection = firestore.collection("incidencias")

    /**
     * Flujo en tiempo real de todas las incidencias.
     * Se actualiza automáticamente cuando Firestore cambia.
     */
    fun getIncidencias(): Flow<List<Incidencia>> = callbackFlow {
        val listener = collection
            .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Incidencia::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    /** Guarda una nueva incidencia. Devuelve el ID generado por Firestore. */
    suspend fun addIncidencia(incidencia: Incidencia): String {
        val ref = collection.add(incidencia).await()
        return ref.id
    }

    /** Actualiza una incidencia existente por su ID. */
    suspend fun updateIncidencia(incidencia: Incidencia) {
        collection.document(incidencia.id).set(incidencia).await()
    }

    /** Elimina una incidencia por su ID. */
    suspend fun deleteIncidencia(id: String) {
        collection.document(id).delete().await()
    }

    /**
     * Sube una foto a Firebase Storage y devuelve la URL pública.
     * Úsala antes de crear/actualizar la incidencia para obtener fotoUrl.
     */
    suspend fun uploadFoto(uri: Uri): String {
        val fileName = "incidencias/${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(fileName)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
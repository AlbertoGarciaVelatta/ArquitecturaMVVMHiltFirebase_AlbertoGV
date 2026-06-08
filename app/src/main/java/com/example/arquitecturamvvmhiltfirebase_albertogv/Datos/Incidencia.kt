package com.example.arquitecturamvvmhiltfirebase_albertogv.Datos


import com.google.firebase.Timestamp

data class Incidencia(
    var id: String = "",
    val nombre: String = "",
    val prioridad: String = "baja",        // baja | media | alta
    val urgencia: Int = 1,                 // 1 | 2 | 3
    val activo: String = "PC1",            // PC1 | PC2 | PC3 | PC4
    val estado: String = "Abierta",        // Abierta | Resuelta | Rechazada
    val responsable: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val descripcion: String = "",
    val fotoUrl: String = ""
) {
    // Constructor vacío requerido por Firestore
    constructor() : this(
        id = "", nombre = "", prioridad = "baja", urgencia = 1,
        activo = "PC1", estado = "Abierta", responsable = "",
        fechaCreacion = Timestamp.now(), descripcion = "", fotoUrl = ""
    )
}
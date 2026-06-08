package com.example.arquitecturamvvmhiltfirebase_albertogv.recyclerview

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.arquitecturamvvmhiltfirebase_albertogv.databinding.ItemIncidenciaBinding
import com.example.arquitecturamvvmhiltfirebase_albertogv.Datos.Incidencia
import java.text.SimpleDateFormat
import java.util.Locale

class IncidenciaAdapter(
    private val onEditClick: (Incidencia) -> Unit,
    private val onDeleteClick: (Incidencia) -> Unit
) : ListAdapter<Incidencia, IncidenciaAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIncidenciaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemIncidenciaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(incidencia: Incidencia) {
            with(binding) {
                tvNombre.text = incidencia.nombre
                tvPrioridad.text = "Prioridad: ${incidencia.prioridad}"
                tvUrgencia.text = "Urgencia: ${incidencia.urgencia}"
                tvActivo.text = "Activo: ${incidencia.activo}"
                tvResponsable.text = "Responsable: ${incidencia.responsable}"
                tvDescripcion.text = incidencia.descripcion

                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                tvFecha.text = sdf.format(incidencia.fechaCreacion.toDate())

                // Badge de estado con color según valor
                chipEstado.text = incidencia.estado
                val color = when (incidencia.estado) {
                    "Abierta"   -> Color.parseColor("#E53935")  // rojo
                    "Resuelta"  -> Color.parseColor("#43A047")  // verde
                    "Rechazada" -> Color.parseColor("#FB8C00")  // naranja
                    else        -> Color.parseColor("#1976D2")  // azul
                }
                chipEstado.background.setTint(color)

                // Cargar foto con Glide
                if (incidencia.fotoUrl.isNotEmpty()) {
                    Glide.with(root.context)
                        .load(incidencia.fotoUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .centerCrop()
                        .into(ivFoto)
                }

                root.setOnClickListener { onEditClick(incidencia) }
                btnEliminar.setOnClickListener { onDeleteClick(incidencia) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Incidencia>() {
        override fun areItemsTheSame(old: Incidencia, new: Incidencia) = old.id == new.id
        override fun areContentsTheSame(old: Incidencia, new: Incidencia) = old == new
    }
}

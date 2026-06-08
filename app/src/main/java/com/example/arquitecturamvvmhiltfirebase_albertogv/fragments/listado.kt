package com.example.arquitecturamvvmhiltfirebase_albertogv.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.arquitecturamvvmhiltfirebase_albertogv.databinding.FragmentRegistroIncidenciaBinding
import com.example.arquitecturamvvmhiltfirebase_albertogv.databinding.FragmentListadoIncidenciasBinding
import com.example.arquitecturamvvmhiltfirebase_albertogv.recyclerview.IncidenciaAdapter
import com.example.arquitecturamvvmhiltfirebase_albertogv.viewmodel.IncidenciaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListadoIncidenciasFragment : Fragment() {

    private var _binding: FragmentListadoIncidenciasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IncidenciaViewModel by activityViewModels()

    private val adapter = IncidenciaAdapter(
        onEditClick  = { incidencia ->
            // Navegar al formulario pasando la incidencia para editar
            val action = ListadoIncidenciasFragmentDirections
                .actionListadoToRegistro(incidenciaId = incidencia.id)
            findNavController().navigate(action)
        },
        onDeleteClick = { incidencia ->
            viewModel.eliminarIncidencia(incidencia.id)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListadoIncidenciasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = adapter

        // FAB para crear nueva incidencia
        binding.fabNuevaIncidencia.setOnClickListener {
            val action = ListadoIncidenciasFragmentDirections
                .actionListadoToRegistro(incidenciaId = "")
            findNavController().navigate(action)
        }

        // Observar lista
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.incidencias.collect { lista ->
                    adapter.submitList(lista)
                    // Mostrar mensaje vacío si no hay incidencias
                    if (lista.isEmpty()) {
                        binding.tvVacia.visibility  = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.tvVacia.visibility  = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
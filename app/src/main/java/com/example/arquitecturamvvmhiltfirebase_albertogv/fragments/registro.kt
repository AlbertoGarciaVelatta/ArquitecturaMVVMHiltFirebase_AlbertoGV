package com.example.arquitecturamvvmhiltfirebase_albertogv.fragments


import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.arquitecturamvvmhiltfirebase_albertogv.databinding.FragmentRegistroIncidenciaBinding
import com.example.arquitecturamvvmhiltfirebase_albertogv.Datos.Incidencia
import com.example.arquitecturamvvmhiltfirebase_albertogv.viewmodel.IncidenciaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp


@AndroidEntryPoint
class RegistroIncidenciaFragment : Fragment() {

    private var _binding: FragmentRegistroIncidenciaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IncidenciaViewModel by activityViewModels()
    private val args: RegistroIncidenciaFragmentArgs by navArgs()

    private var fotoUri: Uri? = null
    private var incidenciaEdicion: Incidencia? = null

    // Selector de imagen de galería
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                fotoUri = it
                Glide.with(this).load(it).centerCrop().into(binding.ivFotoPreview)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroIncidenciaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarSpinners()
        configurarListeners()
        cargarDatosEdicion()
        observarEstado()
    }

    private fun configurarSpinners() {
        val prioridades = listOf("baja", "media", "alta")
        val urgencias   = listOf("1", "2", "3")
        val activos     = listOf("PC1", "PC2", "PC3", "PC4")
        val estados     = listOf("Abierta", "Resuelta", "Rechazada")

        binding.spinnerPrioridad.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, prioridades
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerUrgencia.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, urgencias
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerActivo.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, activos
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerEstado.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, estados
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun configurarListeners() {
        binding.btnSeleccionarFoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnGuardar.setOnClickListener {
            guardarIncidencia()
        }
    }

    private fun cargarDatosEdicion() {
        val id = args.incidenciaId
        if (id.isEmpty()) return  // es nueva

        // Buscar la incidencia en el StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            val incidencia = viewModel.incidencias.value.find { it.id == id } ?: return@launch
            incidenciaEdicion = incidencia

            with(binding) {
                etNombre.setText(incidencia.nombre)
                etResponsable.setText(incidencia.responsable)
                etDescripcion.setText(incidencia.descripcion)

                spinnerPrioridad.setSelection(listOf("baja","media","alta").indexOf(incidencia.prioridad))
                spinnerUrgencia.setSelection(incidencia.urgencia - 1)
                spinnerActivo.setSelection(listOf("PC1","PC2","PC3","PC4").indexOf(incidencia.activo))
                spinnerEstado.setSelection(listOf("Abierta","Resuelta","Rechazada").indexOf(incidencia.estado))

                if (incidencia.fotoUrl.isNotEmpty()) {
                    Glide.with(requireContext()).load(incidencia.fotoUrl).centerCrop().into(ivFotoPreview)
                }
            }
        }
    }

    private fun guardarIncidencia() {
        val nombre      = binding.etNombre.text.toString().trim()
        val responsable = binding.etResponsable.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.etNombre.error = "El nombre es obligatorio"
            return
        }

        val incidencia = Incidencia(
            id            = incidenciaEdicion?.id ?: "",
            nombre        = nombre,
            prioridad     = binding.spinnerPrioridad.selectedItem.toString(),
            urgencia      = binding.spinnerUrgencia.selectedItem.toString().toInt(),
            activo        = binding.spinnerActivo.selectedItem.toString(),
            estado        = binding.spinnerEstado.selectedItem.toString(),
            responsable   = responsable,
            fechaCreacion = incidenciaEdicion?.fechaCreacion ?: Timestamp.now(),
            descripcion   = descripcion,
            fotoUrl       = incidenciaEdicion?.fotoUrl ?: ""
        )

        if (incidenciaEdicion != null) {
            viewModel.actualizarIncidencia(incidencia, fotoUri)
        } else {
            viewModel.agregarIncidencia(incidencia, fotoUri)
        }
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is IncidenciaViewModel.UiState.Loading -> {
                            binding.btnGuardar.isEnabled = false
                        }
                        is IncidenciaViewModel.UiState.Success -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetUiState()
                            findNavController().popBackStack()
                        }
                        is IncidenciaViewModel.UiState.Error -> {
                            binding.btnGuardar.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetUiState()
                        }
                        else -> { binding.btnGuardar.isEnabled = true }
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
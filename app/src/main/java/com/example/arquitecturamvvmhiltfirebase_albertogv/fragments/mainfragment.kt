package com.example.arquitecturamvvmhiltfirebase_albertogv.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.arquitecturamvvmhiltfirebase_albertogv.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnVerListado).setOnClickListener {
            findNavController().navigate(R.id.action_main_to_listado)
        }

        view.findViewById<Button>(R.id.btnNuevaIncidencia).setOnClickListener {
            findNavController().navigate(R.id.action_main_to_registro)
        }
    }
}
package uz.dev_abubakir_khakimov.product_controller.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.hawk.Hawk
import uz.dev_abubakir_khakimov.product_controller.R
import uz.dev_abubakir_khakimov.product_controller.activities.MainActivity
import uz.dev_abubakir_khakimov.product_controller.databinding.FragmentChangeLanguageBinding

class ChangeLanguageFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentChangeLanguageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChangeLanguageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.english.setOnClickListener {
            setLanguage("en")
        }

        binding.russian.setOnClickListener {
            setLanguage("ru")
        }

        binding.uzbek.setOnClickListener {
            setLanguage("uz")
        }

    }

    private fun setLanguage(lan: String){
        Hawk.put("pref_lang", lan)

        startActivity(Intent(requireActivity(), MainActivity::class.java))
        requireActivity().finish()
    }

}
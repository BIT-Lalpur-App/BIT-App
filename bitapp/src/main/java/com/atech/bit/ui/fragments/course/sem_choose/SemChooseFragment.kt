package com.atech.bit.ui.fragments.course.sem_choose

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atech.bit.NavGraphDirections
import com.atech.bit.R
import com.atech.bit.databinding.FragmentSemChooseBinding
import com.atech.bit.ui.activity.main_activity.viewmodels.PreferenceManagerViewModel
import com.atech.bit.ui.custom_views.DividerItemDecorationNoLast
import com.atech.bit.utils.addMenuHost
import com.atech.core.data.room.syllabus.SyllabusModel
import com.atech.core.utils.KEY_TOGGLE_SYLLABUS_SOURCE
import com.atech.core.utils.openCustomChromeTab
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SemChooseFragment : Fragment(R.layout.fragment_sem_choose) {

    private val viewModel: ChooseSemViewModel by viewModels()
    private val prefManagerViewModel: PreferenceManagerViewModel by activityViewModels()

    private val binding: FragmentSemChooseBinding by viewBinding()
    private lateinit var courseTheoryAdapter: SubjectAdapter
    private lateinit var courseLabAdapter: SubjectAdapter
    private lateinit var coursePeAdapter: SubjectAdapter

    @Inject
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            duration = resources.getInteger(R.integer.duration_medium).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(Color.TRANSPARENT)
        }
    }

    @Inject
    lateinit var db: FirebaseFirestore
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        restoreScroll(binding)
        binding.root.transitionName = viewModel.request

        courseTheoryAdapter = SubjectAdapter { syllabusModel, view ->
            syllabusClick(syllabusModel, view)
        }
        courseTheoryAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        courseLabAdapter = SubjectAdapter { syllabusModel, view ->
            syllabusClick(syllabusModel, view)
        }
        courseLabAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        coursePeAdapter = SubjectAdapter { syllabusModel, view ->
            syllabusClick(syllabusModel, view)
        }
        coursePeAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.apply {
            semChoseExt.apply {
                showTheory.apply {
                    adapter = courseTheoryAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(
                        DividerItemDecorationNoLast(
                            requireContext(),
                            LinearLayoutManager.VERTICAL
                        ).apply {
                            setDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.divider
                                )
                            )
                        }
                    )
                }
                showLab.apply {
                    adapter = courseLabAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(
                        DividerItemDecorationNoLast(
                            requireContext(),
                            LinearLayoutManager.VERTICAL
                        ).apply {
                            setDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.divider
                                )
                            )
                        }
                    )
                }
                showPe.apply {
                    adapter = coursePeAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(
                        DividerItemDecorationNoLast(
                            requireContext(),
                            LinearLayoutManager.VERTICAL
                        ).apply {
                            setDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.divider
                                )
                            )
                        }
                    )
                }
            }
        }
        viewModel.theory.observe(viewLifecycleOwner) {
            courseTheoryAdapter.submitList(it)
        }
        viewModel.lab.observe(viewLifecycleOwner) {
            binding.semChoseExt.showLab.isVisible = it.isNotEmpty()
            binding.semChoseExt.textView7.isVisible = it.isNotEmpty()
            courseLabAdapter.submitList(it)
        }
        viewModel.pe.observe(viewLifecycleOwner) {
            binding.semChoseExt.showPe.isVisible = it.isNotEmpty()
            binding.semChoseExt.textView8.isVisible = it.isNotEmpty()
            coursePeAdapter.submitList(it)
        }
        prefManagerViewModel.preferencesFlow.observe(viewLifecycleOwner) {
            viewModel.sem.value = "${viewModel.request}${it.semSyllabus}"
            buttonColorChange(it.semSyllabus, binding)
        }
        buttonClick()
        setUpMenu()
        switchClick()
        setSource()
        getOnlineSyllabus()
    }

    private fun getOnlineSyllabus() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val d = viewModel.getSyllabus()
                Log.d("XXX", "getOnlineSyllabus: ${d.semesters.size}")
            } catch (e: Exception) {
                Log.d("XXX", "getOnlineSyllabus: ${e.message}")
            }

        }
    }

    private fun setSource() {
        val source = pref.getBoolean(KEY_TOGGLE_SYLLABUS_SOURCE, false)
        binding.switchOldNew.isChecked = source
        setText(source)
        layoutChanges(source)
    }

    private fun switchClick() = binding.switchOldNew.apply {
        setOnCheckedChangeListener { _, isChecked ->
            saveSource(isChecked)
            setText(isChecked)
            layoutChanges(isChecked)
        }
    }

    private fun setText(isEnable: Boolean) {
        binding.switchOldNew.text =
            if (isEnable) resources.getString(R.string.switch_to_old)
            else resources.getString(R.string.switch_to_new)
    }

    private fun saveSource(isEnable: Boolean) {
        pref.edit()
            .putBoolean(
                KEY_TOGGLE_SYLLABUS_SOURCE,
                isEnable
            ).apply()
    }

    private fun layoutChanges(isEnable: Boolean) = binding.apply {
        semChoseOnlineExt.root.isVisible = isEnable
        semChoseExt.root.isVisible = !isEnable
    }


    private fun setUpMenu() {
        addMenuHost(R.menu.menu_sem_choose) { item ->
            when (item.itemId) {
                R.id.menu_download_syllabus -> {
                    getSyllabus(viewModel.request?.uppercase() ?: "BCA")
                    true
                }
                else -> false
            }
        }
    }


    private fun buttonClick() {
        binding.apply {
            bt1.setOnClickListener { prefManagerViewModel.updateSemSyllabus("1") }
            bt2.setOnClickListener { prefManagerViewModel.updateSemSyllabus("2") }
            bt3.setOnClickListener { prefManagerViewModel.updateSemSyllabus("3") }
            bt4.setOnClickListener { prefManagerViewModel.updateSemSyllabus("4") }
            bt5.setOnClickListener { prefManagerViewModel.updateSemSyllabus("5") }
            bt6.setOnClickListener { prefManagerViewModel.updateSemSyllabus("6") }
        }
    }

    private fun syllabusClick(syllabusModel: SyllabusModel, view: View) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.duration_medium).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.duration_small).toLong()
        }

        try {
            val extras = FragmentNavigatorExtras(view to syllabusModel.openCode)
            val action =
                NavGraphDirections.actionGlobalSubjectHandlerFragment(
                    syllabusModel
                )
            findNavController().navigate(action, extras)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Press one item at a time !!", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun getSyllabus(course: String) {
        db.collection("Utils")
            .document("syllabus_download")
            .addSnapshotListener { value, _ ->
                val link = value?.getString(course)
                link?.let {
                    requireActivity().openCustomChromeTab(it)
                }
            }
    }

    /**
     * @author Ayaan
     * @since 4.0.3
     */
    private fun restoreScroll(binding: FragmentSemChooseBinding) {
        try {
            if (viewModel.chooseSemNestedViewPosition != null)
                binding.semChoseExt.nestedViewSyllabus.post {
                    binding.semChoseExt.nestedViewSyllabus.scrollTo(
                        0,
                        viewModel.chooseSemNestedViewPosition!!
                    )
                }
        } catch (e: Exception) {
            Log.e("Error", e.message!!)
        }
    }

    override fun onStop() {
        try {
            super.onStop()
            viewModel.chooseSemNestedViewPosition = binding.semChoseExt.nestedViewSyllabus.scrollY
        } catch (e: Exception) {
            Log.e("Error", e.message!!)
        }
    }

    private fun buttonColorChange(request: String, binding: FragmentSemChooseBinding) {
        when (request) {
            "1" -> binding.toggleChip.check(R.id.bt1)
            "2" -> binding.toggleChip.check(R.id.bt2)
            "3" -> binding.toggleChip.check(R.id.bt3)
            "4" -> binding.toggleChip.check(R.id.bt4)
            "5" -> binding.toggleChip.check(R.id.bt5)
            "6" -> binding.toggleChip.check(R.id.bt6)
            else -> Log.d("Error", "buttonColorChange: Error")
        }
    }
}
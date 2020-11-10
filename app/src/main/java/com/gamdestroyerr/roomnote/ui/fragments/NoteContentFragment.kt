package com.gamdestroyerr.roomnote.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import com.gamdestroyerr.roomnote.R
import com.gamdestroyerr.roomnote.model.Note
import com.gamdestroyerr.roomnote.ui.activity.NoteActivity
import com.gamdestroyerr.roomnote.utils.*
import com.gamdestroyerr.roomnote.viewmodel.NoteActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.android.synthetic.main.bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.fragment_note_content.*
import kotlinx.android.synthetic.main.fragment_note_content.view.*
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NoteContentFragment : Fragment(R.layout.fragment_note_content) {

    private lateinit var navController: NavController
    private lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var result: String
    private var note: Note? = null
    private val currentDate = SimpleDateFormat.getDateInstance().format(Date())
    private var color = -1
    private val REQUEST_IMAGE_CAPTURE = 100
    private val SELECT_IMAGE_FROM_STORAGE = 101
    private lateinit var photoFile: File
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: NoteContentFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            scrimColor = Color.TRANSPARENT
            duration = 300L
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
        addSharedElementListener()
    }

    @SuppressLint("InflateParams", "QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Sets the unique transition name for the layout that is
         being inflated using SharedElementEnterTransition class */
        ViewCompat.setTransitionName(
            noteContentFragmentParent,
            "recyclerView_${args.note?.id}"
        )



        navController = Navigation.findNavController(view)
        val activity = activity as NoteActivity
        noteActivityViewModel = activity.noteActivityViewModel

        registerForContextMenu(noteImage)

        view.backBtn.setOnClickListener {
            requireView().hideKeyboard()
            saveNoteAndGoBack()
        }

        try {
            view.noteContentTxtView.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    view.bottomBar.visibility = View.VISIBLE
                    noteContentTxtView.setStylesBar(styleBar)
                } else view.bottomBar.visibility = View.GONE
            }
        } catch (e: Throwable) {
            Log.d("TAG", e.stackTraceToString())
        }

        view.noteOptionsMenu.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme,
            )
            val bottomSheetView: View = layoutInflater.inflate(
                R.layout.bottom_sheet_dialog,
                null,
            )

            with(bottomSheetDialog) {
                setContentView(bottomSheetView)
                show()
            }

            bottomSheetView.colorPicker.setSelectedColor(color)
            bottomSheetView.colorPicker.setOnColorSelectedListener { value ->
                color = value
                noteContentFragmentParent.apply {
                    setBackgroundColor(color)
                    activity.window.statusBarColor = color
                    toolbarFragmentNoteContent.setBackgroundColor(color)
                    bottomBar.setBackgroundColor(color)
                }
            }
            bottomSheetView.post {
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            bottomSheetView.addImage.setOnClickListener {
                val permission = ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA,
                )
                if (permission != PackageManager.PERMISSION_GRANTED) {

                    val permissionArray = arrayOf(Manifest.permission.CAMERA)
                    ActivityCompat.requestPermissions(
                        activity,
                        permissionArray,
                        REQUEST_IMAGE_CAPTURE
                    )
                    ActivityCompat.OnRequestPermissionsResultCallback { requestCode,
                                                                        permissions,
                                                                        grantResults ->
                        when (requestCode) {
                            REQUEST_IMAGE_CAPTURE -> {
                                if (permissions[0] == Manifest.permission.CAMERA &&
                                    grantResults.isNotEmpty()
                                ) {
                                    Log.d("tag", "this function is called")
                                    takePictureIntent()
                                }
                            }
                        }
                    }
                }
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    takePictureIntent()
                    bottomSheetDialog.dismiss()
                }
            }
            @Suppress("DEPRECATION")
            bottomSheetView.selectImage.setOnClickListener {
                Intent(Intent.ACTION_GET_CONTENT).also { chooseIntent ->
                    chooseIntent.type = "image/*"
                    chooseIntent.resolveActivity(activity.packageManager!!.also {
                        startActivityForResult(chooseIntent, SELECT_IMAGE_FROM_STORAGE)
                    })
                }
                bottomSheetDialog.dismiss()
            }
        }

        //opens with existing note item
        args.let {
            note = it.note
            titleTxtView.setText(note?.title)
            if (note?.content == null) noteContentTxtView.text = note?.content
            else noteContentTxtView.renderMD(note?.content!!)

            if (note == null) {
                lastEdited.text =
                    getString(R.string.edited_on, SimpleDateFormat.getDateInstance().format(Date()))
                setImage(noteActivityViewModel.setImagePath())
            } else {
                lastEdited.text = getString(R.string.edited_on, note?.date)
                color = note!!.color

                if (noteActivityViewModel.setImagePath() != null)
                    setImage(noteActivityViewModel.setImagePath())
                else noteActivityViewModel.saveImagePath(note?.imagePath)

                noteContentFragmentParent.apply {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(10)
                        setBackgroundColor(color)
                        noteImage.visibility = View.VISIBLE
                    }
                }
                toolbarFragmentNoteContent.setBackgroundColor(color)
                bottomBar.setBackgroundColor(color)
                activity.window?.statusBarColor = note?.color!!
            }
        }

        activity.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    saveNoteAndGoBack()
                }
            })
    }

    private fun addSharedElementListener() {
        (sharedElementEnterTransition as Transition).addListener(
            object : TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    super.onTransitionEnd(transition)
                    if (args.note?.imagePath != null) {
                        noteImage.visibility = View.VISIBLE
                        val uri = Uri.fromFile(File(args.note?.imagePath!!))
                        job.launch {
                            requireContext().asyncImageLoader(uri, noteImage, this)
                        }
                    } else noteImage.visibility = View.GONE
                }
            }
        )
    }

    /**
     * This Method handles the save and update operation.
     *
     * Checks if the note arg is null
     * It will save the note with a unique id.
     *
     * If note arg has data it will update
     * note to save any changes. */
    private fun saveNoteAndGoBack() {

        if (titleTxtView.text.toString().isEmpty() &&
            noteContentTxtView.text.toString().isEmpty()
        ) {
            result = "Empty Note Discarded"
            setFragmentResult("key", bundleOf("bundleKey" to result))
            navController.navigate(
                NoteContentFragmentDirections
                    .actionNoteContentFragmentToNoteFragment()
            )

        } else {

            when (note) {
                null -> {
                    noteActivityViewModel.saveNote(
                        Note(
                            0,
                            titleTxtView.text.toString(),
                            noteContentTxtView.getMD(),
                            currentDate,
                            color,
                            noteActivityViewModel.setImagePath(),
                        )
                    )
                    result = "Note Saved"
                    setFragmentResult(
                        "key",
                        bundleOf("bundleKey" to result)
                    )
                    navController.navigate(
                        NoteContentFragmentDirections
                            .actionNoteContentFragmentToNoteFragment()
                    )

                }
                else -> {
                    updateNote()
                    navController.navigate(R.id.action_noteContentFragment_to_noteFragment)
                }
            }
        }
    }

    private fun updateNote() {
        if (note != null) {
            noteActivityViewModel.updateNote(
                Note(
                    note!!.id,
                    titleTxtView.text.toString(),
                    noteContentTxtView.getMD(),
                    currentDate,
                    color,
                    noteActivityViewModel.setImagePath(),
                )
            )
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    @Suppress("DEPRECATION")
    private fun takePictureIntent() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { captureIntent ->
            photoFile = getPhotoFile(requireActivity())
            val fileProvider = FileProvider.getUriForFile(
                requireContext(),
                getString(R.string.fileAuthority),
                photoFile
            )
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            captureIntent.resolveActivity(activity?.packageManager!!.also {
                startActivityForResult(captureIntent, REQUEST_IMAGE_CAPTURE)
            })
        }
    }

    private fun menuIconWithText(r: Drawable, title: String): CharSequence? {
        r.setBounds(0, 0, r.intrinsicWidth, r.intrinsicHeight)
        val sb = SpannableString("   $title")
        val imageSpan = ImageSpan(r, ImageSpan.ALIGN_BOTTOM)
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sb
    }

    /**
     * This method gets a filePath as a string and converts it into URI
     * then passes that URI and the target imageView to and extension function
     * loadImage that will the image to its given target*/
    private fun setImage(filePath: String?) {
        if (filePath != null) {
            val uri = Uri.fromFile(File(filePath))
            noteImage.visibility = View.VISIBLE
            try {
                job.launch {
                    requireContext().asyncImageLoader(uri, noteImage, this)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                noteImage.visibility = View.GONE
            }
        } else noteImage.visibility = View.GONE
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            noteActivityViewModel.saveImagePath(photoFile.absolutePath)
            setImage(photoFile.absolutePath)
        }
        if (requestCode == SELECT_IMAGE_FROM_STORAGE && resultCode == RESULT_OK) {
            val uri = data?.data
            Log.d("Tag", uri.toString())
            if (uri != null) {
                val selectedImagePath = getImageUrlWithAuthority(
                    requireContext(),
                    uri,
                    requireActivity()
                )
                noteActivityViewModel.saveImagePath(selectedImagePath)
                setImage(selectedImagePath)
            }

            noteImage.visibility = View.VISIBLE
            job.launch { requireContext().asyncImageLoader(uri, noteImage, this) }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(
            0,
            1,
            1,
            menuIconWithText(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_round_delete_24
                )!!, getString(R.string.delete)
            )
        )
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                if (note?.imagePath != null) {
                    val toDelete = File(note?.imagePath!!)
                    if (toDelete.exists()) {
                        toDelete.delete()
                    }
                }
                if (noteActivityViewModel.setImagePath() != null) {
                    val toDelete = File(noteActivityViewModel.setImagePath()!!)
                    if (toDelete.exists()) {
                        toDelete.delete()
                    }
                    noteActivityViewModel.saveImagePath(null)
                }

                noteImage.visibility = View.GONE
                updateNote()
                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (job.isActive) {
            job.cancel()
        }
    }
}
package com.waesh.favdish.view.activities

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.waesh.favdish.R
import com.waesh.favdish.application.FavDishApplication
import com.waesh.favdish.databinding.ActivityAddUpdateDishBinding
import com.waesh.favdish.databinding.DialogCustomLlistBinding
import com.waesh.favdish.databinding.DialogImageSelectionBinding
import com.waesh.favdish.model.entities.FavDish
import com.waesh.favdish.util.Constants
import com.waesh.favdish.util.makeToast
import com.waesh.favdish.view.adapters.CustomItemListAdapter
import com.waesh.favdish.view.adapters.ISelectionAction
import com.waesh.favdish.viewmodel.FavDishViewModel
import com.waesh.favdish.viewmodel.FavDishViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class AddUpdateDishActivity : AppCompatActivity(), ISelectionAction {
    private var imagePath: String = ""
    private var _binding: ActivityAddUpdateDishBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private lateinit var mDialog: Dialog
    private var editDishDetail: FavDish? = null

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    private val camTakePictureResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                imageUri?.let { uri ->
                    //val ss = uri.toString()   //uri to string
                    //val gg = Uri.parse(ss)    //uri from string
                    imagePath = uri.toString()
                    Glide.with(this)
                        .load(uri)
                        .centerCrop()
                        .into(binding.ivDishImage)
                }
            }
        }


    private val galleryUriContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            Glide.with(this)
                .load(it)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("kkkCat", "Error: Load")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let {
                            val bitmap: Bitmap = resource.toBitmap()
                            imagePath = saveImageToInternalStorage(bitmap).toString()
                        }
                        return false
                    }

                })
                .into(binding.ivDishImage)
        }

    companion object {
        private const val IMAGE_DIRECTORY = "camera_photo"
        private const val appAuthority: String = "com.waesh.favdish.fileProvider"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivAddDishImage.setOnClickListener {
            customImageSelectionDialog()
        }

        binding.etType.setOnClickListener { etType ->
            val list = resources.getStringArray(R.array.dishType).toList()
            createItemDialog(list, resources.getString(R.string.title_select_type), etType.id)
        }

        binding.etCategory.setOnClickListener { etCategory ->
            val list = resources.getStringArray(R.array.dishCategory).toList()
            createItemDialog(
                list,
                resources.getString(R.string.title_select_category),
                etCategory.id
            )
        }

        binding.etCookingTime.setOnClickListener { etCookingTime ->
            createItemDialog(
                resources.getStringArray(R.array.cookingTime).toList(),
                resources.getString(R.string.title_select_time),
                etCookingTime.id
            )
        }

        if (intent.hasExtra(Constants.DISH_DETAILS_EXTRA)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                editDishDetail = intent.getParcelableExtra(Constants.DISH_DETAILS_EXTRA, FavDish::class.java)
            else
                @Suppress("DEPRECATION")
                editDishDetail = intent.getParcelableExtra(Constants.DISH_DETAILS_EXTRA)
        }

        //Insert dish
        binding.apply {
            editDishDetail?.let {
                etTitle.setText(it.title)
                etType.setText(it.type)
                etCookingTime.setText(it.cookingTime)
                etCategory.setText(it.category)
                etIngredients.setText(it.ingredients)
                etDirectionToCook.setText(it.directionToCook)

                imagePath = it.image

                Glide.with(this@AddUpdateDishActivity)
                    .load(it.image)
                    .centerCrop()
                    .into(ivDishImage)

                btnAddDish.text = getString(R.string.btn_text_update)
            }

            btnAddDish.setOnClickListener {
                when {
                    imagePath.isEmpty() -> {
                        makeToast(this@AddUpdateDishActivity, "Select a dish image.")
                    }
                    etTitle.text.toString().isEmpty() -> {
                        makeToast(this@AddUpdateDishActivity, "Insert dish title.")
                    }
                    etType.text.toString().isEmpty() -> {
                        makeToast(this@AddUpdateDishActivity, "Select dish type.")
                    }
                    etCategory.text.toString().isEmpty() -> {
                        makeToast(this@AddUpdateDishActivity, "Select dish category.")
                    }
                    etIngredients.text.toString().isEmpty() -> {
                        makeToast(this@AddUpdateDishActivity, "insert dish ingredients.")
                    }
                    etCookingTime.text.toString().isEmpty() -> {
                        makeToast(this@AddUpdateDishActivity, "Select dish cooking time.")
                    }
                    etDirectionToCook.text.toString().isEmpty() -> {
                        makeToast(this@AddUpdateDishActivity, "Insert cooking direction.")
                    }
                    else -> {
                        val dish = FavDish(
                            imagePath,
                            Constants.IMAGE_SOURCE_LOCAL,
                            etTitle.text.toString().trim(),
                            etType.text.toString(),
                            etCategory.text.toString(),
                            etIngredients.text.toString().trim(),
                            etCookingTime.text.toString(),
                            etDirectionToCook.text.toString().trim(),
                            editDishDetail?.favoriteDish ?: false,
                            editDishDetail?.id ?: 0
                        )
                        if (editDishDetail != null) {
                            mFavDishViewModel.update(dish)
                            makeToast(this@AddUpdateDishActivity, "Dish has been updated.")
                        } else {
                            mFavDishViewModel.insertDish(dish)
                            makeToast(this@AddUpdateDishActivity, "New dish has been added.")
                        }
                        finish()
                    }
                }
            }
        }
    }

    /*private fun makeToast(message: String) {
        Toast.makeText(
            this@AddUpdateDishActivity,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }*/

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun createItemDialog(list: List<String>, title: String, viewId: Int) {

        mDialog = Dialog(this)
        val listDialogBinding = DialogCustomLlistBinding.inflate(layoutInflater)
        mDialog.setContentView(listDialogBinding.root)

        listDialogBinding.apply {
            rvList.layoutManager = LinearLayoutManager(this@AddUpdateDishActivity)
            rvList.adapter = CustomItemListAdapter(this@AddUpdateDishActivity, list, viewId)
            tvTitle.text = title
        }
        mDialog.show()
    }

    override fun onItemClick(itemText: String, position: Int, viewId: Int?): Boolean {
        binding.apply {
            when (viewId) {
                etType.id -> etType.setText(itemText)
                etCategory.id -> etCategory.setText(itemText)
                etCookingTime.id -> etCookingTime.setText(itemText)
            }
        }
        mDialog.dismiss()
        return true
    }

    private fun createImageUri(): Uri? {
        //create the image first
        val image = File(applicationContext.filesDir, "${UUID.randomUUID()}.jpg")
        //create and return Uri
        //val filePath: String = image.absolutePath
        return FileProvider.getUriForFile(this, appAuthority, image)
    }

    //Don't like this method
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        //create filepath
        //TODO create file using createImageUri
        val wrapper = ContextWrapper(application)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return FileProvider.getUriForFile(this, appAuthority, file)
    }

    private fun customImageSelectionDialog() {
        val dialog = Dialog(this)
        val dialogBinding = DialogImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.ivDialGallery.setOnClickListener {

            Dexter.withContext(this)
                .withPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            "Gallery permission granted.",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                        galleryUriContract.launch("image/*")
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            "Gallery permission denied.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissionRequest: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        showRationaleDialogForPermission()
                    }

                })
                .onSameThread()
                .check()

            dialog.dismiss()
        }

        dialogBinding.ivDialCamera.setOnClickListener {

            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    //Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            if (report.areAllPermissionsGranted()) {
                                imageUri = createImageUri()
                                imageUri?.let {
                                    dialog.dismiss()
                                    camTakePictureResult.launch(imageUri)
                                }
                            }
                        }

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationaleDialogForPermission()
                    }

                })
                .onSameThread()
                .check()

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRationaleDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("Permissions are not granted!\n Go to Settings to turn them on.")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val settingsIntent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    settingsIntent.data = uri
                    startActivity(settingsIntent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}
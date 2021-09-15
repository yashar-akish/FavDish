package com.company.favdish.view.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.company.favdish.R
import com.company.favdish.databinding.ActivityAddUpdateDishBinding
import com.company.favdish.databinding.DialogCustomImageSelectionBinding
import com.company.favdish.databinding.DialogCustomListBinding
import com.company.favdish.utils.Constants
import com.company.favdish.view.adapters.CustomListItemAdapter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import org.w3c.dom.Text
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val CAMERA = 1
        private const val GALLERY = 2

        private const val IMAGE_DIRECTORY = "FavDishImages"
    }

    private lateinit var mBinding: ActivityAddUpdateDishBinding
    private var mImagePath : String = ""

    private lateinit var mCustomListDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupActionBar()

        mBinding.ivAddDishImage.setOnClickListener(this)

        mBinding.etType.setOnClickListener(this)
        mBinding.etCategory.setOnClickListener(this)
        mBinding.etCookingTime.setOnClickListener(this)

        mBinding.btnAddDish.setOnClickListener(this)

    }

    /**
     * setting uo the toolbar
     */
    private fun setupActionBar() {
        setSupportActionBar(mBinding.toolbarAddDishActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //allow to have back button on toolbar
        // adding clickListener to back button:
        mBinding.toolbarAddDishActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * implement View.OnClickListener
     */
    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_add_dish_image -> {
                    // display the dialog
                    customImageSelectionDialog()
                    return
                }
                R.id.et_type -> {
                    customItemListDialog(
                        resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(), Constants.DISH_TYPE)
                    return
                }
                R.id.et_category -> {
                    customItemListDialog(
                        resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategories(), Constants.DISH_CATEGORY)
                    return
                }
                R.id.et_cooking_time -> {
                    customItemListDialog(
                        resources.getString(R.string.title_select_dish_cooking_time),
                        Constants.dishCookTime(), Constants.DISH_COOKING_TIME)
                    return
                }
                R.id.btn_add_dish -> {
                    val title = mBinding.etTitle.text.toString().trim { it <= ' ' }
                    val type = mBinding.etType.text.toString().trim { it <= ' ' }
                    val category = mBinding.etCategory.text.toString().trim { it <= ' ' }
                    val ingredients = mBinding.etIngredients.text.toString().trim { it <= ' ' }
                    val cookingTimeInMinutes = mBinding.etCookingTime.text.toString().trim { it <= ' ' }
                    val cookingDirection = mBinding.etDirectionToCook.text.toString().trim { it <= ' ' }

                    when {
                        TextUtils.isEmpty(mImagePath) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_image),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(title) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_title),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(type) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_type),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(ingredients) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_ingredients),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(cookingTimeInMinutes) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_time),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(cookingDirection) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                "All the entries are valid.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * displaying the dialog to select image
     */
    private fun customImageSelectionDialog() {
        val dialog = Dialog(this)
        val binding: DialogCustomImageSelectionBinding =
            DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {

            /**
             * implementing Dexter
             * https://github.com/Karumi/Dexter
             * asking multiple permissions
             */
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                //Manifest.permission.WRITE_EXTERNAL_STORAGE,   -> new apis does not need this line
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    /*surround with (?.let) to make sure the code will be executed
                    only when the report is not empty*/
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            /**
                             * getting access to camera
                             */
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent, CAMERA)
                        }
                    }
                    /*if (report!!.areAllPermissionsGranted()) {
                        Toast.makeText(this@AddUpdateDishActivity, "you have camera permission now.", Toast.LENGTH_SHORT).show()
                    } else {
                        showRationalDialogForPermissions()
                    }*/
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread().check()

            dialog.dismiss()
        }

        binding.tvGallery.setOnClickListener {

            Dexter.withContext(this)
                .withPermission(
                    // withPermissions to withPermission   (s)
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    //Manifest.permission.WRITE_EXTERNAL_STORAGE        ->  new apis does not need this line
                ).withListener(object :
                    PermissionListener {     // compare with the last version in github
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                        val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                        startActivityForResult(galleryIntent, GALLERY)
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            "You have Denied the storage permission to select the photo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }

                }).onSameThread().check()

            dialog.dismiss()
        }

        dialog.show()
    }


    /**
     * in case of refusing the permissions
     */
    private fun showRationalDialogForPermissions() {

        AlertDialog.Builder(this).setMessage(
            "You have turned off the permissions.\nto " +
                    "enable it go to the application settings "
        )
            .setPositiveButton("GO TO THE SETTINGS")
            { _, _ ->
                try {
                    /**
                     * going to the setting of application
                     */
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null) //application link
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    /**
     * getting the result from an activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA) {
                //setting the image of camera as dish image
                data?.extras?.let {
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap
                    //mBinding.ivDishImage.setImageBitmap(thumbnail)  <- because of using Glide remove it

                    //using Glide
                    Glide.with(this)
                        .load(thumbnail)
                        .centerCrop()
                        .into(mBinding.ivDishImage)
                    //saving from camera
                    mImagePath = saveImageToInternalStorage(thumbnail)
                    Log.i("imagePath", mImagePath)

                    // changing the vector image
                    mBinding.ivAddDishImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit))
                }
            }

            if (requestCode == GALLERY) {
                //setting the image from gallery as dish image
                data?.let {
                    val selectedPhotoUri = data.data
                    //mBinding.ivDishImage.setImageURI(selectedPhotoUri)       <- because of using Glide remove it

                    //using Glide
                    Glide.with(this)
                        .load(selectedPhotoUri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("TAG","Error loading image", e)
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
                                    val bitmap : Bitmap = resource.toBitmap()
                                    mImagePath = saveImageToInternalStorage(bitmap)

                                    Log.i("imagePath", mImagePath)
                                }
                                return false
                            }
                        })
                        .into(mBinding.ivDishImage)
                    // changing the vector image
                    mBinding.ivAddDishImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit))
                }
            }
        } else if (requestCode == Activity.RESULT_CANCELED) {
            Log.i("cancelled", "user cancelled")
        }
    }

    /**
     *saving image
     */
    private fun saveImageToInternalStorage(bitmap: Bitmap): String{

        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.absolutePath
    }

    /**
     * running the recyclerView on dialog
     */
    private fun customItemListDialog(title: String, itemList: List<String>, selection: String){

        mCustomListDialog = Dialog(this)
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)

        mCustomListDialog.setContentView(binding.root)
        binding.tvTitle.text = title

        binding.rvList.layoutManager = LinearLayoutManager(this)

        val adapter = CustomListItemAdapter(this, itemList, selection)
        binding.rvList.adapter = adapter

        mCustomListDialog.show()

    }

    /**
     * fill out the form base on selected dialog item
     */
    fun selectedListItem(item: String, selection: String) {

        when(selection) {
            Constants.DISH_TYPE -> {
                mCustomListDialog.dismiss()
                mBinding.etType.setText(item)
            }
            Constants.DISH_CATEGORY -> {
                mCustomListDialog.dismiss()
                mBinding.etCategory.setText(item)
            }
            else -> {
                mCustomListDialog.dismiss()
                mBinding.etCookingTime.setText(item)
            }
        }
    }
}
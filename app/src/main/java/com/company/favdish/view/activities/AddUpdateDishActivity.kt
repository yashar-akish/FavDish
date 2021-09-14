package com.company.favdish.view.activities

import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.Equalizer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.company.favdish.R
import com.company.favdish.databinding.ActivityAddUpdateDishBinding
import com.company.favdish.databinding.DialogCustomImageSelectionBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivityAddUpdateDishBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupActionBar()

        mBinding.ivAddDishImage.setOnClickListener(this)
    }

    /**
     * setting uo the toolbar
     */
    private fun setupActionBar(){
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
        if (v != null){
            when (v.id) {
                R.id.iv_add_dish_image ->{
                    // display the dialog
                    customImageSelectionDialog()
                    return
                }
            }
        }
    }

    /**
     * displaying the dialog to select image
     */
    private fun customImageSelectionDialog(){
        val dialog = Dialog(this)
        val binding: DialogCustomImageSelectionBinding = DialogCustomImageSelectionBinding.inflate(layoutInflater)
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
            ).withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    /*surround with (?.let) to make sure the code will be executed
                    only when the report is not empty*/
                    report?.let {
                        if (report.areAllPermissionsGranted()) {

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
                .withPermission(    // withPermissions to withPermission   (s)
                Manifest.permission.READ_EXTERNAL_STORAGE,
                //Manifest.permission.WRITE_EXTERNAL_STORAGE        ->  new apis does not need this line
            ).withListener(object : PermissionListener{     // compare with the last version in github
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    Toast.makeText(this@AddUpdateDishActivity,
                        "You have the gallery permission to select the photo",
                        Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(this@AddUpdateDishActivity,
                        "You have Denied the storage permission to select the photo",
                        Toast.LENGTH_SHORT).show()
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

        AlertDialog.Builder(this).setMessage("You have turned off the permissions.\nto " +
                "enable it go to the application settings ")
            .setPositiveButton("GO TO THE SETTINGS")
            {
                _,_ -> try {
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
}
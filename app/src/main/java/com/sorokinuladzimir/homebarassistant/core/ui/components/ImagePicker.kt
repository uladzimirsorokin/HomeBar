package com.sorokinuladzimir.homebarassistant.core.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class ImagePickerActions(
    val takePhoto: () -> Unit,
    val chooseFromLibrary: () -> Unit,
)

@Composable
fun rememberImagePicker(
    onPicked: (Uri, Boolean) -> Unit,
    createPhotoUri: () -> Uri?,
): ImagePickerActions {
    val pendingCameraUri = remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) onPicked(uri, false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) pendingCameraUri.value?.let { onPicked(it, true) }
    }

    return remember {
        ImagePickerActions(
            takePhoto = {
                createPhotoUri()?.let { uri ->
                    pendingCameraUri.value = uri
                    cameraLauncher.launch(uri)
                }
            },
            chooseFromLibrary = { galleryLauncher.launch("image/*") }
        )
    }
}

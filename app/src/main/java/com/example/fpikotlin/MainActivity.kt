package com.example.fpikotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fpikotlin.ui.theme.FPIKotlinTheme


var accentColor = Color.LightGray

class MainActivity : ComponentActivity() {

    var imageSaver = ImageSaver(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            setPageContent()
        }
    }

    @Composable
    fun IndeterminateCircularIndicator() {
        CircularProgressIndicator(
            modifier = Modifier
                .width(64.dp)
                .padding(top = 40.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        )
    }

    @Composable
    fun BitmapImage(imageBitmap: Bitmap?) {
        if (imageBitmap == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Image(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Upload an image",
                    modifier = Modifier
                        .rotate(-90F)
                        .size(80.dp)
                )
                Text(text = "Upload an Image to start")
            }
            return
        }
        Image(
            bitmap = imageBitmap.asImageBitmap(),
            contentDescription = "User uploaded image",
            modifier = Modifier.fillMaxHeight(0.85f)
        )
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            1 -> if (data != null) {
                imageSaver.onCreateFileActivityResult(
                    data.data,
                    contentResolver
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1024 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this.applicationContext,
                        "Woho, you have enabled notifications!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this.applicationContext,
                        "Ouch, this is gonna hurt without notifications",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun setPageContent() {
        val imageBitmap: MutableState<Bitmap?> = remember { mutableStateOf(null) }
        val isGrayScale: MutableState<Boolean> = remember { mutableStateOf(false) }
        val openSlider: MutableState<Boolean> = remember { mutableStateOf(false) }
        val sliderPosition: MutableState<Int> = remember { mutableStateOf(0) }
        val isLoading: MutableState<Boolean> = remember { mutableStateOf(false) }

        var bitmapFilters = BitmapFilters()

        val pickMedia =
            rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                isGrayScale.value = false
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                uri?.let {
                    if (Build.VERSION.SDK_INT > 28) {
                        val source = ImageDecoder.createSource(this.contentResolver, uri)
                        val bitmap =
                            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)
                        imageBitmap.value = bitmap
                    }
                }
            }

        fun requestImage() {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        FPIKotlinTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "FPI - Trabalho 1 - 2023/2",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        actions = {
                            IconButton(onClick = {
                                if (imageBitmap.value != null) {
                                    imageSaver.saveImage(imageBitmap.value!!)
                                }
                            }, enabled = imageBitmap.value != null) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    modifier = Modifier.rotate(90f),
                                    contentDescription = "Download your image"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = accentColor)
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { requestImage() },
                        containerColor = accentColor
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add a new image")
                    }
                },
                bottomBar = {
                    MyBottomAppBar(containerColor = accentColor) {
                        IconButton(
                            onClick = {
                                imageBitmap.value = bitmapFilters.horizontalFlip(imageBitmap.value)
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_flip_24),
                                contentDescription = "Flip Horizontally"
                            )
                        }
                        IconButton(
                            onClick = {
                                imageBitmap.value = bitmapFilters.verticalFlip(imageBitmap.value)
                            },
                            modifier = Modifier.rotate(90F),
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_flip_24),
                                contentDescription = "Flip Vertically"
                            )
                        }
                        IconButton(
                            onClick = {
                                if (imageBitmap.value != null) {
                                    isGrayScale.value = true
                                    imageBitmap.value =
                                        bitmapFilters.makeLuminance(imageBitmap.value)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_tonality_24),
                                contentDescription = "Make image gray scale"
                            )
                        }
                        IconButton(
                            onClick = { openSlider.value = !openSlider.value },
                            enabled = isGrayScale.value,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_invert_colors_24),
                                contentDescription = "Quantize Gray Scale Image"
                            )
                        }
                    }
                }) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isLoading.value) {
                            IndeterminateCircularIndicator()
                        } else {
                            BitmapImage(imageBitmap.value)
                        }
                        if (openSlider.value && isGrayScale.value && bitmapFilters.totalShades > 0) {
                            sliderPosition.value =
                                if (sliderPosition.value == 0) bitmapFilters.totalShades else sliderPosition.value
                            SliderMinimalExample(sliderPosition, bitmapFilters.totalShades) {
                                isLoading.value = true
                                imageBitmap.value = bitmapFilters.quantizeGrayScale(it)
                                isLoading.value = false
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SliderMinimalExample(
        sliderPosition: MutableState<Int>,
        maxValue: Int,
        onValueChangeListener: (it: Int) -> Unit
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Slider(value = sliderPosition.value.toFloat(), onValueChange = {
                sliderPosition.value = it.toInt()
            }, onValueChangeFinished = {
                onValueChangeListener(sliderPosition.value)
            }, valueRange = 1f..maxValue.toFloat(), modifier = Modifier.fillMaxWidth(0.8f))
            Text(text = sliderPosition.value.toString())
        }
    }

    @Composable
    fun MyBottomAppBar(
        modifier: Modifier = Modifier,
        containerColor: Color = BottomAppBarDefaults.containerColor,
        contentColor: Color = contentColorFor(containerColor),
        tonalElevation: Dp = BottomAppBarDefaults.ContainerElevation,
        contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
        windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
        content: @Composable RowScope.() -> Unit
    ) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            //shape = ShapeKeyTokens.CornerNone,
            modifier = modifier
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets)
                    .height(80.0.dp)
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}
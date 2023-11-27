package com.example.fpikotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
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
    fun Histogram(histogram: IntArray?, pixelCount: Int) {
        if (histogram == null) {
            Log.d("DEBUG", "isNull")
            return
        }
        val configuration = LocalConfiguration.current
        Log.d("DEBUG", configuration.screenHeightDp.toString())
        Log.d("DEBUG", pixelCount.toString())
        Row(
            modifier = Modifier.fillMaxHeight(0.85f).fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            histogram.forEach { value ->
                Log.d("DEBUG", value.toString())
                Log.d("HEIGHT", (value * 1000 / pixelCount).toString())
                Surface (
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .height(Math.max(value * 10000 / pixelCount, 10).dp)
                        .width(10.dp)
                        .weight(1f)
                        .border(10.dp, accentColor),
                    color = accentColor
                ) {
                    Spacer(modifier = Modifier.fillMaxHeight())}
            }
        }
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
        val openSliderShades: MutableState<Boolean> = remember { mutableStateOf(false) }
        val openSliderBright: MutableState<Boolean> = remember { mutableStateOf(false) }
        val openSliderCont: MutableState<Boolean> = remember { mutableStateOf(false) }
        val showZoomOut: MutableState<Boolean> = remember { mutableStateOf(false) }
        val showConvolution: MutableState<Boolean> = remember { mutableStateOf(false) }
        val grayConv: MutableState<Boolean> = remember { mutableStateOf(false) }
        val conv127: MutableState<Boolean> = remember { mutableStateOf(false) }
        val kernel: MutableState<Array<Array<Double>>> =
            remember { mutableStateOf(ConvolutionFiltersEnum.GAUSSIAN.getKernel()) }
        val xZoom: MutableState<String> = remember { mutableStateOf("0") }
        val yZoom: MutableState<String> = remember { mutableStateOf("0") }
        val sliderPosition: MutableState<Int> = remember { mutableStateOf(0) }
        val isLoading: MutableState<Boolean> = remember { mutableStateOf(false) }
        val showHistogram: MutableState<Boolean> = remember { mutableStateOf(false) }

        var bitmapFilters = BitmapFilters()
        var histogramManager = HistogramManager()

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
                        histogramManager.histogram = null
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
                                "FPI - Trabalho 2 - 2023/2",
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
                            onClick = { openSliderShades.value = !openSliderShades.value },
                            enabled = isGrayScale.value,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_invert_colors_24),
                                contentDescription = "Quantize Gray Scale Image"
                            )
                        }
                        IconButton(
                            onClick = {
                                if (histogramManager.histogram == null && imageBitmap.value != null) {
                                    val width = imageBitmap.value!!.width
                                    val height = imageBitmap.value!!.height
                                    val pixels = IntArray(width * height)
                                    imageBitmap.value!!.getPixels(
                                        pixels,
                                        0,
                                        width,
                                        0,
                                        0,
                                        width,
                                        height
                                    )
                                    histogramManager.histogram = histogramManager.getHistogramArray(
                                        imageBitmap.value!!,
                                        pixels
                                    )
                                }
                                showHistogram.value = !showHistogram.value
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_bar_chart_24),
                                contentDescription = "Build Histogram"
                            )
                        }
                        IconButton(
                            onClick = { openSliderBright.value = !openSliderBright.value },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_brightness_7_24),
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = { openSliderCont.value = !openSliderCont.value },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_timeline_24),
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = {
                                if (imageBitmap.value != null) {
                                    imageBitmap.value =
                                        bitmapFilters.flip90Positive(imageBitmap.value)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_outward_24),
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = {
                                if (imageBitmap.value != null) {
                                    imageBitmap.value =
                                        bitmapFilters.flip90Negative(imageBitmap.value)
                                }
                            },
                            modifier = Modifier.rotate(90F),
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_outward_24),
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = {
                                if (imageBitmap.value != null) {
                                    imageBitmap.value =
                                        bitmapFilters.getNegative(imageBitmap.value)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_brightness_6_24),
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = {
                                if (imageBitmap.value != null) {
                                    imageBitmap.value =
                                        histogramManager.equalizeHistogram(
                                            imageBitmap.value!!,
                                            null,
                                            isGrayScale.value
                                        )
                                }
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_switch_access_shortcut_24),
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = {
                                if (imageBitmap.value != null) {
                                    imageBitmap.value =
                                        bitmapFilters.zoomIn(
                                            imageBitmap.value!!,
                                        )
                                }
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_zoom_in_24),
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = {
                                showZoomOut.value = !showZoomOut.value
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_view_compact_24),
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = {
                                showConvolution.value = !showConvolution.value
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_recycling_24),
                                contentDescription = null
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
                        } else if (showHistogram.value && imageBitmap.value != null) {
                            Histogram(
                                histogramManager.histogram,
                                imageBitmap.value!!.height * imageBitmap.value!!.width
                            )
                        } else {
                            BitmapImage(imageBitmap.value)
                        }
                        if (openSliderShades.value && isGrayScale.value && bitmapFilters.totalShades > 0) {
                            openSliderBright.value = false
                            openSliderCont.value = false
                            sliderPosition.value =
                                if (sliderPosition.value == 0) bitmapFilters.totalShades else sliderPosition.value
                            SliderMinimalExample(sliderPosition, bitmapFilters.totalShades) {
                                isLoading.value = true
                                imageBitmap.value = bitmapFilters.quantizeGrayScale(it)
                                isLoading.value = false
                            }
                        }

                        if (openSliderBright.value) {
                            openSliderShades.value = false
                            openSliderCont.value = false
                            sliderPosition.value =
                                0
                            SliderMinimalExample(sliderPosition, 255, -255) {
                                imageBitmap.value =
                                    bitmapFilters.adjustBrightness(imageBitmap.value, it)
                            }
                        }

                        if (openSliderCont.value) {
                            openSliderBright.value = false
                            openSliderShades.value = false
                            sliderPosition.value =
                                1
                            SliderMinimalExample(sliderPosition, 10, 1) {
                                imageBitmap.value =
                                    bitmapFilters.adjustContrast(imageBitmap.value, it / 5.0f)
                            }
                        }

                        if (showZoomOut.value) {
                            ZoomOutFields(xZoom, yZoom) {
                                if (imageBitmap.value != null) {
                                    imageBitmap.value =
                                        bitmapFilters.zoomOut(
                                            xZoom.value.toInt(),
                                            yZoom.value.toInt(),
                                            imageBitmap.value!!
                                        )
                                }
                            }
                        }

                        if (showConvolution.value) {
                            ConvolutionFields(kernel, grayConv, conv127) {
                                if (imageBitmap.value != null) {
                                    imageBitmap.value =
                                        bitmapFilters.applyConvolution(
                                            imageBitmap.value!!,
                                            kernel.value,
                                            grayConv.value,
                                            conv127.value
                                        )
                                }
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
        minValue: Int = 1,
        onValueChangeListener: (it: Int) -> Unit
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Slider(
                value = sliderPosition.value.toFloat(),
                onValueChange = {
                    sliderPosition.value = it.toInt()
                },
                onValueChangeFinished = {
                    onValueChangeListener(sliderPosition.value)
                },
                valueRange = minValue.toFloat()..maxValue.toFloat(),
                modifier = Modifier.fillMaxWidth(0.8f),
            )
            Text(text = sliderPosition.value.toString())
        }
    }

    @Composable
    fun ZoomOutFields(
        xValue: MutableState<String>,
        yValue: MutableState<String>,
        onConfirm: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.0.dp)
                .padding(10.dp)
        ) {
            val onChangeX: (String) -> Unit = { it ->
                xValue.value = it  // it is supposed to be this
            }
            val onChangeY: (String) -> Unit = { it ->
                yValue.value = it  // it is supposed to be this
            }
            BasicTextField(
                value = xValue.value,
                onValueChange = onChangeX,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            )
            BasicTextField(
                value = yValue.value,
                onValueChange = onChangeY,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            )
            IconButton(
                onClick = onConfirm
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_check_circle_24),
                    contentDescription = null
                )
            }
        }
    }

    @Composable
    fun ConvolutionFields(
        kernel: MutableState<Array<Array<Double>>>,
        gray: MutableState<Boolean>,
        sum127: MutableState<Boolean>,
        onConfirm: () -> Unit
    ) {
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row {
                Button(
                    onClick = {
                        kernel.value = ConvolutionFiltersEnum.GAUSSIAN.getKernel(); gray.value =
                        false; sum127.value = false
                    }, colors = ButtonDefaults.textButtonColors(
                        containerColor = accentColor
                    )
                ) {
                    Text("Gaussian")
                }
                Button(
                    onClick = {
                        kernel.value = ConvolutionFiltersEnum.LAPLACIAN.getKernel(); gray.value =
                        true; sum127.value = false
                    }, colors = ButtonDefaults.textButtonColors(
                        containerColor = accentColor
                    )
                ) {
                    Text("Laplacian")
                }
                Button(
                    onClick = {
                        kernel.value = ConvolutionFiltersEnum.HIGH_PASS.getKernel(); gray.value =
                        true; sum127.value = false
                    }, colors = ButtonDefaults.textButtonColors(
                        containerColor = accentColor
                    )
                ) {
                    Text("High Pass")
                }
                Button(
                    onClick = {
                        kernel.value = ConvolutionFiltersEnum.PREWITT_HX.getKernel(); gray.value =
                        true; sum127.value = true
                    }, colors = ButtonDefaults.textButtonColors(
                        containerColor = accentColor
                    )
                ) {
                    Text("Prewitt H")
                }
                Button(
                    onClick = {
                        kernel.value = ConvolutionFiltersEnum.PREWITT_HY.getKernel(); gray.value =
                        true; sum127.value = true
                    }, colors = ButtonDefaults.textButtonColors(
                        containerColor = accentColor
                    )
                ) {
                    Text("Prewitt V")
                }
                Button(
                    onClick = {
                        kernel.value = ConvolutionFiltersEnum.SOBEL_HX.getKernel(); gray.value =
                        true; sum127.value = true
                    }, colors = ButtonDefaults.textButtonColors(
                        containerColor = accentColor
                    )
                ) {
                    Text("Sobel H")
                }
                Button(
                    onClick = {
                        kernel.value = ConvolutionFiltersEnum.SOBEL_HY.getKernel(); gray.value =
                        true; sum127.value = true
                    }, colors = ButtonDefaults.textButtonColors(
                        containerColor = accentColor
                    )
                ) {
                    Text("Sobel Y")
                }
                IconButton(
                    onClick = onConfirm
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_check_circle_24),
                        contentDescription = null
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.0.dp)
            ) {
                for (i in kernel.value.indices) {
                    for (j in kernel.value[i].indices) {
                        val onChange: (String) -> Unit = { it ->
                            kernel.value[i][j] = it.toDouble()
                        }
                        BasicTextField(
                            value = kernel.value[i][j].toString(),
                            onValueChange = onChange,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        )
                    }
                }
            }
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
        val scrollState = rememberScrollState()
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
                    .padding(contentPadding)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}
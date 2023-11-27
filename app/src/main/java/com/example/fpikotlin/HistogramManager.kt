package com.example.fpikotlin

import android.graphics.Bitmap
import android.util.Log

class HistogramManager {
    var histogram: IntArray? = null
    /**
     *  Returns array of histogram values.
     *  @param image - Bitmap grayscale image.
     *  @param pixels - Pixel Array.
     */
    fun getHistogramArray(image: Bitmap?, pixelsArray: IntArray?): IntArray {
        if (image == null) {
            return IntArray(0)
        }
        val pixels = pixelsArray?.let { it } ?: run {
            val width = image.width
            val height = image.height
            val pixels = IntArray(width * height)
            image.getPixels(pixels, 0, width, 0, 0, width, height)
            pixels
        }

        val histogramArray = IntArray(256)

        pixels.forEach { pixel ->
            val luminance = pixel and 0xFF
            histogramArray[(luminance).coerceIn(0..254)]++
        }

        return histogramArray
    }

    /**
     *  Returns array of cumulative histogram values.
     *
     *  @param histogram - Array of histogram values.
     */
    private fun getCumulativeHistogram(histogram: IntArray, scalingFactor: Double): IntArray {
        val cumulativeHistogram = IntArray(256)
        Log.d("DEBUG", scalingFactor.toString())
        cumulativeHistogram[0] = histogram[0] * scalingFactor.toInt()
        for (i in 1 until cumulativeHistogram.size) {
            cumulativeHistogram[i] = cumulativeHistogram[i - 1] + (histogram[i] * scalingFactor).toInt()
        }
        return cumulativeHistogram
    }

    /**
     *  Equalize an image.
     *
     *  @param image - Grayscale bitmap image to be equalized
     *  @param histogram - Array of histogram values or null.
     *
     *  @return Equalized bitmap image.
     */
    fun equalizeHistogram(image: Bitmap, histogram: IntArray?, isGrayScale: Boolean?): Bitmap {
        val grayscaleImage = isGrayScale?.let { image } ?: run { BitmapFilters().makeLuminance(image) }
        ?: return image

        val width = grayscaleImage.width
        val height = grayscaleImage.height
        val pixels = IntArray(width * height)
        grayscaleImage.getPixels(pixels, 0, width, 0, 0, width, height)

        val scalingFactor = 255.0 / (width * height).toDouble()

        val nonNullHistogram = histogram?.let { it } ?: getHistogramArray(grayscaleImage, pixels)
        val cumulativeHistogram = getCumulativeHistogram(nonNullHistogram, scalingFactor)

        for (i in pixels.indices) {
            val value = cumulativeHistogram[pixels[i] and 0xFF]
            val red = value
            val blue = value
            val green = value
            pixels[i] = (red shl 16) or (green shl 8) or blue or (0xFF shl 24)
        }

        return Bitmap.createBitmap(pixels, width, height, grayscaleImage.config)
    }

    fun matchHistograms(image: Bitmap, target: Bitmap): Bitmap {
        val targetGrayscale = BitmapFilters().makeLuminance(target);

        val width = image.width
        val height = image.height
        val pixels = IntArray(width * height)
        image.getPixels(pixels, 0, width, 0, 0, width, height)

        val srcHistogram = this.getHistogramArray(image, pixels);

        val targetWidth = image.width
        val targetHeight = image.height
        val targetPixels = IntArray(width * height)
        target.getPixels(targetPixels, 0, width, 0, 0, width, height)

        val targetHistogram = this.getHistogramArray(targetGrayscale, targetPixels);

        var scalingFactor = 255.0 / (width * height).toDouble()
        val cumulativeSrcHistogram = this.getCumulativeHistogram(srcHistogram, scalingFactor);
        scalingFactor = 255.0 / (targetWidth * targetHeight).toDouble()
        val cumulativeTargetHistogram = this.getCumulativeHistogram(targetHistogram, scalingFactor);

        val mappedValues = IntArray(256);

        for(i in 0..255) {
            var j = 255
            while (j >= 0) {
                if(cumulativeSrcHistogram[i] == cumulativeTargetHistogram[j])
                    mappedValues[i] = j
                j--
            }
        }

        for (i in pixels.indices) {
            pixels[i] = mappedValues[pixels[i] and 0xFF]
        }

        return Bitmap.createBitmap(pixels, width, height, image.config);
    }

}

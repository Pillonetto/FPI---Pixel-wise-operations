package com.example.fpikotlin

import android.graphics.Bitmap

class HistogramManager {
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
            histogramArray[luminance]++
        }

        return histogramArray
    }

    /**
     *  Returns array of cumulative histogram values.
     *
     *  @param histogram - Array of histogram values.
     */
    private fun getCumulativeHistogram(histogram: IntArray, scalingFactor: Float): IntArray {
        val cumulativeHistogram = IntArray(256)
        cumulativeHistogram[0] = histogram[0] * scalingFactor.toInt()
        for (i in 1..cumulativeHistogram.size) {
            cumulativeHistogram[i] = cumulativeHistogram[i - 1] + histogram[i] * scalingFactor.toInt()
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

        if(grayscaleImage == null) {
            return image
        }

        val width = grayscaleImage.width
        val height = grayscaleImage.height
        val pixels = IntArray(width * height)
        grayscaleImage.getPixels(pixels, 0, width, 0, 0, width, height)

        val scalingFactor = 255 / (width * height).toFloat()

        val nonNullHistogram = histogram?.let { it } ?: getHistogramArray(grayscaleImage, pixels)
        val cumulativeHistogram = getCumulativeHistogram(nonNullHistogram, scalingFactor)

        for (i in pixels.indices) {
            pixels[i] = cumulativeHistogram[pixels[i] and 0xFF]
        }

        return Bitmap.createBitmap(pixels, width, height, grayscaleImage.config)
    }
}
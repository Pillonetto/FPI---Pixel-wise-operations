package com.example.fpikotlin

import android.graphics.Bitmap

class BitmapFilters {
    var lowestValue = 0
    var highestValue = 0
    var totalShades = 0

    var quantizationBitmap: Bitmap? = null

    fun horizontalFlip(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) {
            return bitmap
        }
        val width = bitmap.width
        val height = bitmap.height

        val pixels = IntArray(width * height)

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            val rowStart = y * width
            val rowEnd = rowStart + width - 1
            var left = rowStart
            var right = rowEnd

            //Inverte os pixels da linha
            while (left < right) {
                val temp = pixels[left]
                pixels[left] = pixels[right]
                pixels[right] = temp

                left++
                right--
            }
        }

        // Cria novo bitmap com os pixels invertidos
        val resultBitmap = Bitmap.createBitmap(width, height, bitmap.config)
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return resultBitmap
    }

    fun verticalFlip(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) {
            return bitmap
        }
        val width = bitmap.width
        val height = bitmap.height

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val flippedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // ArrayCopy
        val flippedPixels = IntArray(width * height)
        for (row in 0 until height) {
            val destRowIndex = height - 1 - row
            // ArrOrigem, Posicao inicial (comecand em 0), ArrSaida, Posicao inicial da saida, tamanho
            // Copia uma linha inteira do bitmap original para o bitmap invertido
            System.arraycopy(pixels, row * width, flippedPixels, destRowIndex * width, width)
        }

        // Cria novo bitmap com os pixels invertidos
        flippedBitmap.setPixels(flippedPixels, 0, width, 0, 0, width, height)

        return flippedBitmap
    }

    fun makeLuminance(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) {
            return bitmap
        }
        val width = bitmap.width
        val height = bitmap.height

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            //Pixel eh valor RRGGBB, fazemos shift e and para pegar cada cor
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF
            //Calculo da luminancia fornecido pelo professor, convertido para int para habilitar shifts
            val luminance = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
            //Valor em hexadecimal #AARRGGBB
            pixels[i] = ((luminance shl 16) or (luminance shl 8) or luminance or (0xFF shl 24))
            lowestValue = if (luminance < lowestValue) luminance else lowestValue
            highestValue = if (luminance > highestValue) luminance else highestValue
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        quantizationBitmap = bitmap.copy(bitmap.config, true)
        totalShades = highestValue - lowestValue + 1

        return bitmap
    }

    fun quantizeGrayScale(shades: Int): Bitmap? {
        if (quantizationBitmap == null || shades == 0) {
            return null
        }

        val bitmap = quantizationBitmap!!.copy(quantizationBitmap!!.config, true)

        val binSize = totalShades / shades
        val width = bitmap.width
        val height = bitmap.height

        val pixels = IntArray(width * height)


        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val lowestValueCorrected = kotlin.math.max(0.0, lowestValue - 0.5)
        var lowerLimit = 0.0
        var upperLimit = 0.0

        for (i in pixels.indices) {
            val pixelLuminance = pixels[i] and 0XFF
            var j = pixelLuminance - lowestValueCorrected
            j = kotlin.math.floor(j / binSize)
            lowerLimit = lowestValueCorrected + j * binSize
            upperLimit = lowestValueCorrected + (j + 1) * binSize
            val newValue = kotlin.math.min(((upperLimit + lowerLimit) / 2).toInt(), 255)
            pixels[i] = (newValue shl 16) or (newValue shl 8) or newValue or (0xFF shl 24)
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
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

    fun flip90Positive(bitmap: Bitmap?): Bitmap?
    {
        if (bitmap == null) {
            return bitmap
        }
        val width = bitmap.width
        val height = bitmap.height

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val pixelMatrix = Array(height) { Array(width) { 0 } }
        for(i in 0 until height) {
            for(j in 0 until width) {
                pixelMatrix[i][j] = pixels[i * width + j]
            }
        }

        val transposedMatrix = transposeMatrix(pixelMatrix)
        for(i in 0 until height) {
            transposedMatrix[i].reverse();
        }
        val newPixels = transposedMatrix.flatMap { it.asIterable() }.toIntArray()

        val resultBitmap = Bitmap.createBitmap(height, width, bitmap.config)
        resultBitmap.setPixels(newPixels, 0, width, 0, 0, width, height)

        return resultBitmap
    }

    fun flip90Negative(bitmap: Bitmap?): Bitmap?
    {
        if (bitmap == null) {
            return bitmap
        }
        val width = bitmap.width
        val height = bitmap.height

        val pixels = IntArray(width * height)
        val pixelMatrix = getMatrixFromPixels(pixels, width, height)

        for(i in 0 until height) {
            pixelMatrix[i].reverse();
        }
        val transposedMatrix = transposeMatrix(pixelMatrix)
        val newPixels = transposedMatrix.flatMap { it.asIterable() }.toIntArray()

        val resultBitmap = Bitmap.createBitmap(height, width, bitmap.config)
        resultBitmap.setPixels(newPixels, 0, width, 0, 0, width, height)

        return resultBitmap
    }

    fun getMatrixFromPixels(pixels: IntArray, width: Int, height: Int): Array<Array<Int>> {
        val pixelMatrix = Array(height) { Array(width) { 0 } }
        for(i in 0 until height) {
            for(j in 0 until width) {
                pixelMatrix[i][j] = pixels[i * width + j]
            }
        }
        return pixelMatrix
    }

    // Function to transpose a matrix
    fun transposeMatrix(matrix: Array<Array<Int>>): Array<Array<Int>> {
        val rows = matrix.size
        val columns = matrix[0].size
        val transposedMatrix = Array(columns) { Array(rows) { 0 } }

        for (i in 0 until rows) {
            for (j in 0 until columns) {
                transposedMatrix[j][i] = matrix[i][j]
            }
        }

        return transposedMatrix
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
        var lowerLimit: Double
        var upperLimit: Double

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

    fun adjustBrightness(bitmap: Bitmap?, factor: Int): Bitmap? {
        if (bitmap == null) {
            return null
        }
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val pixelRange = 0..255
        val acceptedInterval = -255 .. 255
        val acceptedFactor = factor.coerceIn(acceptedInterval)

        for(i in pixels.indices) {
            val pixel = pixels[i]
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF

            val newRed = (red + acceptedFactor).coerceIn(pixelRange)
            val newGreen = (green + acceptedFactor).coerceIn(pixelRange)
            val newBlue = (blue + acceptedFactor).coerceIn(pixelRange)

            pixels[i] = (newRed shl 16) or (newGreen shl 8) or newBlue or (0xFF shl 24)
        }

        val resultBitmap = Bitmap.createBitmap(width, height, bitmap.config)
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return resultBitmap
    }

    fun adjustContrast(bitmap: Bitmap?, factor: Int): Bitmap? {
        if (bitmap == null) {
            return null
        }
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val pixelRange = 0..255
        val acceptedFactor = kotlin.math.abs(factor)

        for(i in pixels.indices) {
            val pixel = pixels[i]
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF

            val newRed = (red * acceptedFactor).coerceIn(pixelRange)
            val newGreen = (green * acceptedFactor).coerceIn(pixelRange)
            val newBlue = (blue * acceptedFactor).coerceIn(pixelRange)

            pixels[i] = (newRed shl 16) or (newGreen shl 8) or newBlue or (0xFF shl 24)
        }

        val resultBitmap = Bitmap.createBitmap(width, height, bitmap.config)
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return resultBitmap
    }

    fun getNegative(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) {
            return null
        }
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for(i in pixels.indices) {
            val pixel = pixels[i]
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF

            val newRed = 255 - red
            val newGreen = 255 - green
            val newBlue = 255 - blue

            pixels[i] = (newRed shl 16) or (newGreen shl 8) or newBlue or (0xFF shl 24)
        }

        val resultBitmap = Bitmap.createBitmap(width, height, bitmap.config)
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return resultBitmap
    }

    fun zoomOut(sX: Int, sY: Int, image: Bitmap): Bitmap {
        val width = image.width
        val height = image.height

        val pixels = IntArray(width * height)
        val pixelMatrix = getMatrixFromPixels(pixels, width, height)

        val newWidth = width / sX
        val newHeight = height / sY
        val newPixels = IntArray(newWidth * newHeight)

        for(i in 0 until newHeight) {
            for(j in 0 until newWidth) {
                var avgRed = 0
                var avgGreen = 0
                var avgBlue = 0

                var startY = i * sY
                var startX = j * sX
                for(k in 0 until sY) {
                    for(l in 0 until sX) {
                        val pixel = pixelMatrix[startY + k][startX + l]
                        avgRed +=  (pixel shr 16)and 0XFF
                        avgGreen +=  (pixel shr 8)and 0XFF
                        avgBlue +=  pixel and 0XFF
                    }
                }

                avgRed /= sX * sY
                avgGreen /= sX * sY
                avgBlue /= sX * sY

                val newPixel = (avgRed shl 16) or (avgGreen shl 8) or avgBlue or (0xFF shl 24)
                newPixels[i * newWidth + j] = newPixel
            }
        }

        val resultBitmap = Bitmap.createBitmap(newWidth, newHeight, image.config)
        resultBitmap.setPixels(newPixels, 0, newWidth, 0, 0, newWidth, newHeight)
        return resultBitmap
    }

    fun zoomIn(image: Bitmap): Bitmap {
        val width = image.width
        val height = image.height

        val pixels = IntArray(width * height)
        val pixelMatrix = getMatrixFromPixels(pixels, width, height)

        val newWidth = width * 2
        val newHeight = height * 2

        var newPixels = getMatrixFromPixels(IntArray(newWidth * newHeight), newWidth, newHeight)
        // New pixels across width
        for(i in 0 until height) {
            for(j in 1 until newWidth step 2) {
                val fstPixel = pixelMatrix[i][(j/2 - 1)]
                val sndPixel = pixelMatrix[i][(j/2)]

                val red1 = (fstPixel shr 16) and 0xFF
                val red2 = (sndPixel shr 16) and 0xFF
                val green1 = (fstPixel shr 8) and 0xFF
                val green2 = (sndPixel shr 8) and 0xFF
                val blue1 = fstPixel and 0xFF
                val blue2 = sndPixel and 0xFF

                var intRed = (red1 + red2) / 2
                var intGreen = (green1 + green2) / 2
                var intBlue = (blue1 + blue2) / 2

                val newPixel = (intRed shl 16) or (intGreen shl 8) or intBlue or (0xFF shl 24)
                newPixels[i][j-1] = fstPixel
                newPixels[i][j] = newPixel
                newPixels[i][j + 1] = sndPixel
            }
        }

        // New pixels across height
        for(i in 1 until newHeight step 2) {
            for(j in 0 until newWidth) {
                val fstPixel = newPixels[(i/2 - 1)][j]
                val sndPixel = newPixels[(i/2)][j]

                val red1 = (fstPixel shr 16) and 0xFF
                val red2 = (sndPixel shr 16) and 0xFF
                val green1 = (fstPixel shr 8) and 0xFF
                val green2 = (sndPixel shr 8) and 0xFF
                val blue1 = fstPixel and 0xFF
                val blue2 = sndPixel and 0xFF

                var intRed = (red1 + red2) / 2
                var intGreen = (green1 + green2) / 2
                var intBlue = (blue1 + blue2) / 2

                val newPixel = (intRed shl 16) or (intGreen shl 8) or intBlue or (0xFF shl 24)
                newPixels[i-1][j] = fstPixel
                newPixels[i][j] = newPixel
                newPixels[i + 1][j] = sndPixel
            }
        }

        val resultBitmap = Bitmap.createBitmap(newWidth, newHeight, image.config)
        resultBitmap.setPixels(newPixels.flatMap { it.asIterable() }.toIntArray(), 0, newWidth, 0, 0, newWidth, newHeight)
        return resultBitmap
    }

    fun applyConvolution(image: Bitmap, kernel: Array<Array<Int>>): Bitmap {
        val width = image.width
        val height = image.height

        val pixels = IntArray(width * height)
        val pixelMatrix = getMatrixFromPixels(pixels, width, height)

        val newPixels = getMatrixFromPixels(IntArray(width * height), width, height)

        val kernelSize = kernel.size
        val kernelHalfSize = kernelSize / 2

        for(i in 0 until height) {
            for(j in 0 until width) {
                var newRed = 0
                var newGreen = 0
                var newBlue = 0

                for(k in 0 until kernelSize) {
                    for(l in 0 until kernelSize) {
                        val pixel = pixelMatrix[i + k - kernelHalfSize][j + l - kernelHalfSize]
                        val red = (pixel shr 16) and 0xFF
                        val green = (pixel shr 8) and 0xFF
                        val blue = pixel and 0xFF

                        newRed += red * kernel[k][l]
                        newGreen += green * kernel[k][l]
                        newBlue += blue * kernel[k][l]
                    }
                }

                newRed = newRed.coerceIn(0..255)
                newGreen = newGreen.coerceIn(0..255)
                newBlue = newBlue.coerceIn(0..255)

                val newPixel = (newRed shl 16) or (newGreen shl 8) or newBlue or (0xFF shl 24)
                newPixels[i][j] = newPixel
            }
        }

        val resultBitmap = Bitmap.createBitmap(width, height, image.config)
        resultBitmap.setPixels(newPixels.flatMap { it.asIterable() }.toIntArray(), 0, width, 0, 0, width, height)
        return resultBitmap
    }
}
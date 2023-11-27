package com.example.fpikotlin

val SIZE = 3
enum class ConvolutionFiltersEnum {

    GAUSSIAN {
        override fun getKernel(): Array<Array<Double>> {
            val kernel = Array(SIZE) { Array(SIZE) { 0.0 } }
            kernel[0] = arrayOf(0.0625, 0.125, 0.0625)
            kernel[1] = arrayOf(0.125, 0.25, 0.125)
            kernel[2] = arrayOf(0.0625, 0.125, 0.0625)
            return kernel
        }
    },

    LAPLACIAN {
        override fun getKernel(): Array<Array<Double>> {
            val kernel = Array(SIZE) { Array(SIZE) { 0.0 } }
            kernel[0] = arrayOf(0.0, -1.0, 0.0)
            kernel[1] = arrayOf(-1.0, 4.0, -1.0)
            kernel[2] = arrayOf(0.0, -1.0, 0.0)
            return kernel
        }
    },

    HIGH_PASS {
        override fun getKernel(): Array<Array<Double>> {
            val kernel = Array(SIZE) { Array(SIZE) { 0.0 } }
            kernel[0] = arrayOf(-1.0, -1.0, -1.0)
            kernel[1] = arrayOf(-1.0, 8.0, -1.0)
            kernel[2] = arrayOf(-1.0, -1.0, -1.0)
            return kernel
        }
    },

    PREWITT_HX {
        override fun getKernel(): Array<Array<Double>> {
            val kernel = Array(SIZE) { Array(SIZE) { 0.0 } }
            kernel[0] = arrayOf(-1.0, 0.0, 1.0)
            kernel[1] = arrayOf(-1.0, 0.0, 1.0)
            kernel[2] = arrayOf(-1.0, 0.0, 1.0)
            return kernel
        }
    },

    PREWITT_HY {
        override fun getKernel(): Array<Array<Double>> {
            val kernel = Array(SIZE) { Array(SIZE) { 0.0 } }
            kernel[0] = arrayOf(-1.0, -1.0, -1.0)
            kernel[1] = arrayOf(0.0, 0.0, 0.0)
            kernel[2] = arrayOf(1.0, 1.0, 1.0)
            return kernel
        }
    },


    SOBEL_HX {
        override fun getKernel(): Array<Array<Double>> {
            val kernel = Array(SIZE) { Array(SIZE) { 0.0 } }
            kernel[0] = arrayOf(-1.0, 0.0, 1.0)
            kernel[1] = arrayOf(-2.0, 0.0, 2.0)
            kernel[2] = arrayOf(-1.0, 0.0, 1.0)
            return kernel
        }
    },


    SOBEL_HY {
        override fun getKernel(): Array<Array<Double>> {
            val kernel = Array(SIZE) { Array(SIZE) { 0.0 } }
            kernel[0] = arrayOf(-1.0, -2.0, -1.0)
            kernel[1] = arrayOf(0.0, 0.0, 0.0)
            kernel[2] = arrayOf(1.0, 2.0, 1.0)
            return kernel
        }
    };

    abstract fun getKernel(): Array<Array<Double>>
}
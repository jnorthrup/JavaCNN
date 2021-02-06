package edu.hitsz.c102c.cnn

import edu.hitsz.c102c.cnn.CNN.LayerBuilder
import edu.hitsz.c102c.cnn.Layer
import edu.hitsz.c102c.cnn.CNN
import edu.hitsz.c102c.dataset.Dataset
import edu.hitsz.c102c.cnn.CNN.Lisenter
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.io.PrintWriter
import java.lang.RuntimeException
import edu.hitsz.c102c.cnn.Layer.LayerType
import edu.hitsz.c102c.util.ConcurenceRunner.TaskManager
import edu.hitsz.c102c.cnn.Layer.Size
import java.io.ObjectOutputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.FileInputStream
import java.lang.ClassNotFoundException
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.jvm.JvmStatic
import edu.hitsz.c102c.util.TimedTest.TestTask
import edu.hitsz.c102c.cnn.RunCNN
import edu.hitsz.c102c.util.*
import java.io.PrintStream
import edu.hitsz.c102c.util.Util.OperatorOnTwo
import java.util.Arrays
import java.util.HashSet
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.lang.Runnable
import java.util.concurrent.CountDownLatch
import java.lang.InterruptedException
import java.util.concurrent.Executors
import java.io.BufferedReader
import java.io.FileReader
import java.io.Serializable

/**
 * cnn����Ĳ�
 *
 * @author jiqunpeng
 *
 * ����ʱ�䣺2014-7-8 ����3:58:46
 */
class Layer private constructor() : Serializable {
    var outMapNum: Int =0

    var mapSize  : Size? = null /**
     * ��ȡ�������
     *
     * @return
     */
    lateinit var  type          : LayerType



    /**
     * ��ȡ����˵Ĵ�С��ֻ�о������kernelSize���������δnull
     *
     * @return
     */
    var kernelSize // ����˴�С��ֻ�о������
            : Size? = null
        private set

    /**
     * ��ȡ������С��ֻ�в�������scaleSize���������δnull
     *
     * @return
     */
    var scaleSize // ������С��ֻ�в�������
            : Size? = null
        private set

    /**
     * ��ȡ���еľ����
     *
     * @return
     */
    lateinit var kernel // ����ˣ�ֻ�о������������
            : Array<Array<Array<DoubleArray>>>
        private set

      lateinit var bias // ÿ��map��Ӧһ��ƫ�ã�ֻ�о������������
            : DoubleArray
private set
    /**
     * ��ȡbatch����map����
     *
     * @return
     */
    // �������batch�����map��outmaps[0][0]��ʾ��һ����¼ѵ���µ�0�����map
    lateinit var maps: Array<Array<Array<DoubleArray>>>
        private set

    /**
     * ��ȡ����(ÿ����¼��ÿ��map)�Ĳв�
     *
     * @return
     */
    // �в��matlab toolbox��d��Ӧ
    lateinit var errors: Array<Array<Array<DoubleArray>>>
        private set

    /**
     * ��ȡ������
     *
     * @return
     */
    var classNum = -1 // ������
        private set








    enum class LayerType {
        // ���������ͣ�����㡢����㡢����㡢������
        input, output, conv, samp
    }

    /**
     * ����˻��߲�����scale�Ĵ�С,�������Բ���.���Ͱ�ȫ�����Ժ󲻿��޸�
     *
     * @author jiqunpeng
     *
     * ����ʱ�䣺2014-7-8 ����4:11:00
     */
    class Size(val x: Int, val y: Int) : Serializable {
        override fun toString(): String {
            val s = StringBuilder("Size(").append(" x = ")
                .append(x).append(" y= ").append(y).append(")")
            return s.toString()
        }

        /**
         * ����scaleSize�õ�һ���µ�Size��Ҫ��this.x��this.
         * y�ֱܷ�scaleSize.x��scaleSize.y����
         *
         * @param scaleSize
         * @return
         */
        fun divide(scaleSize: Size?): Size {
            val x = x / scaleSize!!.x
            val y = y / scaleSize.y
            if (x * scaleSize.x != this.x || y * scaleSize.y != this.y) throw RuntimeException("$this��������$scaleSize")
            return Size(x, y)
        }

        /**
         * ��ȥsize��С����x��y�ֱ𸽼�һ��ֵappend
         *
         * @param size
         * @param append
         * @return
         */
        fun subtract(size: Size?, append: Int): Size {
            val x = x - size!!.x + append
            val y = y - size.y + append
            return Size(x, y)
        }

        companion object {
            private const val serialVersionUID = -209157832162004118L
        }
    }

    /**
     * �����ʼ�������
     *
     * @param frontMapNum
     */
    fun initKernel(frontMapNum: Int) {
//		int fan_out = getOutMapNum() * kernelSize.x * kernelSize.y;
//		int fan_in = frontMapNum * kernelSize.x * kernelSize.y;
//		double factor = 2 * Math.sqrt(6 / (fan_in + fan_out));
        kernel = Array(frontMapNum) {
            Array(outMapNum) {
                Array(
                    kernelSize!!.x
                ) { DoubleArray(kernelSize!!.y) }
            }
        }
        for (i in 0 until frontMapNum) for (j in 0 until outMapNum) kernel[i][j] = Util.randomMatrix(
            kernelSize!!.x, kernelSize!!.y, true
        )
    }

    /**
     * �����ľ���˵Ĵ�С����һ���map��С
     *
     * @param frontMapNum
     * @param size
     */
    fun initOutputKerkel(frontMapNum: Int, size: Size?) {
        kernelSize = size
        //		int fan_out = getOutMapNum() * kernelSize.x * kernelSize.y;
//		int fan_in = frontMapNum * kernelSize.x * kernelSize.y;
//		double factor = 2 * Math.sqrt(6 / (fan_in + fan_out));
        kernel = Array(frontMapNum) {
            Array(outMapNum) {
                Array(
                    kernelSize!!.x
                ) { DoubleArray(kernelSize!!.y) }
            }
        }
        for (i in 0 until frontMapNum) for (j in 0 until outMapNum) kernel[i][j] = Util.randomMatrix(
            kernelSize!!.x, kernelSize!!.y, false
        )
    }

    /**
     * ��ʼ��ƫ��
     *
     * @param frontMapNum
     */
    fun initBias(frontMapNum: Int) {
        bias = Util.randomArray(outMapNum)
    }

    /**
     * ��ʼ�����map
     *
     * @param batchSize
     */
    fun initOutmaps(batchSize: Int) {
        maps = Array(batchSize) {
            Array(outMapNum) {
                Array(
                    mapSize!!.x
                ) { DoubleArray(mapSize!!.y) }
            }
        }
    }

    /**
     * ����mapֵ
     *
     * @param mapNo
     * �ڼ���map
     * @param mapX
     * map�ĸ�
     * @param mapY
     * map�Ŀ�
     * @param value
     */
    fun setMapValue(mapNo: Int, mapX: Int, mapY: Int, value: Double) {
        maps[recordInBatch][mapNo]!![mapX]!![mapY] = value
    }

    /**
     * �Ծ�����ʽ���õ�mapNo��map��ֵ
     *
     * @param mapNo
     * @param outMatrix
     */
    fun setMapValue(mapNo: Int, outMatrix: Array<DoubleArray>) {
        // Log.i(type.toString());
        // Util.printMatrix(outMatrix);
        maps[recordInBatch][mapNo] = outMatrix
    }

    /**
     * ��ȡ��index��map���󡣴������ܿ��ǣ�û�з��ظ��ƶ��󣬶���ֱ�ӷ������ã����ö��������
     * �����޸�outmaps�������޸������setMapValue(...)
     *
     * @param index
     * @return
     */
    fun getMap(index: Int): Array<DoubleArray> {
        return maps[recordInBatch][index]
    }

    /**
     * ��ȡǰһ���i��map����ǰ���j��map�ľ����
     *
     * @param i
     * ��һ���map�±�
     * @param j
     * ��ǰ���map�±�
     * @return
     */
    fun getKernel(i: Int, j: Int): Array<DoubleArray> {
        return kernel[i][j]
    }

    /**
     * ���òв�ֵ
     *
     * @param mapNo
     * @param mapX
     * @param mapY
     * @param value
     */
    fun setError(mapNo: Int, mapX: Int, mapY: Int, value: Double) {
        errors[recordInBatch][mapNo]!![mapX]!![mapY] = value
    }

    /**
     * ��map�������ʽ���òв�ֵ
     *
     * @param mapNo
     * @param matrix
     */
    fun setError(mapNo: Int, matrix: Array<DoubleArray>) {
        // Log.i(type.toString());
        // Util.printMatrix(matrix);
        errors[recordInBatch][mapNo] = matrix
    }

    /**
     * ��ȡ��mapNo��map�Ĳв�.û�з��ظ��ƶ��󣬶���ֱ�ӷ������ã����ö��������
     * �����޸�errors�������޸������setError(...)
     *
     * @param mapNo
     * @return
     */
    fun getError(mapNo: Int): Array<DoubleArray> {
        return errors[recordInBatch][mapNo]
    }

    /**
     * ��ʼ���в�����
     *
     * @param batchSize
     */
    fun initErros(batchSize: Int) {
        errors = Array(batchSize) {
            Array(outMapNum) {
                Array(
                    mapSize!!.x
                ) { DoubleArray(mapSize!!.y) }
            }
        }
    }

    /**
     *
     * @param lastMapNo
     * @param mapNo
     * @param kernel
     */
    fun setKernel(lastMapNo: Int, mapNo: Int, kernel: Array<DoubleArray>) {
        this.kernel[lastMapNo][mapNo] = kernel
    }

    /**
     * ��ȡ��mapNo��
     *
     * @param mapNo
     * @return
     */
    fun getBias(mapNo: Int): Double {
        return bias!![mapNo]
    }

    /**
     * ���õ�mapNo��map��ƫ��ֵ
     *
     * @param mapNo
     * @param value
     */
    fun setBias(mapNo: Int, value: Double) {
        bias!![mapNo] = value
    }

    /**
     * ��ȡ��recordId��¼�µ�mapNo�Ĳв�
     *
     * @param recordId
     * @param mapNo
     * @return
     */
    fun getError(recordId: Int, mapNo: Int): Array<DoubleArray> {
        return errors[recordId][mapNo]
    }

    /**
     * ��ȡ��recordId��¼�µ�mapNo�����map
     *
     * @param recordId
     * @param mapNo
     * @return
     */
    fun getMap(recordId: Int, mapNo: Int): Array<DoubleArray> {
        return maps[recordId][mapNo]
    }

    companion object {
        /**
         *
         */
        private const val serialVersionUID = -5747622503947497069L
        private var recordInBatch = 0 // ��¼��ǰѵ������batch�ĵڼ�����¼

        /**
         * ׼����һ��batch��ѵ��
         */
        fun prepareForNewBatch() {
            recordInBatch = 0
        }

        /**
         * ׼����һ����¼��ѵ��
         */
        fun prepareForNewRecord() {
            recordInBatch++
        }

        /**
         * ��ʼ�������
         *
         * @param mapSize
         * @return
         */
        fun buildInputLayer(mapSize: Size?): Layer {
            val layer = Layer()
            layer.type = LayerType.input
            layer.outMapNum = 1 // ������map����Ϊ1����һ��ͼ
            layer.mapSize=(mapSize) //
            return layer
        }

        /**
         * ��������
         *
         * @return
         */
        fun buildConvLayer(outMapNum: Int, kernelSize: Size?): Layer {
            val layer = Layer()
            layer.type = LayerType.conv
            layer.outMapNum = outMapNum
            layer.kernelSize = kernelSize
            return layer
        }

        /**
         * ���������
         *
         * @param scaleSize
         * @return
         */
        fun buildSampLayer(scaleSize: Size?): Layer {
            val layer = Layer()
            layer.type = LayerType.samp
            layer.scaleSize = scaleSize
            return layer
        }

        /**
         * ���������,���������������ĸ��������������Ԫ�ĸ���
         *
         * @return
         */
        fun buildOutputLayer(classNum: Int): Layer {
            val layer = Layer()
            layer.classNum = classNum
            layer.type = LayerType.output
            layer.mapSize = Size(1, 1)
            layer.outMapNum = classNum
            // int outMapNum = 1;
            // while ((1 << outMapNum) < classNum)
            // outMapNum += 1;
            // layer.outMapNum = outMapNum;
            Log.i("outMapNum:" + layer.outMapNum)
            return layer
        }

        var count = 0
    }
}
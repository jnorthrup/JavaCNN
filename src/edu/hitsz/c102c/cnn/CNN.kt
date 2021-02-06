package edu.hitsz.c102c.cnn

import edu.hitsz.c102c.cnn.CNN.LayerBuilder
import edu.hitsz.c102c.cnn.Layer
import edu.hitsz.c102c.cnn.CNN
import edu.hitsz.c102c.dataset.Dataset
import edu.hitsz.c102c.cnn.CNN.Lisenter
import java.util.concurrent.atomic.AtomicBoolean
import java.lang.RuntimeException
import edu.hitsz.c102c.cnn.Layer.LayerType
import edu.hitsz.c102c.util.ConcurenceRunner.TaskManager
import edu.hitsz.c102c.cnn.Layer.Size
import java.lang.ClassNotFoundException
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.jvm.JvmStatic
import edu.hitsz.c102c.util.TimedTest.TestTask
import edu.hitsz.c102c.cnn.RunCNN
import edu.hitsz.c102c.util.*
import edu.hitsz.c102c.util.Util.OperatorOnTwo
import java.io.*
import java.util.Arrays
import java.util.HashSet
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.lang.Runnable
import java.util.concurrent.CountDownLatch
import java.lang.InterruptedException
import java.util.ArrayList
import java.util.concurrent.Executors

class CNN(layerBuilder: LayerBuilder, batchSize: Int) : Serializable {
    // ����ĸ���
    private val layers: List<Layer>

    // ����
    private val layerNum: Int

    // �������µĴ�С
    private val batchSize: Int

    // �������������Ծ����ÿһ��Ԫ�س���һ��ֵ
    private var divide_batchSize: Util.Operator? = null

    // �������������Ծ����ÿһ��Ԫ�س���alphaֵ
    private var multiply_alpha: Util.Operator? = null

    // �������������Ծ����ÿһ��Ԫ�س���1-labmda*alphaֵ
    private var multiply_lambda: Util.Operator? = null

    /**
     * ��ʼ��������
     */
    private fun initPerator() {
        divide_batchSize = object : Util.Operator {

            val serialVersionUID = 7424011281732651055L
            override fun process(value: Double): Double {
                return value / batchSize
            }
        }
        multiply_alpha = object : Util.Operator {

            val serialVersionUID = 5761368499808006552L
            override fun process(value: Double): Double {
                return value * ALPHA
            }
        }
        multiply_lambda = object : Util.Operator {

            val serialVersionUID = 4499087728362870577L
            override fun process(value: Double): Double {
                return value * (1 - LAMBDA * ALPHA)
            }
        }
    }

    /**
     * ��ѵ������ѵ������
     *
     * @param trainset
     * @param repeat
     * �����Ĵ���
     */
    fun train(trainset: Dataset?, repeat: Int) {
        // ����ֹͣ��ť
        Lisenter().start()
        var t = 0
        while (t < repeat && !stopTrain!!.get()) {
            var epochsNum = trainset!!.size() / batchSize
            if (trainset.size() % batchSize != 0) epochsNum++ // ���ȡһ�Σ�������ȡ��
            Log.i("")
            Log.i(t.toString() + "th iter epochsNum:" + epochsNum)
            var right = 0
            var count = 0
            for (i in 0 until epochsNum) {
                val randPerm = Util.randomPerm(trainset.size(), batchSize)
                Layer.Companion.prepareForNewBatch()
                for (index in randPerm!!) {
                    val isRight = train(trainset.getRecord(index))
                    if (isRight) right++
                    count++
                    Layer.Companion.prepareForNewRecord()
                }

                // ����һ��batch�����Ȩ��
                updateParas()
                if (i % 50 == 0) {
                    print("..")
                    if (i + 50 > epochsNum) println()
                }
            }
            val p = 1.0 * right / count
            if (t % 10 == 1 && p > 0.96) { //��̬����׼ѧϰ����
                ALPHA = 0.001 + ALPHA * 0.9
                Log.i("Set alpha = " + ALPHA)
            }
            Log.i("precision $right/$count=$p")
            t++
        }
    }

    internal class Lisenter : Thread() {
        override fun run() {
            println("Input & to stop train.")
            while (true) {
                try {
                    val a = System.`in`.read()
                    if (a == '&'.toInt()) {
                        stopTrain!!.compareAndSet(false, true)
                        break
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            println("Lisenter stop")
        }

        init {
            isDaemon = true
            stopTrain = AtomicBoolean(false)
        }
    }

    /**
     * ��������
     *
     * @param trainset
     * @return
     */
    fun test(trainset: Dataset): Double {
        Layer.Companion.prepareForNewBatch()
        val iter = trainset.iter()
        var right = 0
        while (iter!!.hasNext()) {
            val record = iter.next()
            forward(record)
            val outputLayer = layers[layerNum - 1]
            val mapNum = outputLayer.outMapNum
            val out = DoubleArray(mapNum)
            for (m in 0 until mapNum) {
                val outmap = outputLayer.getMap(m)
                out[m] = outmap!![0]!![0]
            }
            if (record!!.lable!!.toInt() == Util.getMaxIndex(out)) right++
        }
        val p = 1.0 * right / trainset.size()
        Log.i("precision", p.toString() + "")
        return p
    }

    /**
     * Ԥ����
     *
     * @param testset
     * @param fileName
     */
    fun predict(testset: Dataset?, fileName: String?) {
        Log.i("begin predict")
        try {
            val max = layers[layerNum - 1].classNum
            val writer = PrintWriter(File(fileName))
            Layer.Companion.prepareForNewBatch()
            val iter = testset!!.iter()
            while (iter!!.hasNext()) {
                val record = iter.next()
                forward(record)
                val outputLayer = layers[layerNum - 1]
                val mapNum = outputLayer.outMapNum
                val out = DoubleArray(mapNum)
                for (m in 0 until mapNum) {
                    val outmap = outputLayer.getMap(m)
                    out[m] = outmap!![0]!![0]
                }
                // int lable =
                // Util.binaryArray2int(out);
                val lable = Util.getMaxIndex(out)
                // if (lable >= max)
                // lable = lable - (1 << (out.length -
                // 1));
                writer.write(
                    """
    $lable
    
    """.trimIndent()
                )
            }
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        Log.i("end predict")
    }

    private fun isSame(output: DoubleArray, target: DoubleArray): Boolean {
        var r = true
        for (i in output.indices) if (Math.abs(output[i] - target[i]) > 0.5) {
            r = false
            break
        }
        return r
    }

    /**
     * ѵ��һ����¼��ͬʱ�����Ƿ�Ԥ����ȷ��ǰ��¼
     *
     * @param record
     * @return
     */
    private fun train(record: Dataset.Record?): Boolean {
        forward(record)
        return backPropagation(record)
        // System.exit(0);
    }

    /*
	 * ������
	 */
    private fun backPropagation(record: Dataset.Record?): Boolean {
        val result = setOutLayerErrors(record)
        setHiddenLayerErrors()
        return result
    }

    /**
     * ���²���
     */
    private fun updateParas() {
        for (l in 1 until layerNum) {
            val layer = layers[l]
            val lastLayer = layers[l - 1]
            when (layer.type) {
                LayerType.conv, LayerType.output -> {
                    updateKernels(layer, lastLayer)
                    updateBias(layer, lastLayer)
                }
                else -> {
                }
            }
        }
    }

    /**
     * ����ƫ��
     *
     * @param layer
     * @param lastLayer
     */
    private fun updateBias(layer: Layer, lastLayer: Layer) {
        val errors = layer.errors
        val mapNum = layer.outMapNum
        object : TaskManager(mapNum) {
            override fun process(start: Int, end: Int) {
                for (j in start until end) {
                    val error = Util.sum(errors, j)
                    // ����ƫ��
                    val deltaBias = Util.sum(error) / batchSize
                    val bias = layer.getBias(j) + ALPHA * deltaBias
                    layer.setBias(j, bias)
                }
            }
        }.start()
    }

    /**
     * ����layer��ľ���ˣ�Ȩ�أ���ƫ��
     *
     * @param layer
     * ��ǰ��
     * @param lastLayer
     * ǰһ��
     */
    private fun updateKernels(layer: Layer, lastLayer: Layer) {
        val mapNum = layer.outMapNum
        val lastMapNum = lastLayer.outMapNum
        object : TaskManager(mapNum) {
            override fun process(start: Int, end: Int) {
                for (j in start until end) {
                    for (i in 0 until lastMapNum) {
                        // ��batch��ÿ����¼delta���
                          var deltaKernel: Array<DoubleArray>  = emptyArray()
                        for (r in 0 until batchSize) {
                            val error = layer.getError(r, j)
                            deltaKernel = if (deltaKernel.isEmpty()) Util.convnValid(
                                lastLayer.getMap(r, i), error
                            ) else { // �ۻ����
                                Util.matrixOp(
                                    Util.convnValid(
                                        lastLayer.getMap(r, i), error
                                    ),
                                    deltaKernel, null, null, Util.plus
                                )
                            }
                        }

                        // ����batchSize
                        deltaKernel = Util.matrixOp(
                            deltaKernel,
                            divide_batchSize
                        )
                        // ���¾����
                        val kernel = layer.getKernel(i, j)
                        deltaKernel = Util.matrixOp(
                            kernel, deltaKernel,
                            multiply_lambda, multiply_alpha, Util.plus
                        )
                        layer.setKernel(i, j, deltaKernel)
                    }
                }
            }
        }.start()
    }

    /**
     * �����н�����Ĳв�
     */
    private fun setHiddenLayerErrors() {
        for (l in layerNum - 2 downTo 1) {
            val layer = layers[l]
            val nextLayer = layers[l + 1]
            when (layer.type) {
                LayerType.samp -> setSampErrors(layer, nextLayer)
                LayerType.conv -> setConvErrors(layer, nextLayer)
                else -> {
                }
            }
        }
    }

    /**
     * ���ò�����Ĳв�
     *
     * @param layer
     * @param nextLayer
     */
    private fun setSampErrors(layer: Layer, nextLayer: Layer) {
        val mapNum = layer.outMapNum
        val nextMapNum = nextLayer.outMapNum
        object : TaskManager(mapNum) {
            override fun process(start: Int, end: Int) {
                for (i in start until end) {
                    var sum: Array<DoubleArray> = emptyArray() // ��ÿһ������������
                    for (j in 0 until nextMapNum) {
                        val nextError = nextLayer.getError(j)
                        val kernel = nextLayer.getKernel(i, j)
                        // �Ծ���˽���180����ת��Ȼ�����fullģʽ�µþ��
                        sum = if (sum .isEmpty()  ) Util.convnFull(nextError, Util.rot180(kernel)) else Util.matrixOp(
                            Util.convnFull(
                                nextError,
                                Util.rot180(kernel)
                            ), sum, null,
                            null, Util.plus
                        )
                    }
                    layer.setError(i, sum)
                }
            }
        }.start()
    }

    /**
     * ���þ����Ĳв�
     *
     * @param layer
     * @param nextLayer
     */
    private fun setConvErrors(layer: Layer, nextLayer: Layer) {
        // ��������һ��Ϊ�����㣬�������map������ͬ����һ��mapֻ����һ���һ��map���ӣ�
        // ���ֻ�轫��һ��Ĳв�kronecker��չ���õ������
        val mapNum = layer.outMapNum
        object : TaskManager(mapNum) {
            override fun process(start: Int, end: Int) {
                for (m in start until end) {
                    val scale = nextLayer.scaleSize
                    val nextError = nextLayer.getError(m)
                    val map = layer.getMap(m)
                    // ������ˣ����Եڶ��������ÿ��Ԫ��value����1-value����
                    var outMatrix = Util.matrixOp(
                        map,
                        Util.cloneMatrix(map), null, Util.one_value,
                        Util.multiply
                    )
                    outMatrix = Util.matrixOp(
                        outMatrix,
                        Util.kronecker(nextError, scale!!), null, null,
                        Util.multiply
                    )
                    layer.setError(m, outMatrix)
                }
            }
        }.start()
    }

    /**
     * ���������Ĳв�ֵ,�������񾭵�Ԫ�������٣��ݲ����Ƕ��߳�
     *
     * @param record
     * @return
     */
    private fun setOutLayerErrors(record: Dataset.Record?): Boolean {
        val outputLayer = layers[layerNum - 1]
        val mapNum = outputLayer.outMapNum
        // double[] target =
        // record.getDoubleEncodeTarget(mapNum);
        // double[] outmaps = new double[mapNum];
        // for (int m = 0; m < mapNum; m++) {
        // double[][] outmap = outputLayer.getMap(m);
        // double output = outmap[0][0];
        // outmaps[m] = output;
        // double errors = output * (1 - output) *
        // (target[m] - output);
        // outputLayer.setError(m, 0, 0, errors);
        // }
        // // ��ȷ
        // if (isSame(outmaps, target))
        // return true;
        // return false;
        val target = DoubleArray(mapNum)
        val outmaps = DoubleArray(mapNum)
        for (m in 0 until mapNum) {
            val outmap = outputLayer.getMap(m)
            outmaps[m] = outmap!![0]!![0]
        }
        val lable = record!!.lable!!.toInt()
        target[lable] = 1.0
        // Log.i(record.getLable() + "outmaps:" +
        // Util.fomart(outmaps)
        // + Arrays.toString(target));
        for (m in 0 until mapNum) {
            outputLayer.setError(
                m, 0, 0, outmaps[m] * (1 - outmaps[m])
                        * (target[m] - outmaps[m])
            )
        }
        return lable == Util.getMaxIndex(outmaps)
    }

    /**
     * ǰ�����һ����¼
     *
     * @param record
     */
    private fun forward(record: Dataset.Record?) {
        // ����������map
        setInLayerOutput(record)
        for (l in 1 until layers.size) {
            val layer = layers[l]
            val lastLayer = layers[l - 1]
            when (layer.type) {
                LayerType.conv -> setConvOutput(layer, lastLayer)
                LayerType.samp -> setSampOutput(layer, lastLayer)
                LayerType.output -> setConvOutput(layer, lastLayer)
                else -> {
                }
            }
        }
    }

    /**
     * ���ݼ�¼ֵ���������������ֵ
     *
     * @param record
     */
    private fun setInLayerOutput(record: Dataset.Record?) {
        val inputLayer = layers[0]
        val mapSize = inputLayer.mapSize
        val attr = record!!.attrs
        if (attr!!.size != mapSize!!.x * mapSize.y) throw RuntimeException("���ݼ�¼�Ĵ�С�붨���map��С��һ��!")
        for (i in 0 until mapSize.x) {
            for (j in 0 until mapSize.y) {
                // ����¼���Ե�һά����Ū�ɶ�ά����
                inputLayer.setMapValue(0, i, j, attr!![mapSize.x * i + j])
            }
        }
    }

    /*
	 * �����������ֵ,ÿ���̸߳���һ����map
	 */
    private fun setConvOutput(layer: Layer, lastLayer: Layer) {
        val mapNum = layer.outMapNum
        val lastMapNum = lastLayer.outMapNum
        object : TaskManager(mapNum) {
            override fun process(start: Int, end: Int) {
                for (j in start until end) {
                    lateinit var sum: Array<DoubleArray>   // ��ÿһ������map�ľ���������
                    for (i in 0 until lastMapNum) {
                        val lastMap = lastLayer.getMap(i)
                        val kernel = layer.getKernel(i, j)
                        sum = when (i) {
                            0 -> Util.convnValid(lastMap, kernel)
                            else -> Util.matrixOp(
                                Util.convnValid(lastMap, kernel), sum,
                                null, null, Util.plus
                            )
                        }
                    }
                    val bias = layer.getBias(j)
                    sum = Util.matrixOp(sum, object : Util.Operator {
                        val serialVersionUID = 2469461972825890810L
                        override fun process(value: Double): Double {
                            return Util.sigmod(value + bias)
                        }
                    })
                    layer.setMapValue(j, sum)
                }
            }
        }.start()
    }

    /**
     * ���ò���������ֵ���������ǶԾ����ľ�ֵ����
     *
     * @param layer
     * @param lastLayer
     */
    private fun setSampOutput(layer: Layer, lastLayer: Layer) {
        val lastMapNum = lastLayer.outMapNum
        object : TaskManager(lastMapNum) {
            override fun process(start: Int, end: Int) {
                for (i in start until end) {
                    val lastMap = lastLayer.getMap(i)
                    val scaleSize = layer.scaleSize
                    // ��scaleSize������о�ֵ����
                    val sampMatrix = Util.scaleMatrix(lastMap, scaleSize)
                    layer.setMapValue(i, sampMatrix)
                }
            }
        }.start()
    }

    /**
     * ����cnn�����ÿһ��Ĳ���
     *
     * @param batchSize
     * * @param classNum
     * @param inputMapSize
     */
    fun setup(batchSize: Int) {
        val inputLayer = layers[0]
        // ÿһ�㶼��Ҫ��ʼ�����map
        inputLayer.initOutmaps(batchSize)
        for (i in 1 until layers.size) {
            val layer = layers[i]
            val frontLayer = layers[i - 1]
            val frontMapNum = frontLayer.outMapNum
            when (layer.type) {
                LayerType.input -> {
                }
                LayerType.conv -> {
                    // ����map�Ĵ�С
                    layer.mapSize = frontLayer.mapSize!!.subtract(
                        layer.kernelSize, 1
                    )
                    // ��ʼ������ˣ�����frontMapNum*outMapNum�������
                    layer.initKernel(frontMapNum)
                    // ��ʼ��ƫ�ã�����frontMapNum*outMapNum��ƫ��
                    layer.initBias(frontMapNum)
                    // batch��ÿ����¼��Ҫ����һ�ݲв�
                    layer.initErros(batchSize)
                    // ÿһ�㶼��Ҫ��ʼ�����map
                    layer.initOutmaps(batchSize)
                }
                LayerType.samp -> {
                    // �������map��������һ����ͬ
                    layer.outMapNum = frontMapNum
                    // ������map�Ĵ�С����һ��map�Ĵ�С����scale��С
                    layer.mapSize = frontLayer.mapSize!!.divide(
                        layer.scaleSize
                    )
                    // batch��ÿ����¼��Ҫ����һ�ݲв�
                    layer.initErros(batchSize)
                    // ÿһ�㶼��Ҫ��ʼ�����map
                    layer.initOutmaps(batchSize)
                }
                LayerType.output -> {
                    // ��ʼ��Ȩ�أ�����ˣ��������ľ���˴�СΪ��һ���map��С
                    layer.initOutputKerkel(frontMapNum, frontLayer.mapSize)
                    // ��ʼ��ƫ�ã�����frontMapNum*outMapNum��ƫ��
                    layer.initBias(frontMapNum)
                    // batch��ÿ����¼��Ҫ����һ�ݲв�
                    layer.initErros(batchSize)
                    // ÿһ�㶼��Ҫ��ʼ�����map
                    layer.initOutmaps(batchSize)
                }
            }
        }
    }

    /**
     * ������ģʽ�������,Ҫ�����ڶ������Ϊ�����������Ϊ�����
     *
     * @author jiqunpeng
     *
     * ����ʱ�䣺2014-7-8 ����4:54:29
     */
    class LayerBuilder() {
        val mLayers: MutableList<Layer>

        constructor(layer: Layer) : this() {
            mLayers.add(layer)
        }

        fun addLayer(layer: Layer): LayerBuilder {
            mLayers.add(layer)
            return this
        }

        init {
            mLayers = ArrayList()
        }
    }

    /**
     * ���л�����ģ��
     *
     * @param fileName
     */
    fun saveModel(fileName: String?) {
        try {
            val oos = ObjectOutputStream(
                FileOutputStream(fileName)
            )
            oos.writeObject(this)
            oos.flush()
            oos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         *
         */
        private const val serialVersionUID = 337920299147929932L
        private var ALPHA = 0.85
        protected const val LAMBDA = 0.0
        private var stopTrain: AtomicBoolean? = null

        /**
         * �����л�����ģ��
         *
         * @param fileName
         * @return
         */
        fun loadModel(fileName: String?): CNN? {
            try {
                val `in` = ObjectInputStream(
                    FileInputStream(
                        fileName
                    )
                )
                val cnn = `in`.readObject() as CNN
                `in`.close()
                return cnn
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            return null
        }
    }

    /**
     * ��ʼ������
     *
     * @param layerBuilder
     * �����
     * @param inputMapSize
     * ����map�Ĵ�С
     * @param classNum
     * ���ĸ�����Ҫ�����ݼ������ת��Ϊ0-classNum-1����ֵ
     */
    init {
        layers = layerBuilder.mLayers
        layerNum = layers.size
        this.batchSize = batchSize
        setup(batchSize)
        initPerator()
    }
}
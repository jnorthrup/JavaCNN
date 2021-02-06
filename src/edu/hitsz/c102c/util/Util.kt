package edu.hitsz.c102c.util

import java.lang.RuntimeException
import edu.hitsz.c102c.cnn.Layer.Size
import java.lang.StringBuilder
import kotlin.jvm.JvmStatic
import java.io.Serializable
import java.util.*

object Util {
    // ����ÿ��Ԫ��value������1-value�Ĳ���
    val one_value: Operator = object : Operator {
        /**
         *
         */
         val serialVersionUID = 3752139491940330714L
        override fun process(value: Double): Double {
            return 1 - value
        }
    }

    // digmod����
    val digmod: Operator = object : Operator {
        /**
         *
         */
         val serialVersionUID = -1952718905019847589L
        override fun process(value: Double): Double {
            return 1 / (1 + Math.pow(Math.E, -value))
        }
    }

    /**
     * ��������ӦԪ�صļӷ�����
     */
    val plus: OperatorOnTwo = object : OperatorOnTwo {
        /**
         *
         */
         val serialVersionUID = -6298144029766839945L
        override fun process(a: Double, b: Double): Double {
            return a + b
        }
    }

    /**
     * ��������ӦԪ�صĳ˷�����
     */
    var multiply: OperatorOnTwo = object : OperatorOnTwo {
        /**
         *
         */
         val serialVersionUID = -7053767821858820698L
        override fun process(a: Double, b: Double): Double {
            return a * b
        }
    }

    /**
     * ��������ӦԪ�صļ�������
     */
    var minus: OperatorOnTwo = object : OperatorOnTwo {
        /**
         *
         */
         val serialVersionUID = 7346065545555093912L
        override fun process(a: Double, b: Double): Double {
            return a - b
        }
    }

    fun printMatrix(matrix: Array<DoubleArray>) {
        for (i in matrix!!.indices) {
            var line = Arrays.toString(matrix[i])
            line = line.replace(", ".toRegex(), "\t")
            println(line)
        }
        println()
    }

    /**
     * �Ծ������180����ת,����matrix�ĸ����ϸ��ƣ������ԭ���ľ�������޸�
     *
     * @param matrix
     */
    fun rot180(matrix: Array<DoubleArray>): Array<DoubleArray> {
        var matrix = matrix
        matrix = cloneMatrix(matrix)
        val m = matrix.size
        val n: Int = matrix[0]!!.size
        // ���жԳƽ��н���
        for (i in 0 until m) {
            for (j in 0 until n / 2) {
                val tmp = matrix[i]!![j]
                matrix[i]!![j] = matrix[i]!![n - 1 - j]
                matrix[i]!![n - 1 - j] = tmp
            }
        }
        // ���жԳƽ��н���
        for (j in 0 until n) {
            for (i in 0 until m / 2) {
                val tmp = matrix[i]!![j]
                matrix[i]!![j] = matrix[m - 1 - i]!![j]
                matrix[m - 1 - i]!![j] = tmp
            }
        }
        return matrix
    }

    private val r = Random(2)

    /**
     * �����ʼ������
     *
     * @param x
     * @param y
     * @param b
     * @return
     */
    fun randomMatrix(x: Int, y: Int, b: Boolean): Array<DoubleArray> {
        val matrix = Array(x) { DoubleArray(y) }
        val tag = 1
        for (i in 0 until x) {
            for (j in 0 until y) {
                // ���ֵ��[-0.05,0.05)֮�䣬��Ȩ�س�ʼ��ֵ��С���������ڱ�������
                matrix[i][j] = (r.nextDouble() - 0.05) / 10
                //				matrix[i][j] = tag * 0.5;
//				if (b)
//					matrix[i][j] *= 1.0*(i + j + 2) / (i + 1) / (j + 1);
//				tag *= -1;
            }
        }
        // printMatrix(matrix);
        return matrix
    }

    /**
     * �����ʼ��һά����
     *
     * @param len
     * @return
     */
    fun randomArray(len: Int): DoubleArray {
        val data = DoubleArray(len)
        for (i in 0 until len) {
            // data[i] = r.nextDouble() / 10 - 0.05;
            data[i] = 0.0
        }
        return data
    }

    /**
     * ������еĳ����������ȡbatchSize��[0,size)����
     *
     * @param size
     * @param batchSize
     * @return
     */
    fun randomPerm(size: Int, batchSize: Int): IntArray {
        val set: MutableSet<Int> = HashSet()
        while (set.size < batchSize) {
            set.add(r.nextInt(size))
        }
        val randPerm = IntArray(batchSize)
        var i = 0
        for (value in set) randPerm[i++] = value
        return randPerm
    }

    /**
     * ���ƾ���
     *
     * @param matrix
     * @return
     */
    fun cloneMatrix(matrix: Array<DoubleArray>): Array<DoubleArray > {
        val m = matrix!!.size
        val n: Int = matrix[0]!!.size
        val outMatrix = Array<DoubleArray>(m) { DoubleArray(n) }
        for (i in 0 until m) {
            for (j in 0 until n) {
                outMatrix[i]!![j] = matrix[i]!![j]
            }
        }
        return outMatrix
    }

    /**
     * �Ե���������в���
     *
     * @param ma
     * @param operator
     * @return
     */
    fun matrixOp(ma: Array<DoubleArray>, operator: Operator?): Array<DoubleArray> {
        val m = ma!!.size
        val n: Int = ma[0].size
        for (i in 0 until m) {
            for (j in 0 until n) {
                ma[i]!![j] = operator!!.process(ma[i]!![j])
            }
        }
        return ma
    }

    /**
     * ����ά����ͬ�ľ����ӦԪ�ز���,�õ��Ľ������mb�У���mb[i][j] = (op_a
     * ma[i][j]) op (op_b mb[i][j])
     *
     * @param ma
     * @param mb
     * @param operatorB
     * �ڵ�mb�����ϵĲ���
     * @param operatorA
     * ��ma����Ԫ���ϵĲ���
     * @return
     */
    fun matrixOp(
        ma: Array<DoubleArray > , mb: Array<DoubleArray > ,
        operatorA: Operator?, operatorB: Operator?,
        operator: OperatorOnTwo?
    ): Array<DoubleArray > {
        val m = ma.size
        val n: Int = ma[0].size
        if (m != mb.size || n != mb[0].size) throw RuntimeException(
            "���������С��һ�� ma.length:" + ma.size
                    + "  mb.length:" + mb.size
        )
        for (i in 0 until m) {
            for (j in 0 until n) {
                var a = ma[i]!![j]
                if (operatorA != null) a = operatorA.process(a)
                var b = mb[i]!![j]
                if (operatorB != null) b = operatorB.process(b)
                mb[i]!![j] = operator!!.process(a, b)
            }
        }
        return mb
    }

    /**
     * �����ڿ˻�,�Ծ��������չ
     *
     * @param matrix
     * @param scale
     * @return
     */
    fun kronecker(matrix: Array<DoubleArray>, scale: Size ): Array<DoubleArray> {
        val m = matrix!!.size
        val n: Int = matrix[0].size
        val outMatrix = Array<DoubleArray>(m * scale.x) { DoubleArray(n * scale.y) }
        for (i in 0 until m) {
            for (j in 0 until n) {
                for (ki in i * scale.x until (i + 1) * scale.x) {
                    for (kj in j * scale.y until (j + 1) * scale.y) {
                        outMatrix[ki]!![kj] = matrix[i]!![j]
                    }
                }
            }
        }
        return outMatrix
    }

    /**
     * �Ծ�����о�ֵ��С
     *
     * @param matrix
     * @param scaleSize
     * @return
     */
    fun scaleMatrix(
        matrix: Array<DoubleArray>,
        scale: Size?
    ): Array<DoubleArray> {
        val m = matrix!!.size
        val n: Int = matrix[0].size
        val sm = m / scale!!.x
        val sn = n / scale.y
        val outMatrix = Array<DoubleArray>(sm) { DoubleArray(sn) }
        if (sm * scale.x != m || sn * scale.y != n) throw RuntimeException("scale��������matrix")
        val size = scale.x * scale.y
        for (i in 0 until sm) {
            for (j in 0 until sn) {
                var sum = 0.0
                for (si in i * scale.x until (i + 1) * scale.x) {
                    for (sj in j * scale.y until (j + 1) * scale.y) {
                        sum += matrix[si]!![sj]
                    }
                }
                outMatrix[i]!![j] = sum / size
            }
        }
        return outMatrix
    }

    /**
     * ����fullģʽ�ľ��
     *
     * @param matrix
     * @param kernel
     * @return
     */
    fun convnFull(
        matrix: Array<DoubleArray>,
        kernel: Array<DoubleArray>
    ): Array<DoubleArray> {
        val m = matrix!!.size
        val n: Int = matrix[0].size
        val km = kernel!!.size
        val kn: Int = kernel[0].size
        // ��չ����
        val extendMatrix: Array<DoubleArray>     = Array(m + 2 * (km - 1)) {
            DoubleArray(
                n + 2
                        * (kn - 1)
            )
        }
        for (i in 0 until m) {
            for (j in 0 until n) extendMatrix[i + km - 1]!![j + kn - 1] = matrix[i]!![j]
        }
        return convnValid(extendMatrix, kernel)
    }

    /**
     * ����validģʽ�ľ��
     *
     * @param matrix
     * @param kernel
     * @return
     */
    fun convnValid(
        matrix: Array<DoubleArray>,
        kernel: Array<DoubleArray>
    ): Array<DoubleArray> {
        //kernel = rot180(kernel);
        val m = matrix!!.size
        val n: Int = matrix[0].size
        val km = kernel!!.size
        val kn: Int = kernel[0].size
        // ��Ҫ�����������
        val kns = n - kn + 1
        // ��Ҫ�����������
        val kms = m - km + 1
        // �������
        val outMatrix = Array<DoubleArray>(kms) { DoubleArray(kns) }
        for (i in 0 until kms) {
            for (j in 0 until kns) {
                var sum = 0.0
                for (ki in 0 until km) {
                    for (kj in 0 until kn) sum += matrix[i + ki]!![j + kj] * kernel[ki]!![kj]
                }
                outMatrix[i]!![j] = sum
            }
        }
        return outMatrix
    }

    /**
     * ��ά����ľ��,����Ҫ�����������һά��ͬ
     *
     * @param matrix
     * @param kernel
     * @return
     */
    fun convnValid(
        matrix: Array<Array<Array<DoubleArray>>>,
        mapNoX: Int, kernel: Array<Array<Array<DoubleArray>>>, mapNoY: Int
    ): Array<DoubleArray> {
        val m = matrix.size
        val n: Int = matrix[0][mapNoX].size
        val h: Int = matrix[0][mapNoX][0].size
        val km = kernel.size
        val kn: Int = kernel[0][mapNoY].size
        val kh: Int = kernel[0][mapNoY][0].size
        val kms = m - km + 1
        val kns = n - kn + 1
        val khs = h - kh + 1
        if (matrix.size != kernel.size) throw RuntimeException("�����������ڵ�һά�ϲ�ͬ")
        // �������
        val outMatrix = Array(kms) { Array(kns) { DoubleArray(khs) } }
        for (i in 0 until kms) {
            for (j in 0 until kns) for (k in 0 until khs) {
                var sum = 0.0
                for (ki in 0 until km) {
                    for (kj in 0 until kn) for (kk in 0 until kh) {
                        sum += (matrix[i + ki][mapNoX][j + kj][k + kk]
                                * kernel[ki][mapNoY][kj][kk])
                    }
                }
                outMatrix[i][j][k] = sum
            }
        }
        return outMatrix[0]
    }

    fun sigmod(x: Double): Double {
        return 1 / (1 + Math.pow(Math.E, -x))
    }

    /**
     * �Ծ���Ԫ�����
     *
     * @param error
     * @return ע�������ͺܿ��ܻ����
     */
    fun sum(error: Array<DoubleArray>): Double   = error.fold(0.0,{acc, doubles ->  acc+ doubles.sum() })


    /**
     *
     * @param errors
     * @param y
     * @return
     */
    fun sum(errors: Array<Array<Array<DoubleArray>>>, y: Int): Array<DoubleArray> {
         val memento = errors[0][y]
        val outerSize: Int = memento.size
        val innerSize: Int = memento[0].size
        val result = Array(outerSize) { DoubleArray(innerSize) }
        for (z1 in 0 until outerSize) {
            for (z2 in 0 until innerSize) {                 var sum = 0.0

                for (x in errors.indices) sum += errors[x][y][z1] [z2]

                result[z1][z2] = sum
            }
        }
        return result
    }

    fun binaryArray2int(array: DoubleArray): Int {
        val d = IntArray(array.size)
        for (i in d.indices) {
            if (array[i] >= 0.500000001) d[i] = 1 else d[i] = 0
        }
        val s = Arrays.toString(d)
        val binary = s.substring(1, s.length - 1).replace(", ", "")
        return binary.toInt(2)
    }

    /**
     * ���Ծ��,���Խ����4���²������еľ����߲���2��
     */
    private fun testConvn() {
        var count = 1
        val m = Array<DoubleArray>(5) { DoubleArray(5) }
        for (i in m.indices) for (j in 0 until m[0].size) m[i]!![j] = count++.toDouble()
        val k = Array<DoubleArray>(3) { DoubleArray(3) }
        for (i in k.indices) for (j in 0 until k[0].size) k[i]!![j] = 1.0
        val out: Array<DoubleArray>
        // out= convnValid(m, k);
        printMatrix(m)
        out = convnFull(m, k)
        printMatrix(out)
        // System.out.println();
        // out = convnFull(m, Util.rot180(k));
        // Util.printMatrix(out);
    }

    private fun testScaleMatrix() {
        var count = 1
        val m = Array<DoubleArray>(16) { DoubleArray(16) }
        for (i in m.indices) for (j in 0 until m[0].size) m[i]!![j] = count++.toDouble()
        val out = scaleMatrix(m, Size(2, 2))
        printMatrix(m)
        printMatrix(out)
    }

    private fun testKronecker() {
        var count = 1
        val m = Array<DoubleArray>(5) { DoubleArray(5) }
        for (i in m.indices) for (j in 0 until m[0].size) m[i]!![j] = count++.toDouble()
        val out = kronecker(m, Size(2, 2))
        printMatrix(m)
        println()
        printMatrix(out)
    }

    private fun testMatrixProduct() {
        var count = 1
        val m = Array<DoubleArray>(5) { DoubleArray(5) }
        for (i in m.indices) for (j in 0 until m[0].size) m[i]!![j] = count++.toDouble()
        val k = Array<DoubleArray>(5) { DoubleArray(5) }
        for (i in k.indices) for (j in 0 until k[0].size) k[i]!![j] = j.toDouble()
        printMatrix(m)
        printMatrix(k)
        val out = matrixOp(m, k, object : Operator {
            /**
             *
             */
            /**
             *
             */
             val serialVersionUID = -680712567166604573L
            override fun process(value: Double): Double {
                return value - 1
            }
        }, object : Operator {
            /**
             *
             */
            /**
             *
             */
             val serialVersionUID = -6335660830579545544L
            override fun process(value: Double): Double {
                return -1 * value
            }
        }, multiply)
        printMatrix(out)
    }

    private fun testCloneMatrix() {
        var count = 1
        val m = Array<DoubleArray>(5) { DoubleArray(5) }
        for (i in m.indices) for (j in 0 until m[0].size) m[i]!![j] = count++.toDouble()
        val out = cloneMatrix(m)
        printMatrix(m)
        printMatrix(out)
    }

    fun testRot180() {
        val matrix = arrayOf<DoubleArray>(
            doubleArrayOf(1.0, 2.0, 3.0, 4.0),
            doubleArrayOf(4.0, 5.0, 6.0, 7.0),
            doubleArrayOf(7.0, 8.0, 9.0, 10.0)
        )
        printMatrix(matrix)
        rot180(matrix)
        println()
        printMatrix(matrix)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // new TimedTest(new TestTask() {
        //
        // @Override
        // public void process() {
        // testConvn();
        // // testScaleMatrix();
        // // testKronecker();
        // // testMatrixProduct();
        // // testCloneMatrix();
        // }
        // }, 1).test();
        // ConcurenceRunner.stop();
        println(sigmod(0.727855957917715))
        val a = 1.0
        val b = 1
        println(a == b.toDouble())
    }

    /**
     * ȡ����Ԫ�ص��±�
     *
     * @param out
     * @return
     */
    fun getMaxIndex(out: DoubleArray): Int {
        var max = out[0]
        var index = 0
        for (i in 1 until out.size) if (out[i] > max) {
            max = out[i]
            index = i
        }
        return index
    }

    fun fomart(data: DoubleArray): String {
        val sb = StringBuilder("[")
        for (each in data) sb.append(String.format("%4f,", each))
        sb.append("]")
        return sb.toString()
    }

    /**
     * �����ӦԪ�����ʱ��ÿ��Ԫ���ϵĲ���
     *
     * @author jiqunpeng
     *
     * ����ʱ�䣺2014-7-9 ����9:28:35
     */
    interface Operator : Serializable {
        fun process(value: Double): Double
    }

    interface OperatorOnTwo : Serializable {
        fun process(a: Double, b: Double): Double
    }
}
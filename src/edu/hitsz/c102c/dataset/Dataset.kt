package edu.hitsz.c102c.dataset

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
import edu.hitsz.c102c.util.TimedTest
import edu.hitsz.c102c.util.TimedTest.TestTask
import edu.hitsz.c102c.cnn.RunCNN
import edu.hitsz.c102c.util.ConcurenceRunner
import edu.hitsz.c102c.util.Util.OperatorOnTwo
import java.util.Arrays
import java.util.HashSet
import java.util.Locale
import edu.hitsz.c102c.util.TestArray
import java.io.*
import java.util.concurrent.ExecutorService
import java.lang.Runnable
import java.util.concurrent.CountDownLatch
import java.lang.InterruptedException
import java.util.concurrent.Executors
import java.util.ArrayList

class Dataset {
    // ��������
    private var records: MutableList<Record>

    // ����±�
    var lableIndex: Int
        private set
    private var maxLable = -1.0

    constructor(classIndex: Int) {
        lableIndex = classIndex
        records = ArrayList()
    }

    constructor(datas: List<DoubleArray>) : this() {
        for (data in datas) {
            append(Record(data))
        }
    }

    private constructor() {
        lableIndex = -1
        records = ArrayList()
    }

    fun size(): Int {
        return records.size
    }

    fun append(record: Record) {
        records.add(record)
    }

    /**
     * �������
     */
    fun clear() {
        records.clear()
    }

    /**
     * ���һ����¼
     *
     * @param attrs
     * ��¼������
     * @param lable
     * ��¼�����
     */
    fun append(attrs: DoubleArray, lable: Double ) {
        records.add(Record(attrs, lable))
    }

    fun iter(): Iterator<Record> {
        return records.iterator()
    }

    /**
     * ��ȡ��index����¼������
     *
     * @param index
     * @return
     */
    fun getAttrs(index: Int): DoubleArray {
        return records[index].attrs
    }

    fun getLable(index: Int): Double? {
        return records[index].getLable()
    }

    /**
     * ���ݼ�¼(ʵ��),��¼�����Ժ�������,������Ϊ��һ�л������һ�л��߿�
     *
     * @author jiqunpeng
     *
     * ����ʱ�䣺2014-6-15 ����8:03:29
     */
    inner class Record {
        /**
         * �ü�¼������
         *
         * @return
         */
        // �洢����
        var attrs: DoubleArray
            private set
        var lable: Double =0.0

        constructor(attrs: DoubleArray, lable: Double) {
            this.attrs = attrs
            this.lable = lable
        }

        constructor(data: DoubleArray) {
            if (lableIndex == -1) attrs = data else {
                lable = data[lableIndex]
                if (lable  > maxLable) maxLable = lable
                attrs = if (lableIndex == 0) Arrays.copyOfRange(data, 1, data.size) else Arrays.copyOfRange(
                    data,
                    0,
                    data.size - 1
                )
            }
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("attrs:")
            sb.append(Arrays.toString(attrs))
            sb.append("lable:")
            sb.append(lable)
            return sb.toString()
        }

        /**
         * �ü�¼�����
         *
         * @return
         */
        fun getLable(): Double? {
            return if (lableIndex == -1) null else lable
        }

        /**
         * �������ж����Ʊ���
         *
         * @param n
         * @return
         */
        fun getEncodeTarget(n: Int): IntArray {
            val binary = Integer.toBinaryString(lable!!.toInt())
            val bytes = binary.toByteArray()
            val encode = IntArray(n)
            var j = n
            for (i in bytes.indices.reversed()) encode[--j] = (bytes[i].toInt()  - '0'.toInt())
            return encode
        }

        fun getDoubleEncodeTarget(n: Int): DoubleArray {
            val binary = Integer.toBinaryString(lable.toInt())
            val bytes = binary.toByteArray()
            val encode = DoubleArray(n)
            var j = n
            for (i in bytes.indices.reversed()) encode[--j] = (bytes[i].toInt() - '0'.toInt()).toDouble()
            return encode
        }
    }

    /**
     * ��ȡ��index����¼
     *
     * @param index
     * @return
     */
    fun getRecord(index: Int): Record {
        return records[index]
    }

    companion object {
        /**
         * �������ݼ�
         *
         * @param filePath
         * �ļ�����·��
         * @param tag
         * �ֶηָ���
         * @param lableIndex
         * ����±꣬��0��ʼ
         * @return
         */
        fun load(filePath: String?, tag: String?, lableIndex: Int): Dataset? {
            val dataset = Dataset()
            dataset.lableIndex = lableIndex
            val file = File(filePath)
            try {
                val `in` = BufferedReader(FileReader(file))
                lateinit var line: String
                while (`in`.readLine()?.also { line = it } != null) {
                    val datas = line.split(tag!!).toTypedArray()
                    if (datas.size != 0) {
                        val data = DoubleArray(datas.size)
                        for (i in datas.indices) data[i] = datas[i].toDouble()
                        val record = dataset.Record(data)
                        dataset.append(record)
                    }
                }
                `in`.close()
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
            println("��������:" + dataset.size())
            return dataset
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val d = Dataset()
            d.lableIndex = 10
            val r = d.Record(doubleArrayOf(3.0, 2.0, 2.0, 5.0, 4.0, 5.0, 3.0, 11.0, 3.0, 12.0, 1.0))
            val encode = r.getEncodeTarget(4)
            println(r.lable)
            println(Arrays.toString(encode))
        }
    }
}
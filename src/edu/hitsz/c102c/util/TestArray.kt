package edu.hitsz.c102c.util

import kotlin.jvm.JvmStatic
import java.util.Locale

/**
 * ����Ԫ��ֱ�ӷ���������ͨ���������������Ч�ʣ� ���ۣ�������ʽ���ʲ�û�н����ٶ�
 *
 * @author jiqunpeng
 *
 * ����ʱ�䣺2014-7-9 ����3:18:30
 */
class TestArray(m: Int, n: Int) {
    var data: Array<DoubleArray>
    operator fun set(x: Int, y: Int, value: Double) {
        data[x][y] = value
    }

    private fun useOrigin() {
        for (i in data.indices) for (j in 0 until data[0].size) data[i][j] = (i * j).toDouble()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val a = "aAdfa��_"
            println(a.toUpperCase(Locale.CHINA))
            val d = arrayOfNulls<DoubleArray>(3)
            //		d[0] = new double[] { 1,2,3 };
//		d[1] = new double[] { 3,4,5,6 };
            println(d[1]!![3])
            val t = TestArray(10000, 1000)
            TimedTest({
                for (i in t.data.indices)
                    for (j in 0 until t.data[0].size)
                        t.set(i, j, (i * j).toDouble())
            } as TimedTest.TestTask, 1).test()
        }
    }

    init {
        data = Array(m) { DoubleArray(n) }
    }
}
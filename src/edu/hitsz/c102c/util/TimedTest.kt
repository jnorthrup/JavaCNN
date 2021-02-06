package edu.hitsz.c102c.util

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
import edu.hitsz.c102c.util.TimedTest
import edu.hitsz.c102c.util.TimedTest.TestTask
import edu.hitsz.c102c.cnn.RunCNN
import edu.hitsz.c102c.util.ConcurenceRunner
import java.io.PrintStream
import edu.hitsz.c102c.util.Util.OperatorOnTwo
import java.util.Arrays
import java.util.HashSet
import java.util.Locale
import edu.hitsz.c102c.util.TestArray
import java.util.concurrent.ExecutorService
import java.lang.Runnable
import java.util.concurrent.CountDownLatch
import java.lang.InterruptedException
import java.util.concurrent.Executors
import java.io.BufferedReader
import java.io.FileReader

/**
 * ��ʱ�Ĳ��Թ���
 *
 * @author jiqunpeng
 *
 * ����ʱ�䣺2014-7-8 ����8:21:56
 */public
class TimedTest(private val task: TestTask, private val repeat: Int) {
 public    interface TestTask {
        fun process()
    }

    fun test() {
        val t = System.currentTimeMillis()
        for (i in 0 until repeat) {
            task.process()
        }
        val cost = (System.currentTimeMillis() - t) / 1000.0
        Log.i("cost ", cost.toString() + "s")
    }
}
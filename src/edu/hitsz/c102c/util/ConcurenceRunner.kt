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
 * �������й���
 *
 * @author jiqunpeng
 *
 * ����ʱ�䣺2014-6-16 ����3:33:41
 */
object ConcurenceRunner {
    private var exec: ExecutorService? = null
    var cpuNum = 0
    fun run(task: Runnable?) {
        exec!!.execute(task)
    }

    fun stop() {
        exec!!.shutdown()
    }

    // public abstract static class Task implements
    // Runnable {
    // int start, end;
    //
    // public Task(int start, int end) {
    // this.start = start;
    // this.end = end;
    // // Log.i("new Task",
    // // "start "+start+" end "+end);
    // }
    //
    // @Override
    // public void run() {
    // process(start, end);
    // }
    //
    // public abstract void process(int start, int
    // end);
    //
    // }
    abstract class TaskManager(private val workLength: Int) {
        fun start() {
            val runCpu = if (cpuNum < workLength) cpuNum else 1
            // ��Ƭ��������ȡ��
            val gate = CountDownLatch(runCpu)
            val fregLength = (workLength + runCpu - 1) / runCpu
            for (cpu in 0 until runCpu) {
                val start = cpu * fregLength
                val tmp = (cpu + 1) * fregLength
                val end = if (tmp <= workLength) tmp else workLength
                val task = Runnable {
                    process(start, end)
                    gate.countDown()
                }
                run(task)
            }
            try { // �ȴ������߳�����
                gate.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                throw RuntimeException(e)
            }
        }

        abstract fun process(start: Int, end: Int)
    }

    init {
        cpuNum = Runtime.getRuntime().availableProcessors()
        // cpuNum = 1;
        println("cpuNum:" + cpuNum)
        exec = Executors.newFixedThreadPool(cpuNum)
    }
}
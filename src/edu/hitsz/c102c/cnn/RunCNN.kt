package edu.hitsz.c102c.cnn

import edu.hitsz.c102c.cnn.CNN.LayerBuilder
import edu.hitsz.c102c.cnn.Layer.Size
import edu.hitsz.c102c.dataset.Dataset
import edu.hitsz.c102c.util.ConcurenceRunner
import edu.hitsz.c102c.util.TimedTest
import edu.hitsz.c102c.util.TimedTest.TestTask
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

object RunCNN {
    fun runCnn() {
        //斐膘珨跺橙儅朸冪厙釐
        val builder = LayerBuilder()
        builder.addLayer(Layer.Companion.buildInputLayer(Size(28, 28)))
        builder.addLayer(Layer.Companion.buildConvLayer(6, Size(5, 5)))
        builder.addLayer(Layer.Companion.buildSampLayer(Size(2, 2)))
        builder.addLayer(Layer.Companion.buildConvLayer(12, Size(5, 5)))
        builder.addLayer(Layer.Companion.buildSampLayer(Size(2, 2)))
        builder.addLayer(Layer.Companion.buildOutputLayer(10))
        val cnn = CNN(builder, 50)

        //絳�輮�擂摩
        val fileName = "dataset/train.format"
        var dataset: Dataset? = Dataset.Companion.load(fileName, ",", 784)
        cnn.train(dataset, 3) //
        val modelName = "model/model.cnn"
        try {
            Files.createDirectories(Paths.get("model"))
            cnn.saveModel(modelName)
            dataset!!.clear()
            dataset = null

            //啎聆
            // CNN cnn = CNN.loadModel(modelName);
            val testset: Dataset? = Dataset.Companion.load("dataset/test.format", ",", -1)
            cnn.predict(testset, "dataset/test.predict")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {

        TimedTest(object : TestTask {
            override fun process() {
                runCnn()

            }

        }, 1).test()
        ConcurenceRunner.stop()
    }

}
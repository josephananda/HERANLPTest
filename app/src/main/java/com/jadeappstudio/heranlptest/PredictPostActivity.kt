package com.jadeappstudio.heranlptest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.share424.sastrawi.Stemmer.StemmerFactory
import com.share424.sastrawi.StopWordRemover.StopWordRemoverFactory
import kotlinx.android.synthetic.main.activity_predict_post.*
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Interpreter.Options
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class PredictPostActivity : AppCompatActivity() {
    //Name of TFLite model (in /assets folder)
    private val modelAssetPath = "modelpost.tflite"

    private var tfLiteInterpreter: Interpreter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predict_post)

        val stemmer = StemmerFactory(applicationContext).create()
        val stopwordRemover = StopWordRemoverFactory(applicationContext).create()

        //Instantiate the classifier
        val classifier = Classifier(this, "word_dict_post.json")
        //Instantiate the TFLiteInterpreter
        val options = Options()
        tfLiteInterpreter = Interpreter(loadModelFile(), options)

        classifier.processVocab()

        btnPredict.setOnClickListener {
            val stemmed = stemmer.stem(etTweetText.editText!!.text.toString().toLowerCase(Locale.ROOT).trim())
            val stopword = stopwordRemover.remove(stemmed)
            if (stopword.isNotEmpty()) {
                tvStemmed.text = "$stopword"
                val tokenizedMessage = classifier.tokenize(stopword)
                val paddedMessage = classifier.padSequence(tokenizedMessage)
                val results = classifySequence(paddedMessage)

                val highest = results.maxOrNull()
                val idxLabel = results.indexOfFirst { it == highest!! }
                val finalLabel = findLabel(idxLabel)
                tvLabelProbability.text =
                    "Lokasi Kerja: ${results[0]}\nLokasi Pendidikan: ${results[1]}\nLokasi Privat: ${results[2]}\nLokasi Publik: ${results[3]}\nNetral: ${results[4]}\nSiber: ${results[5]}"
                tvFinalLabel.text = "Highest: $highest\nFinal Label: $finalLabel"
            }
        }
    }

    /**
     * A method to find the label of the predicted text
     * @param idx Index of the highest label
     * @return String of the label
     */
    private fun findLabel(idx: Int): String {
        var label = ""
        when (idx) {
            0 -> label = "Lokasi Kerja"
            1 -> label = "Lokasi Pendidikan"
            2 -> label = "Lokasi Privat"
            3 -> label = "Lokasi Publik"
            4 -> label = "Netral"
            5 -> label = "Siber"
        }
        return label
    }

    /**
     * A method to load the TFLite Model
     * @throws IOException
     * @return MappedByteBuffer
     */
    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = assets.openFd(modelAssetPath)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * A method to classify and generate probability of every class of the predicted text
     * @param sequence array of tokenized text
     * @return FloatArray
     */
    //Perform inference, given the input sequence
    private fun classifySequence(sequence: IntArray): FloatArray {
        //Input shape -> (1 , inputPaddedSequenceLength) -> (1,50)
        val inputs: Array<FloatArray> = arrayOf(sequence.map { it.toFloat() }.toFloatArray())
        //Output shape -> (1,6) (as numClasses = 6)
        val outputs: Array<FloatArray> = arrayOf(FloatArray(6))
        tfLiteInterpreter?.run(inputs, outputs)
        return outputs[0]
    }
}
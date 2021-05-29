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

class PredictLaporanActivity : AppCompatActivity() {
    //Name of TFLite model (in /assets folder)
    private val modelAssetPath = "modellaporan.tflite"

    private var tfLiteInterpreter: Interpreter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predict_laporan)

        val stemmer = StemmerFactory(applicationContext).create()
        val stopwordRemover = StopWordRemoverFactory(applicationContext).create()

        //Instantiate the classifier
        val classifier = Classifier(this, "word_dict_laporan.json")
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
                    "Eksploitasi: ${results[0]}\nEksploitasi Seksual: ${results[1]}\nKekerasan Fisik: ${results[2]}\nKekerasan Lainnya: ${results[3]}\nKekerasan Psikis: ${results[4]}\nKekerasan Seksual: ${results[5]}\nPenelantaran: ${results[6]}"
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
            0 -> label = "Eksploitasi"
            1 -> label = "Eksploitasi Seksual"
            2 -> label = "Kekerasan Fisik"
            3 -> label = "Kekerasan Lainnya"
            4 -> label = "Kekerasan Psikis"
            5 -> label = "Kekerasan Seksual"
            6 -> label = "Penelantaran"
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
        //Input shape -> (1, inputPaddedSequenceLength) -> (1,50)
        val inputs: Array<FloatArray> = arrayOf(sequence.map { it.toFloat() }.toFloatArray())
        //Output shape -> (1,7) (as numClasses = 7)
        val outputs: Array<FloatArray> = arrayOf(FloatArray(7))
        tfLiteInterpreter?.run(inputs, outputs)
        return outputs[0]
    }
}
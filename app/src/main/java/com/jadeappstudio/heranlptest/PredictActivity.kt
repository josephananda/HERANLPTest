package com.jadeappstudio.heranlptest

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_predict.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class PredictActivity : AppCompatActivity() {
    // Name of TFLite model ( in /assets folder ).
    private val MODEL_ASSETS_PATH = "model.tflite"

    // Max Length of input sequence. The input shape for the model will be ( None , INPUT_MAXLEN ).
    private val INPUT_MAXLEN = 45

    private var tfLiteInterpreter : Interpreter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predict)

        // Init the classifier.
        val classifier = Classifier( this , "word_dict.json" , INPUT_MAXLEN )
        // Init TFLiteInterpreter
        tfLiteInterpreter = Interpreter( loadModelFile() )

        val progressDialog = ProgressDialog( this )
        progressDialog.setMessage( "Parsing word_dict.json ..." )
        progressDialog.setCancelable( false )
        progressDialog.show()
        classifier.processVocab( object: Classifier.VocabCallback {
            override fun onVocabProcessed() {
                // Processing done, dismiss the progressDialog.
                progressDialog.dismiss()
            }
        })

        btnPredict.setOnClickListener {

            val tokenizedMessage = classifier.tokenize(etTweetText.editText!!.text.toString().toLowerCase().trim())
            //var paddedMessage = classifier.padSequence(tokenizedMessage)
            val results = classifySequence(tokenizedMessage)

            val highest = results.maxOrNull()
            val idxLabel = results.indexOfFirst { it == highest!! }
            val finalLabel = findLabel(idxLabel)

            tvLabelProbability.text = "Cyber: ${results[0]}\nEducational Places: ${results[1]}\nNeutral: ${results[2]}\nPrivate Places: ${results[3]}\nPublic Places: ${results[4]}\nWorkplaces: ${results[5]}"
            tvFinalLabel.text = "Highest: $highest\nFinal Label: $finalLabel"
        }
    }

    /**
     * A method to find the label of the predicted text
     * @param idx Index of the highest label
     * @return String of the label
     */
    fun findLabel(idx: Int): String {
        var label = ""
        when(idx){
            0 -> label = "Cyber"
            1 -> label = "Educational Places"
            2 -> label = "Neutral"
            3 -> label = "Private Places"
            4 -> label = "Public Places"
            5 -> label = "Work Places"
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
        val assetFileDescriptor = assets.openFd(MODEL_ASSETS_PATH)
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
    // Perform inference, given the input sequence.
    private fun classifySequence (sequence : IntArray ): FloatArray {
        // Input shape -> ( 1 , INPUT_MAXLEN )
        val inputs : Array<FloatArray> = arrayOf( sequence.map { it.toFloat() }.toFloatArray() )
        // Output shape -> ( 1 , 6 ) ( as numClasses = 6 )
        val outputs : Array<FloatArray> = arrayOf( FloatArray( 6 ) )
        tfLiteInterpreter?.run( inputs , outputs )
        return outputs[0]
    }
}
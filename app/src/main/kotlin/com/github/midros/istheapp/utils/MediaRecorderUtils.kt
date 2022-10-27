package com.github.midros.istheapp.utils

import android.media.MediaRecorder
import android.util.Log
import com.pawegio.kandroid.e
import android.view.SurfaceHolder
import android.media.CamcorderProfile







/**
 * Created by luis rafael on 21/03/19.
 */
class MediaRecorderUtils(private val errorAction: () -> Unit) : MediaRecorder() {
//graba llamada
var holder: SurfaceHolder? = null
    fun startRecording2(audioSource: Int,fileName:String?){
        try {
           /* setAudioSource(audioSource)
            setOutputFormat(OutputFormat.THREE_GPP)
            setAudioEncoder(AudioEncoder.AMR_NB)
            setOutputFile(fileName)
            Log.i("startRecording",  "startRecording" )
            val errorListener = OnErrorListener { _, _, _ -> errorAction() }
            setOnErrorListener(errorListener)*/
            setAudioSource(AudioSource.VOICE_CALL  ) //.DEFAULT
            setVideoSource(VideoSource.DEFAULT)

            val cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
            setProfile(cpHigh)
            setOutputFile(fileName)
            setMaxDuration(50000) // 50 seconds
            setMaxFileSize(5000000) // Approximately 5 megabytes

            prepare()
            start()
        } catch (er: Throwable) {
            e(Consts.TAG, er.message.toString())
            Log.i("error",  er.message.toString())
            errorAction()
        }
    }
    fun startRecording(audioSource: Int,fileName:String?){
        try {
            setAudioSource(audioSource)
            setOutputFormat(OutputFormat.THREE_GPP)
            setAudioEncoder(AudioEncoder.AMR_NB)
            setOutputFile(fileName)
            val errorListener = OnErrorListener { _, _, _ -> errorAction() }
            setOnErrorListener(errorListener)
       //  setPreviewDisplay(holder.getSurface());
            prepare()
            start()
            Log.i("startRecording2",  "startRecording2" )
        } catch (er: Throwable) {
            e(Consts.TAG, er.message.toString())
            Log.i("error",  er.message.toString())
            errorAction()
        }
    }
    fun stopRecording(sendFile : () -> Unit){
        try {
            stop()
            sendFile()
        } catch (e: Throwable) {
//            sendFile()
            Log.i("error",  e.message.toString())
            e(Consts.TAG, e.message.toString())
            errorAction()
        }
    }

}
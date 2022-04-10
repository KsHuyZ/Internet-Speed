package com.example.speedtestapp3

import android.app.Activity
import android.util.Log
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError

class InternetSpeedBuilder(var activity: Activity) {

    private var countTestSpeed = 0
    private var LIMIT = 3
    lateinit var url: String
    lateinit var javaListener: OnEventInternetSpeedListener
    private lateinit var progressModel: ProgressionModel
    lateinit var onDownloadProgressListener: () -> Unit
    lateinit var onUploadProgressListener: () -> Unit
    lateinit var onTotalProgressListener: () -> Unit

    fun start(url: String, limitCount: Int) {
        this.url = url
        this.LIMIT = limitCount
        startTestDownload()
    }

    fun stop() {

    }

    fun setOnEventInternetSpeedListener(javaListener: OnEventInternetSpeedListener) {
        this.javaListener = javaListener
    }

    fun setOnEventInternetSpeedListener(onDownloadProgress: () -> Unit, onUploadProgress: () -> Unit, onTotalProgress: () -> Unit) {
        this.onDownloadProgressListener = onDownloadProgress
        this.onUploadProgressListener = onUploadProgress
        this.onTotalProgressListener = onTotalProgress
    }

    private fun startTestDownload() {
        progressModel = ProgressionModel()
        DownloadTest().execute()
    }

    private fun startTestUpload() {
        UploadTest().execute()

    }

    interface OnEventInternetSpeedListener {
        fun onDownloadProgress(count: Int, progressModel: ProgressionModel)
        fun onUploadProgress(count: Int, progressModel: ProgressionModel)
        fun onTotalProgress(count: Int, progressModel: ProgressionModel)
    }

    inner class DownloadTest : BackgroundTask(activity){
        override fun doInBackground() {

            val speedTestSocket = SpeedTestSocket()

            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {

                override fun onCompletion(report: SpeedTestReport) {
                    // called when download/upload is finished
                    Log.v("speedtest Download $countTestSpeed", "[COMPLETED] rate in octet/s : ${report.transferRateOctet}")
                    Log.v("speedtest Download $countTestSpeed" , "[COMPLETED] rate in bit/s   : ${report.transferRateBit}")

                    startTestUpload()
                }

                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    // called when a download/upload error occur
                }

                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    // called to notify download/upload progress
                    Log.v("speedtest Download $countTestSpeed", "[PROGRESS] progress : $percent%")
                    Log.v("speedtest Download $countTestSpeed", "[PROGRESS] rate in octet/s : ${report.transferRateOctet}")
                    Log.v("speedtest Download $countTestSpeed", "[PROGRESS] rate in bit/s   : ${report.transferRateBit}")

                    progressModel.progressTotal = percent / 2
                    progressModel.progressDownload = percent
                    progressModel.downloadSpeed = report.transferRateBit

                    activity.runOnUiThread { onPostExecute() }

                }
            })
            speedTestSocket.startDownload(url)
        }

        override fun onPostExecute() {
            javaListener.onDownloadProgress(countTestSpeed, progressModel)
            javaListener.onTotalProgress(countTestSpeed, progressModel)
        }

    }

    inner class UploadTest: BackgroundTask(activity){
        override fun doInBackground() {
            val speedTestSocket = SpeedTestSocket()

            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {

                override fun onCompletion(report: SpeedTestReport) {
                    // called when download/upload is finished
                    Log.v("speedtest Upload $countTestSpeed", "[COMPLETED] rate in octet/s : ${report.transferRateOctet}")
                    Log.v("speedtest Upload $countTestSpeed", "[COMPLETED] rate in bit/s   : ${report.transferRateBit}")

                    countTestSpeed++
                    if (countTestSpeed < LIMIT) {
                        startTestDownload()
                    }
                }

                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    // called when a download/upload error occur
                }

                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    // called to notify download/upload progress
                    Log.v("speedtest Upload $countTestSpeed", "[PROGRESS] progress : $percent%")
                    Log.v("speedtest Upload $countTestSpeed", "[PROGRESS] rate in octet/s : ${report.transferRateOctet}")
                    Log.v("speedtest Upload $countTestSpeed", "[PROGRESS] rate in bit/s   : ${report.transferRateBit}")

                    progressModel.progressTotal = percent / 2 + 50
                    progressModel.progressUpload = percent
                    progressModel.uploadSpeed= report.transferRateBit

                    activity.runOnUiThread{
                        onPostExecute()
                    }
                }
            })
            speedTestSocket.startDownload(url)
        }

        override fun onPostExecute() {
            if (countTestSpeed < LIMIT) {
                javaListener.onUploadProgress(countTestSpeed, progressModel)
                javaListener.onTotalProgress(countTestSpeed, progressModel)
            }
        }
    }
}
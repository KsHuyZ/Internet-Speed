package com.example.speedtestapp3

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.math.BigDecimal
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    lateinit var barImage: ImageView
    lateinit var downloadSpeed: TextView
    lateinit var uploadSpeed: TextView
    lateinit var totalSpeed: TextView

    var position = 0
    var lastPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        barImage = findViewById(R.id.barImageView)
        downloadSpeed = findViewById(R.id.download_speed)
        uploadSpeed = findViewById(R.id.upload_speed)
        totalSpeed = findViewById(R.id.total)

        val builder = InternetSpeedBuilder(this)
        builder.setOnEventInternetSpeedListener(object:
            InternetSpeedBuilder.OnEventInternetSpeedListener{
            @SuppressLint("SetTextI18n")
            override fun onDownloadProgress(count: Int, progressModel: ProgressionModel) {
                val bigDecimal = BigDecimal("" + progressModel.downloadSpeed)
                val finalDownload = (bigDecimal.toLong() / 1000000).toFloat()

                val bd: BigDecimal = progressModel.downloadSpeed
                val d = bd.toDouble()

                position = getPositionByRate(finalDownload)

                runOnUiThread{
                    val rotateAnimation = RotateAnimation(
                        lastPosition.toFloat(),
                        position.toFloat(),
                        Animation.RELATIVE_TO_SELF,
                        0.5F,
                        Animation.RELATIVE_TO_SELF,
                        0.5F
                    )
                    rotateAnimation.interpolator = LinearInterpolator()
                    rotateAnimation.duration = 1000
                    barImage.startAnimation(rotateAnimation)
                    downloadSpeed.text = formatFileSize(d)
                }
                lastPosition = position
            }

            @SuppressLint("SetTextI18n")
            override fun onUploadProgress(count: Int, progressModel: ProgressionModel) {
                val bigDecimal = BigDecimal("" + progressModel.uploadSpeed)
                val finalDownload = (bigDecimal.toLong() / 1000000).toFloat()

                val bd: BigDecimal = progressModel.uploadSpeed
                val d = bd.toDouble()

                position = getPositionByRate(finalDownload)

                runOnUiThread {
                    val rotateAnimation = RotateAnimation(
                        lastPosition.toFloat(),
                        position.toFloat(),
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                    )
                    rotateAnimation.interpolator = LinearInterpolator()
                    rotateAnimation.duration = 1000
                    barImage.startAnimation(rotateAnimation)
                    uploadSpeed.text = formatFileSize(d)
                }
                lastPosition = position
            }

            @SuppressLint("SetTextI18n")
            override fun onTotalProgress(count: Int, progressModel: ProgressionModel) {
                val downloadDecimal: BigDecimal = progressModel.downloadSpeed
                val downloadFinal = downloadDecimal.toDouble()

                val uploadDecimal: BigDecimal = progressModel.uploadSpeed
                val uploadFinal = uploadDecimal.toDouble()
                val totalSpeedCount = (downloadFinal + uploadFinal) / 2

                val finalDownload = (downloadDecimal.toLong() / 1000000).toFloat()
                val finalUpload = (uploadDecimal.toLong() / 1000000).toFloat()
                val totalSpeedTest = (finalDownload + finalUpload) / 2

                position = getPositionByRate(totalSpeedTest)

                runOnUiThread {
                    barImage.rotation = position.toFloat()
                    totalSpeed.text =   formatFileSize(totalSpeedCount)
                }

                lastPosition = position
            }
        })

        val btn: Button = findViewById(R.id.rotate_button)
        btn.setOnClickListener(){
            builder.start("http://www.speedtest.net/mini.php", 1)
        }
    }

    fun formatFileSize(size:Double): String {
        val hrSize:String
        val b:Double = size
        val k:Double = size/1024.0
        val m:Double = (size/1024.0)/1024.0
        val g:Double = ((size/1024.0)/1024.0)/1024.0
        val t:Double = (((size/1024.0)/1024.0)/1024.0)/1024.0

        val dec = DecimalFormat("0.00")

        when {
            t>1 -> {
                hrSize = dec.format(t)
            }
            g>1 -> {
                hrSize = dec.format(g)
            }
            m>1 -> {
                hrSize = dec.format(m).plus(" mb/s")
            }
            k>1 -> {
                hrSize = dec.format(k).plus(" kb/s")
            }
            else -> {
                hrSize = dec.format(b).plus(" b/s")
            }
        }
        return hrSize
    }

    fun getPositionByRate(rate: Float): Int{
        when {
            rate <= 1 -> {
                return (rate*30).toInt()
            }
            rate <= 10 -> {
                return (rate*6+30).toInt()
            }
            rate <= 30 -> {
                return (((rate - 10) * 3) + 90).toInt()
            }
            rate <= 50 -> {
                return (((rate - 30) * 1.5) + 150).toInt()
            }
            rate <= 100 -> {
                return (((rate - 50) * 1.2) + 180).toInt()
            }
            else -> return 0
        }
    }
}
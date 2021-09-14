package com.example.face

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    lateinit var camera_device: CameraDevice
        fun iscamera_deviceInitialzed() = ::camera_device.isInitialized
    lateinit var texture_view_surface: Surface
    lateinit var image_reader_surface: Surface
    lateinit var capture_session: CameraCaptureSession
    lateinit var image_reader: ImageReader

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textureView = findViewById<TextureView>(R.id.textureView)
        textureView.surfaceTextureListener =
            object : TextureView.SurfaceTextureListener{
                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {

                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    return false
                }

                //表示摄像头进入可用状态

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    texture_view_surface = Surface(textureView.surfaceTexture)
                    open_camera()
                }

            }
        val take_photo_buutton = findViewById<Button>(R.id.button)
        take_photo_buutton.setOnClickListener {
            take_photo()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun take_photo(){
        if (iscamera_deviceInitialzed()){

        }
        val request_builder = camera_device.createCaptureRequest(
            CameraDevice.TEMPLATE_STILL_CAPTURE
        )
        request_builder.addTarget(image_reader_surface)
        val request = request_builder.build()
        capture_session.capture(request,null,null)
    }

    val image_available = ImageReader.OnImageAvailableListener {
        Log.i("ccccc","got a image")
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun open_camera(){

        image_reader = ImageReader.newInstance(200,200,
            ImageFormat.JPEG,2)
        image_reader_surface = image_reader.surface
        image_reader.setOnImageAvailableListener(image_available,null)

        val camera_manager = getSystemService(
            Context.CAMERA_SERVICE
        ) as CameraManager


        //申请权限
//        requestPermissions(arrayOf(Manifest.permission.CAMERA),11)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        //第一个参数为摄像头id，这里选择id为0通常是后置摄像头
        camera_manager.openCamera(camera_manager.cameraIdList[0],
            camera_state_callback,null)
    }

    val camera_state_callback : CameraDevice.StateCallback =
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : CameraDevice.StateCallback(){
            override fun onOpened(camera: CameraDevice) {
                //保存摄像对象
                camera_device = camera
                //参数：图片数据流向的终点，
                camera_device.createCaptureSession(
                    listOf(image_reader_surface,texture_view_surface),
                    session_state_callback,null

                )
            }

            override fun onDisconnected(camera: CameraDevice) {
            }

            override fun onError(camera: CameraDevice, error: Int) {
            }

        }
    val session_state_callback =
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : CameraCaptureSession.StateCallback(){
            override fun onConfigured(session: CameraCaptureSession) {
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                capture_session = session
                //实现预览
                val request_builder = camera_device.createCaptureRequest(
                    CameraDevice.TEMPLATE_PREVIEW
                )
                request_builder.addTarget(texture_view_surface)
                val request = request_builder.build()
                capture_session.setRepeatingRequest(request,null,null)
            }

        }
}
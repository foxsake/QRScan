package alday.paa.qrscan

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.net.Socket
import java.io.DataOutputStream
import android.content.Intent
import android.os.Vibrator
import android.os.Handler
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat

import kotlinx.android.synthetic.main.activity_main.scannerView
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private var flashOn: Boolean = false
    private var doubleBack: Boolean = false
    private val INTRO_REQUEST_CODE = 4
    private val PERMISSIONS_REQUEST_CAMERA = 44

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        requestCameraPermission()

        scannerView.setOnClickListener { scannerView.resumeCameraPreview(this) }
        fab.setOnClickListener {
            scannerView.flash = !flashOn
            flashOn = !flashOn
            if(flashOn)
                fab.setImageDrawable(
                        ContextCompat.getDrawable(fab.context, R.drawable.ic_flash_off))
            else
                fab.setImageDrawable(
                        ContextCompat.getDrawable(fab.context, R.drawable.ic_flash_on))
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if(prefs.getBoolean("first_run", true)){
            val intent: Intent = Intent(this, MainIntroActivity::class.java)
            startActivityForResult(intent, INTRO_REQUEST_CODE)
        }
    }

    fun requestCameraPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    alert(getString(R.string.text_runtime_request_camera_permission)) {
                        yesButton { ActivityCompat.requestPermissions(
                                this@MainActivity, arrayOf(Manifest.permission.CAMERA),
                                PERMISSIONS_REQUEST_CAMERA) }
                        noButton { requestCameraPermission() }
                    }.show()
                } else {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.CAMERA),
                            PERMISSIONS_REQUEST_CAMERA)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSIONS_REQUEST_CAMERA && grantResults.isNotEmpty()
                && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            requestCameraPermission()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        scannerView.setResultHandler(this)
        scannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }

    override fun onBackPressed() {
        if(doubleBack){
            super.onBackPressed()
            return
        }
        scannerView.resumeCameraPreview(this)
        doubleBack = true
        toast(getString(R.string.text_back_to_exit))
        Handler().postDelayed({ doubleBack = false }, 2000)
    }

    override fun handleResult(result: Result) {
        val prefs = Prefs(this)
        val mText: String = result.text
        Log.v("QRScan", mText)
        Log.v("QRScan", result.barcodeFormat.toString())

        if(prefs.vibrate) {
            val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(100)
        }

        doAsync {
            var ip: String = getString(R.string.text_default_ip)
            if(prefs.wireless)
                ip = prefs.ip

            val socket : Socket = Socket(ip, prefs.port)
            val os : DataOutputStream = DataOutputStream(socket.getOutputStream())
            os.writeBytes(mText)
            os.close()
            socket.close()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == INTRO_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit().putBoolean("first_run", false).apply()
            }else{
                finish()
            }
        }
    }

    class Prefs (context: Context){
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)!!
        val vibrate = prefs.getBoolean("vibrate_switch", true)
        val wireless = prefs.getBoolean("wireless_switch", false)
        val ip = prefs.getString("ip_text", context.getString(R.string.text_default_ip))!!
        val port = Integer.parseInt(
                prefs.getString("port_text", context.getString(R.string.text_default_port)))
    }
}

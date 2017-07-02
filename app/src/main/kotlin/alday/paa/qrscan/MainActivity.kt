package alday.paa.qrscan

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.net.Socket
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import android.os.Vibrator
import android.content.Context
import android.os.Handler
import android.support.v4.content.ContextCompat

import kotlinx.android.synthetic.main.activity_main.scannerView
import kotlinx.android.synthetic.main.activity_main.fab

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private var flashOn: Boolean = false
    private var doubleBack: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scannerView.setOnClickListener { scannerView.resumeCameraPreview(this) }
        fab.setOnClickListener {
            scannerView.setFlash(!flashOn)
            flashOn = !flashOn
            if(flashOn)
                fab.setImageDrawable(
                        ContextCompat.getDrawable(fab.getContext(), R.drawable.ic_flash_off))
            else
                fab.setImageDrawable(
                        ContextCompat.getDrawable(fab.getContext(), R.drawable.ic_flash_on))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        //TODO add history
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
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
            super.onBackPressed();
            return;
        }
        var vv :ZXingScannerView = scannerView
        scannerView.resumeCameraPreview(this)
        doubleBack = true
        toast("Press back again to exit")
        Handler().postDelayed(object : Runnable {
            override fun run() {
                doubleBack = false
            }
        }, 2000)
    }

    override fun handleResult(result: Result) {
        var mText: String = result.text
        Log.v("QRScan", mText)
        Log.v("QRScan", result.barcodeFormat.toString())
        var vibrator : Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(100)

        doAsync {
            var socket : Socket = Socket("127.0.0.1", 59900)
            var os : java.io.DataOutputStream = java.io.DataOutputStream(socket.getOutputStream())
            os.writeBytes(mText)
            os.close();
        }
    }
}

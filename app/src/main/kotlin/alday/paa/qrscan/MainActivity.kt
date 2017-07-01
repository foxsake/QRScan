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
import android.os.Vibrator
import android.content.Context
import android.support.v4.content.ContextCompat

import kotlinx.android.synthetic.main.content_main.scannerView
import kotlinx.android.synthetic.main.activity_main.fab

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private var mText: String = ""
    private var flashOn: Boolean = false

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
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        //TODO add history
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

    override fun handleResult(result: Result) {
        mText = result.text
        Log.v("QRScan", mText)
        Log.v("QRScan", result.barcodeFormat.toString())
        var vibrator : Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(100)

        doAsync {
            var socket : Socket = Socket("127.0.0.1", 59900)
            var bos : java.io.BufferedOutputStream =
                    java.io.BufferedOutputStream(socket.getOutputStream())
            bos.write(mText.toByteArray())
            bos.flush()
            bos.close()
        }
    }
}

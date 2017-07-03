package alday.paa.qrscan

import android.Manifest
import android.os.Bundle
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide


/**
 * Created by paul on 7/3/17.
 */
class MainIntroActivity: IntroActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(SimpleSlide.Builder()
                .title("QRScanner")
                .description("Scan Bar Codes and QR-Codes.")
                .image(R.mipmap.ic_launcher)
                .background(R.color.blue)
                .backgroundDark(R.color.darkBlue)
                .scrollable(false)
                .build())

        addSlide(SimpleSlide.Builder()
                .title("Permissions to use your camera")
                .description("I need Permission to use the camera to do my job.")
                .image(R.mipmap.ic_launcher)
                .background(R.color.red)
                .backgroundDark(R.color.darkRed)
                .scrollable(false)
                .permission(Manifest.permission.CAMERA)
                .build())

        buttonBackFunction = BUTTON_BACK_FUNCTION_BACK
    }

}
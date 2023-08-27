package pt.ulusofona.deisi.cm2223.g21702361

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import pt.ulusofona.deisi.cm2223.g21702361.MainActivity
import pt.ulusofona.deisi.cm2223.g21702361.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3000 milliseconds (3 seconds)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}

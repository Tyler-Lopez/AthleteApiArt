package com.company.athleteapiart

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.company.athleteapiart.ui.theme.AthleteApiArtTheme





class MainActivity : ComponentActivity() {

    val intentUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
        .buildUpon()
        .appendQueryParameter("client_id", "75992")
        .appendQueryParameter("redirect_uri", "com.company.athleteapiart://myapp.com")
        .appendQueryParameter("response_type", "code")
        .appendQueryParameter("approval_prompt", "auto")
        .appendQueryParameter("scope", "activity:read_all")
        .build()

    val intentSub = Intent(Intent.ACTION_VIEW, intentUri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            var intentSubRes = intentSub.dataString

            println("here")

            AthleteApiArtTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column() {
                        Button(onClick = {
                            startActivity(intentSub)
                        }) {
                            Text("Login with Strava")
                        }
                        Text(intentSubRes ?: "")
                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val uri = intent.data
        if (uri != null) {
            setContent {
                Text("${uri}")
            }
        }

    }
    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
    }
    // how to get access token from redirect uri in android
    // https://stackapps.com/questions/3174/how-to-get-access-token-from-redirect-uri-in-android
   // override fun onResume() {
   //     super.onResume()



  // }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AthleteApiArtTheme {
        Greeting("Android")
    }
}
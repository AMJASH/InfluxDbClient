package website.yuanhui.www.influxdb.client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import okhttp3.OkHttpClient
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import website.yuanhui.www.influxdb.client.ui.ShowResults
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


fun main() {
    mainWindows()
}

fun mainWindows() {
    singleWindowApplication(
        title = "InfluxDbClient(Desktop)", state = WindowState(size = DpSize(800.dp, 600.dp))
    ) {
        val url = remember { mutableStateOf("") }
        val username = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }
        val dbname = remember { mutableStateOf("") }
        val sql = remember { mutableStateOf("") }
        val result = remember { mutableStateOf(AAA(null, null)) }
        val showIndex = remember { mutableStateOf(0) }
        Column {
            Row {
                OutlinedTextField(value = url.value,
                    onValueChange = { str -> url.value = str },
                    modifier = Modifier.height(60.dp).widthIn(10.dp, 200.dp),
                    textStyle = TextStyle().merge(),
                    label = { Text("url") })
                OutlinedTextField(value = username.value,
                    onValueChange = { str -> username.value = str },
                    modifier = Modifier.height(60.dp).widthIn(10.dp, 200.dp),
                    textStyle = TextStyle().merge(),
                    label = { Text("username") })
                OutlinedTextField(value = password.value,
                    onValueChange = { str -> password.value = str },
                    modifier = Modifier.height(60.dp).widthIn(10.dp, 200.dp),
                    textStyle = TextStyle().merge(),
                    visualTransformation = VisualTransformation.None,
                    label = { Text("password") })
                OutlinedTextField(value = dbname.value,
                    onValueChange = { str -> dbname.value = str },
                    modifier = Modifier.height(60.dp).widthIn(10.dp, 200.dp),
                    textStyle = TextStyle().merge(),
                    label = { Text("dbname") })
            }
            Divider()
            Row {
                TextButton(
                    onClick = {
                        val connect = InfluxDBFactory.connect(url.value, username.value, password.value, okBuilder())
                        connect.setDatabase(dbname.value)
                        showIndex.value = 0
                        try {
                            result.value = AAA(connect.query(Query(sql.value)), null)
                        } catch (e: Exception) {
                            result.value = AAA(null, e)
                        }
                    }, border = BorderStroke(1.dp, Color.Black), modifier = Modifier.heightIn(60.dp)
                ) {
                    Text(text = "查询")
                }

                OutlinedTextField(
                    value = sql.value,
                    onValueChange = { str -> sql.value = str },
                    modifier = Modifier.heightIn(60.dp, 150.dp).widthIn(200.dp, 800.dp),
                    textStyle = TextStyle().merge(),
                    label = { Text("sql") },
                    singleLine = true
                )
            }
            Divider()
            getResultRow(result, showIndex)
        }
    }
}

//column 列
//row 行
@Composable//显示Sql返回的结果数或者异常
fun getResultRow(val_: MutableState<AAA>, showIndex: MutableState<Int>) {
    val aaa = val_.value
    val e = aaa.e?.toString() ?: ""
    val results = aaa.result?.results ?: Collections.emptyList()
    val widthIn = results.size * 100
    return Column {
        Divider()
        Text(text = e)
        Row(modifier = Modifier.size(widthIn.dp, 40.dp), horizontalArrangement = Arrangement.Start) {
            for (i in 0 until results.size) {
                TextButton(
                    onClick = {
                        showIndex.value = i
                    }, modifier = Modifier.size(100.dp, 40.dp), border = BorderStroke(1.dp, color = Color.Blue)
                ) {
                    Text("Result[$i]")
                }
            }
        }
        Divider()
        ShowResults(showIndex.value, results)
    }
}

fun okBuilder(): OkHttpClient.Builder {
    val target = OkHttpClient.Builder()
    try {
        val trustManager: X509TrustManager = object : X509TrustManager {
            override fun checkClientTrusted(x509Certificates: Array<X509Certificate?>?, s: String?) {}
            override fun checkServerTrusted(x509Certificates: Array<X509Certificate?>?, s: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }
        }
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
        val sslSocketFactory = sslContext.socketFactory
        target.sslSocketFactory(sslSocketFactory, trustManager).retryOnConnectionFailure(true)
            .hostnameVerifier { _: String?, _: SSLSession? -> true }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return target
}
class AAA(var result: QueryResult?, var e: Exception?)
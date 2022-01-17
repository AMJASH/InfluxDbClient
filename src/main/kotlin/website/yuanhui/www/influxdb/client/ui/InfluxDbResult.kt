package website.yuanhui.www.influxdb.client.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.influxdb.dto.QueryResult
import java.util.*

@Composable
fun ShowResults(i: Int, list: List<QueryResult.Result>) {
    if (list.isEmpty()) {
        return Column {}
    }
    val remember = remember { mutableStateOf(0) }
    return influxDbResult(remember, list[i])
}

@Composable
fun influxDbResult(i: MutableState<Int>, res: QueryResult.Result) {
    val series = res.series ?: Collections.emptyList()
    if (series.isEmpty()) {
        return Column {}
    }
    return Column(modifier = Modifier.height(Dp.Infinity).width(Dp.Infinity)) {
        Text(res.error ?: "")
        Row {
            for (it in 0 until series.size) {
                TextButton(
                    onClick = { i.value = it },
                    modifier = Modifier.size(Dp.Unspecified, Dp.Unspecified),
                    border = BorderStroke(1.dp, color = Color.Blue),
                ) {
                    Text(series[it].name ?: "", fontSize = 10.sp)
                }
            }
        }
        Divider()
        series(series[i.value])
    }
}

@Composable
fun series(val_: QueryResult.Series) {
    val columns = val_.columns ?: Collections.emptyList()
    if (columns.isEmpty()) {
        return LazyColumn {}
    }
    var values = val_.values ?: Collections.emptyList()
    if (values.isEmpty()) {
        return LazyColumn {}
    }

    return LazyColumn {
        item {
            LazyRow {
                items(columns.size) {
                    TextField(value = columns[it], onValueChange = {}, readOnly = true)
                }
            }
        }
        items(values.size) {
            val res = values[it]
            Row(modifier = Modifier.height(Dp.Infinity).width(Dp.Infinity)) {
                for (re in res) {

                    TextField(value = (re?.toString()?:"null"), onValueChange = {}, readOnly = true)
                }
            }
        }
    }
}

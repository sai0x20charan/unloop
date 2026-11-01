package com.charan.unloop.presentation.stats.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.deeplink.invoke
import com.charan.unloop.core.utils.DateTimeUtils.getDisplayName
import com.charan.unloop.core.utils.DateTimeUtils.toFocusDuration
import com.charan.unloop.presentation.stats.GraphData
import com.charan.unloop.presentation.stats.StatsType
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.VicoTheme
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaDayOfWeek
import java.time.format.TextStyle
import androidx.compose.ui.platform.LocalResources
import com.charan.unloop.core.utils.DateTimeUtils.toFocusHours
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import kotlin.math.ceil

@Composable
fun StatGraph(
    graphData : List<GraphData> = emptyList(),
    statType : StatsType = StatsType.WEEKLY

) {
    val values = graphData.map{ it.value }
    val resources = LocalResources.current
    val maxAxisSeconds = ceil((values.maxOrNull() ?: 0) / 3600.0)
        .coerceAtLeast(1.0) * 3600.0


    val modelProducer = remember { CartesianChartModelProducer() }
    val labelListKey = remember { ExtraStore.Key<List<String>>() }

    val graph = rememberLineComponent(
        fill = Fill(color = MaterialTheme.colorScheme.primary),
        shape = CircleShape,
        thickness = 8.dp
    )
    LaunchedEffect(graphData, statType) {
        if(graphData.isNotEmpty()) {
            modelProducer.runTransaction {
                columnModel { series(values) }
                extras { it[labelListKey] = if(statType == StatsType.WEEKLY) graphData.map { it.date.dayOfWeek.getDisplayName(
                    TextStyle.SHORT) } else graphData.map { it.date.day.toString() } }

            }
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            modifier = Modifier.fillMaxHeight(),
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = remember(graph,graphData) {
                        getColumnProvider(
                            graph,

                            graphData
                        )
                    },
                    rangeProvider = CartesianLayerRangeProvider.fixed(
                        minY = 0.0,
                        maxY = maxAxisSeconds
                    ),
                ),
                marker = rememberDefaultCartesianMarker(
                    label = rememberTextComponent(
                        style = androidx.compose.ui.text.TextStyle(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        padding = Insets(horizontal = 12.dp, vertical = 8.dp),
                        margins = Insets(vertical = 2.dp),
                        background = rememberShapeComponent(
                            shape = CircleShape,
                            fill = Fill(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        ),

                    ),
                    labelPosition = DefaultCartesianMarker.LabelPosition.AbovePoint,
                    valueFormatter = DefaultCartesianMarker.ValueFormatter { context,targets ->
                        val target = targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget
                        val column = target?.columns?.firstOrNull()
                        val x = target?.x?.toInt() ?: 0
                        val y = column?.entry?.y ?: 0.0


                        y.toInt().toFocusDuration(resources)

                    }
                ),


                startAxis = VerticalAxis.rememberStart(
                    guideline = rememberAxisGuidelineComponent(
                        thickness = 0.5.dp,
                        fill = Fill(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)),
                        shape = CircleShape,
                        margins = Insets(horizontal = 5.dp)
                    ),


                    line = null,
                    tick = null,
                    itemPlacer = VerticalAxis.ItemPlacer.step({ 3600.0 }) ,
                    valueFormatter = CartesianValueFormatter { context, y, _ ->
                        y.toInt().toFocusHours(resources)
                    }
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    guideline = null,
                    line = null,
                    tick = null,
                    valueFormatter = CartesianValueFormatter { context, x, _ ->
                        val index = x.toInt()
                        context.model.extraStore[labelListKey]
                            .getOrNull(index)
                            ?: ""
                    },

                ),


            ),

            zoomState = rememberVicoZoomState(zoomEnabled = false, maxZoom = Zoom.Content),
            scrollState = rememberVicoScrollState(scrollEnabled = false),


            modelProducer = modelProducer

        )
    }

}

@Preview
@Composable
fun StatGraphPreview(){
    StatGraph(
        graphData = listOf(
            GraphData(
                value = 10,
                day = DayOfWeek.MONDAY,
                date = LocalDate(2024, 6, 1)

            ),
            GraphData(
                value = 20,
                day = DayOfWeek.TUESDAY,
                date = LocalDate(2024, 6, 2)

            ),
            GraphData(
                value = 30,
                day = DayOfWeek.WEDNESDAY,
                date = LocalDate(2024, 6, 3)

            )
        )
    )
}

private fun getColumnProvider(graph : LineComponent, values : List<GraphData>) =
    object : ColumnCartesianLayer.ColumnProvider {
        override fun getColumn(
            entry: ColumnCartesianLayerModel.Entry,
            extraStore: ExtraStore
        ): LineComponent {
            return graph
        }

        override fun getWidestSeriesColumn(
            seriesKey: Any,
            seriesIndex: Int,
            extraStore: ExtraStore
        ): LineComponent {
            return graph
        }
    }

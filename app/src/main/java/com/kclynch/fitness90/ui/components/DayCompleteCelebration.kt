package com.kclynch.fitness90.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val angleDegrees: Float,
    val distance: Float,
    val size: Float,
    val color: Color,
    val fullRotation: Float
)

private val ConfettiColors = listOf(
    Color(0xFF2E7D32),
    Color(0xFFFFC107),
    Color(0xFFE53935),
    Color(0xFF1E88E5),
    Color(0xFF8E24AA),
    Color(0xFFFF7043)
)

/**
 * A brief confetti burst + "Day Complete" text, shown as a full-screen
 * overlay. [key] should change on every trigger so the particle layout
 * re-randomizes each time.
 */
@Composable
fun DayCompleteCelebration(visible: Boolean, key: Any, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val particles = remember(key) {
            List(28) {
                ConfettiParticle(
                    angleDegrees = Random.nextFloat() * 360f,
                    distance = 220f + Random.nextFloat() * 260f,
                    size = 6f + Random.nextFloat() * 8f,
                    color = ConfettiColors[Random.nextInt(ConfettiColors.size)],
                    fullRotation = 180f + Random.nextFloat() * 360f
                )
            }
        }

        val progressAnim = remember(key) { Animatable(0f) }
        LaunchedEffect(key) {
            progressAnim.snapTo(0f)
            progressAnim.animateTo(1f, animationSpec = tween(durationMillis = 1100, easing = LinearOutSlowInEasing))
        }
        val progress = progressAnim.value

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val eased = 1f - (1f - progress) * (1f - progress)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                particles.forEach { particle ->
                    val dist = particle.distance * eased
                    val x = center.x + cos(Math.toRadians(particle.angleDegrees.toDouble())).toFloat() * dist
                    val y = center.y + sin(Math.toRadians(particle.angleDegrees.toDouble())).toFloat() * dist -
                        (particle.distance * 0.3f * progress)
                    rotate(degrees = particle.fullRotation * progress, pivot = Offset(x, y)) {
                        drawRect(
                            color = particle.color.copy(alpha = alpha),
                            topLeft = Offset(x - particle.size / 2f, y - particle.size / 2f),
                            size = Size(particle.size, particle.size)
                        )
                    }
                }
            }
            Text(
                text = "🎉 Day Complete! 🎉",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

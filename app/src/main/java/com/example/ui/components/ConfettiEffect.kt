package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.SageGreenLight
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.WarmAmber
import kotlin.random.Random

private data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float
)

@Composable
fun MicroConfettiEffect(
    trigger: Boolean,
    onAnimationEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }
    val colors = listOf(SageGreenLight, SoftCyan, WarmAmber, SoftViolet, Color(0xFFF472B6))

    val particles = remember {
        List(40) {
            ConfettiParticle(
                initialX = 0.5f + (Random.nextFloat() - 0.5f) * 0.3f,
                initialY = 0.4f,
                velocityX = (Random.nextFloat() - 0.5f) * 600f,
                velocityY = -(Random.nextFloat() * 450f + 150f),
                color = colors.random(),
                size = Random.nextFloat() * 8f + 5f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = LinearEasing)
        )
        onAnimationEnd()
    }

    val t = progress.value
    if (t < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val gravity = 900f
            particles.forEach { particle ->
                val x = size.width * particle.initialX + particle.velocityX * t
                val y = size.height * particle.initialY + (particle.velocityY * t + 0.5f * gravity * t * t)
                val alpha = (1f - t).coerceIn(0f, 1f)

                rotate(
                    degrees = particle.rotationSpeed * t,
                    pivot = Offset(x, y)
                ) {
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = Offset(x - particle.size / 2, y - particle.size / 2),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}

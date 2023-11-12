package us.huseli.retaintheme.snackbar

import androidx.compose.material3.SnackbarDuration
import java.util.UUID

data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short,
    val onActionPerformed: (() -> Unit)? = null,
    val onDismissed: (() -> Unit)? = null,
) {
    val id: UUID = UUID.randomUUID()

    override fun equals(other: Any?) = other is SnackbarMessage && other.id == id

    override fun hashCode(): Int = id.hashCode()
}

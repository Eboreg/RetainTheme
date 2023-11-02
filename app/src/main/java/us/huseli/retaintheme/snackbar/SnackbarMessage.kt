package us.huseli.retaintheme.snackbar

import java.util.UUID

data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val onActionPerformed: (() -> Unit)? = null,
    val onDismissed: (() -> Unit)? = null,
) {
    val id: UUID = UUID.randomUUID()

    override fun equals(other: Any?) = other is SnackbarMessage && other.id == id

    override fun hashCode(): Int = id.hashCode()
}

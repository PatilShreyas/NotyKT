package dev.shreyaspatil.noty.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.model.NotyTaskAction
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.di.LocalRepository
import dev.shreyaspatil.noty.di.RemoteRepository
import dev.shreyaspatil.noty.utils.getEnum
import kotlinx.coroutines.flow.first

class NotyTaskWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @RemoteRepository private val remoteNoteRepository: NotyNoteRepository,
    @LocalRepository private val localNoteRepository: NotyNoteRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RUN_ATTEMPTS) return Result.failure()

        val noteId = getNoteId()

        return when (getTaskAction()) {
            NotyTaskAction.CREATE -> addNote(noteId)
            NotyTaskAction.UPDATE -> updateNote(noteId)
            NotyTaskAction.DELETE -> deleteNote(noteId)
        }
    }

    private suspend fun addNote(tempNoteId: String): Result {
        val note = fetchLocalNote(tempNoteId)
        val response = remoteNoteRepository.addNote(note.title, note.note)
        return if (response is ResponseResult.Success) {
            // `response.data` will be a noteId received from API.
            localNoteRepository.updateNoteId(tempNoteId, response.data)
            Result.success()
        } else Result.retry()
    }

    private suspend fun updateNote(noteId: String): Result {
        val note = fetchLocalNote(noteId)
        val response = remoteNoteRepository.updateNote(note.id, note.title, note.note)
        return if (response is ResponseResult.Success) Result.success() else Result.retry()
    }

    private suspend fun deleteNote(noteId: String): Result {
        val response = remoteNoteRepository.deleteNote(noteId)
        return if (response is ResponseResult.Success) Result.success() else Result.retry()
    }

    private suspend fun fetchLocalNote(noteId: String): Note =
        localNoteRepository.getNoteById(noteId).first()

    private fun getNoteId(): String = inputData.getString(KEY_NOTE_ID)
        ?: throw IllegalStateException("$KEY_NOTE_ID should be provided as input data.")

    private fun getTaskAction(): NotyTaskAction = inputData.getEnum<NotyTaskAction>(KEY_TASK_TYPE)
        ?: throw IllegalStateException("$KEY_TASK_TYPE should be provided as input data.")

    companion object {
        const val MAX_RUN_ATTEMPTS = 3
        const val KEY_NOTE_ID = "note_id"
        const val KEY_TASK_TYPE = "noty_task_type"
    }
}
